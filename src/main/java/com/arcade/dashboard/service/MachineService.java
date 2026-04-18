package com.arcade.dashboard.service;

import com.arcade.dashboard.model.Machine;
import com.arcade.dashboard.model.PlayEvent;
import com.arcade.dashboard.repository.MachineRepository;
import com.arcade.dashboard.repository.PlayEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MachineService {

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private PlayEventRepository playEventRepository;

    @Value("${arcade.offline.threshold.minutes:5}")
    private int offlineThresholdMinutes;

    // ── Machine CRUD ───────────────────────────────────────────

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Optional<Machine> getMachineById(Long id) {
        return machineRepository.findById(id);
    }

    public Optional<Machine> getMachineByMachineId(String machineId) {
        return machineRepository.findByMachineId(machineId);
    }

    @Transactional
    public Machine addMachine(Machine machine) {
        if (machineRepository.existsByMachineId(machine.getMachineId())) {
            throw new IllegalArgumentException(
                    "Machine ID '" + machine.getMachineId() + "' already exists.");
        }
        if (machineRepository.existsByName(machine.getName())) {
            throw new IllegalArgumentException(
                    "Machine name '" + machine.getName() + "' already exists.");
        }
        machine.setCreatedAt(LocalDateTime.now());
        machine.setPlayCount(0L);
        machine.setDailyPlayCount(0L);
        machine.setStatus("OFFLINE");
        machine.setRfidStatus("UNKNOWN");
        machine.setRelayStatus("UNKNOWN");
        machine.setLcdStatus("UNKNOWN");
        machine.setPendingOfflineCount(0L);
        return machineRepository.save(machine);
    }

    @Transactional
    public Machine updateMachine(Long id, Machine updated) {
        Machine existing = machineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Machine not found: " + id));
        existing.setName(updated.getName());
        existing.setPricePerPlay(updated.getPricePerPlay());
        existing.setLocation(updated.getLocation());
        existing.setNotes(updated.getNotes());
        return machineRepository.save(existing);
    }

    @Transactional
    public void deleteMachine(Long id) {
        machineRepository.deleteById(id);
    }

    @Transactional
    public void resetCount(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Machine not found: " + id));
        machine.setPlayCount(0L);
        machine.setDailyPlayCount(0L);
        machine.setPendingOfflineCount(0L);
        machineRepository.save(machine);
    }

    // ── Reset counts by machineId string (called by ESP32 card) ──

    @Transactional
    public Map<String, Object> resetMachineCountsById(String machineId) {
        Map<String, Object> response = new HashMap<>();
        Optional<Machine> opt = machineRepository.findByMachineId(machineId);

        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Unknown machine: " + machineId);
            return response;
        }

        Machine machine = opt.get();
        machine.setPlayCount(0L);
        machine.setDailyPlayCount(0L);
        machine.setPendingOfflineCount(0L);
        machine.setLastDailyReset(LocalDateTime.now());
        machineRepository.save(machine);

        PlayEvent event = new PlayEvent(machineId, 0L);
        event.setRfidTag("DASHBOARD_RESET");
        playEventRepository.save(event);

        System.out.println("Machine counts reset via card: " + machineId);

        response.put("success", true);
        response.put("message", "Counts reset for: " + machine.getName());
        return response;
    }

    // ── ESP32 Play Data ────────────────────────────────────────

    @Transactional
    public Map<String, Object> receivePlayData(String machineId, Long count,
                                               String rfidTag, Long dailyCount, Long totalCount,
                                               Boolean relayOk, Boolean rfidOk, Boolean lcdOk) {

        Map<String, Object> response = new HashMap<>();
        Optional<Machine> opt = machineRepository.findByMachineId(machineId);

        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Unknown machine ID: " + machineId);
            return response;
        }

        Machine machine = opt.get();

        // Update total play count
        machine.setPlayCount(machine.getPlayCount() + count);

        // Daily count — trust ESP32 value if provided
        if (dailyCount != null) {
            machine.setDailyPlayCount(dailyCount);
        } else {
            LocalDate today = LocalDate.now();
            if (machine.getLastDailyReset() == null ||
                    machine.getLastDailyReset().toLocalDate().isBefore(today)) {
                machine.setDailyPlayCount(0L);
                machine.setLastDailyReset(LocalDateTime.now());
            }
            machine.setDailyPlayCount(machine.getDailyPlayCount() + count);
        }

        // Update timestamps
        machine.setLastSeen(LocalDateTime.now());

        // Update component statuses
        if (rfidOk  != null) machine.setRfidStatus(rfidOk  ? "OK" : "FAULT");
        if (relayOk != null) machine.setRelayStatus(relayOk ? "OK" : "FAULT");
        if (lcdOk   != null) machine.setLcdStatus(lcdOk    ? "OK" : "FAULT");

        // Determine overall status
        boolean anyFault = "FAULT".equals(machine.getRfidStatus())  ||
                "FAULT".equals(machine.getRelayStatus()) ||
                "FAULT".equals(machine.getLcdStatus());
        machine.setStatus(anyFault ? "FAULT" : "ONLINE");

        // Clear offline pending count on successful sync
        if ("OFFLINE_SYNC".equals(rfidTag)) {
            machine.setPendingOfflineCount(0L);
            System.out.println("Offline sync received for: " + machineId +
                    " count=" + count);
        }

        machineRepository.save(machine);

        // Log event
        PlayEvent event = new PlayEvent(machineId, count);
        if (rfidTag != null && !rfidTag.isBlank()) {
            event.setRfidTag(rfidTag);
        }
        playEventRepository.save(event);

        response.put("success",    true);
        response.put("message",    "OK");
        response.put("totalCount", machine.getPlayCount());
        response.put("dailyCount", machine.getDailyPlayCount());
        response.put("totalSales", machine.getTotalSales());
        response.put("dailySales", machine.getDailySales());
        return response;
    }

    // ── Hardware Status Heartbeat ──────────────────────────────

    @Transactional
    public Map<String, Object> updateHardwareStatus(String machineId,
                                                    Boolean rfidOk, Boolean lcdOk, Boolean relayOk,
                                                    Integer wifiRssi, Long freeHeap, Long uptime) {

        Map<String, Object> response = new HashMap<>();
        Optional<Machine> opt = machineRepository.findByMachineId(machineId);

        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Unknown machine: " + machineId);
            return response;
        }

        Machine machine = opt.get();

        if (rfidOk  != null) machine.setRfidStatus(rfidOk  ? "OK" : "FAULT");
        if (lcdOk   != null) machine.setLcdStatus(lcdOk    ? "OK" : "FAULT");
        if (relayOk != null) machine.setRelayStatus(relayOk ? "OK" : "FAULT");
        if (wifiRssi != null) machine.setWifiRssi(wifiRssi);
        if (freeHeap != null) machine.setFreeHeap(freeHeap);
        if (uptime   != null) machine.setUptimeSeconds(uptime);

        machine.setLastSeen(LocalDateTime.now());

        boolean anyFault = "FAULT".equals(machine.getRfidStatus())  ||
                "FAULT".equals(machine.getRelayStatus()) ||
                "FAULT".equals(machine.getLcdStatus());
        machine.setStatus(anyFault ? "FAULT" : "ONLINE");

        machineRepository.save(machine);

        response.put("success", true);
        response.put("message", "Status updated");
        return response;
    }

    // ── Fault Report ───────────────────────────────────────────

    @Transactional
    public Map<String, Object> receiveFaultReport(String machineId,
                                                  String faultType, String message) {

        Map<String, Object> response = new HashMap<>();
        Optional<Machine> opt = machineRepository.findByMachineId(machineId);

        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Unknown machine: " + machineId);
            return response;
        }

        Machine machine = opt.get();
        machine.setLastFaultType(faultType);
        machine.setLastFaultMessage(message);
        machine.setLastFaultTime(LocalDateTime.now());

        switch (faultType) {
            case "RELAY_FAIL":
            case "RELAY_DISCONNECTED":
                machine.setRelayStatus("FAULT"); break;
            case "RFID_OFFLINE":
            case "RFID_DISCONNECTED":
                machine.setRfidStatus("FAULT"); break;
            case "LCD_DISCONNECTED":
                machine.setLcdStatus("FAULT"); break;
        }

        machine.setStatus("FAULT");
        machine.setLastSeen(LocalDateTime.now());
        machineRepository.save(machine);

        System.out.println("FAULT: " + machineId + " → " +
                faultType + " — " + message);

        response.put("success", true);
        response.put("message", "Fault recorded");
        return response;
    }

    // ── Fault Clear ────────────────────────────────────────────

    @Transactional
    public Map<String, Object> clearFault(String machineId,
                                          String faultType, String message) {

        Map<String, Object> response = new HashMap<>();
        Optional<Machine> opt = machineRepository.findByMachineId(machineId);

        if (opt.isEmpty()) {
            response.put("success", false);
            return response;
        }

        Machine machine = opt.get();

        switch (faultType) {
            case "RFID_DISCONNECTED":
            case "RFID_OFFLINE":
                machine.setRfidStatus("OK");  break;
            case "LCD_DISCONNECTED":
                machine.setLcdStatus("OK");   break;
            case "RELAY_FAIL":
            case "RELAY_DISCONNECTED":
                machine.setRelayStatus("OK"); break;
        }

        boolean allClear = "OK".equals(machine.getRfidStatus())  &&
                "OK".equals(machine.getRelayStatus()) &&
                "OK".equals(machine.getLcdStatus());
        if (allClear) machine.setStatus("ONLINE");

        machine.setLastSeen(LocalDateTime.now());
        machineRepository.save(machine);

        System.out.println("Fault cleared: " + faultType +
                " on " + machineId);

        response.put("success", true);
        response.put("message", "Fault cleared: " + faultType);
        return response;
    }

    // ── Scheduled: Offline Detection ──────────────────────────

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void detectOfflineMachines() {
        LocalDateTime threshold =
                LocalDateTime.now().minusMinutes(offlineThresholdMinutes);
        List<Machine> machines = machineRepository.findAll();

        for (Machine machine : machines) {
            if ("ONLINE".equals(machine.getStatus()) ||
                    "FAULT".equals(machine.getStatus())) {
                if (machine.getLastSeen() == null ||
                        machine.getLastSeen().isBefore(threshold)) {
                    machine.setStatus("OFFLINE");
                    machineRepository.save(machine);
                    System.out.println("Machine OFFLINE: " +
                            machine.getName());
                }
            }
        }
    }

    // ── Scheduled: Midnight Daily Reset ───────────────────────

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDailyCountsAtMidnight() {
        List<Machine> machines = machineRepository.findAll();
        for (Machine m : machines) {
            m.setDailyPlayCount(0L);
            m.setLastDailyReset(LocalDateTime.now());
            machineRepository.save(m);
        }
        System.out.println("Daily counts reset at midnight.");
    }

    // ── Dashboard Stats ────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        List<Machine> all = machineRepository.findAll();

        long totalPlays   = all.stream().mapToLong(Machine::getPlayCount).sum();
        long dailyPlays   = all.stream().mapToLong(Machine::getDailyPlayCount).sum();
        double totalSales = all.stream().mapToDouble(Machine::getTotalSales).sum();
        double dailySales = all.stream().mapToDouble(Machine::getDailySales).sum();
        long onlineCount  = all.stream().filter(m -> "ONLINE".equals(m.getStatus())).count();
        long offlineCount = all.stream().filter(m -> "OFFLINE".equals(m.getStatus())).count();
        long faultCount   = all.stream().filter(m -> "FAULT".equals(m.getStatus())).count();
        long rfidFaults   = all.stream().filter(m -> "FAULT".equals(m.getRfidStatus())).count();
        long relayFaults  = all.stream().filter(m -> "FAULT".equals(m.getRelayStatus())).count();
        long lcdFaults    = all.stream().filter(m -> "FAULT".equals(m.getLcdStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMachines",   all.size());
        stats.put("onlineMachines",  onlineCount);
        stats.put("offlineMachines", offlineCount);
        stats.put("faultMachines",   faultCount);
        stats.put("totalPlays",      totalPlays);
        stats.put("dailyPlays",      dailyPlays);
        stats.put("totalSales",      String.format("%.2f", totalSales));
        stats.put("dailySales",      String.format("%.2f", dailySales));
        stats.put("rfidFaults",      rfidFaults);
        stats.put("relayFaults",     relayFaults);
        stats.put("lcdFaults",       lcdFaults);
        stats.put("machines",        all);
        return stats;
    }

    public List<PlayEvent> getRecentEvents() {
        return playEventRepository.findTop20ByOrderByReceivedAtDesc();
    }

    // ── Chart Data ─────────────────────────────────────────────

    public Map<String, Object> getChartData() {
        List<Machine> machines = machineRepository.findAll();

        List<String> labels    = new ArrayList<>();
        List<Double> salesData = new ArrayList<>();
        List<Long>   countData = new ArrayList<>();
        List<Long>   dailyData = new ArrayList<>();

        for (Machine m : machines) {
            labels.add(m.getName());
            salesData.add(m.getTotalSales());
            countData.add(m.getPlayCount());
            dailyData.add(m.getDailyPlayCount());
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels",    labels);
        chartData.put("salesData", salesData);
        chartData.put("countData", countData);
        chartData.put("dailyData", dailyData);
        return chartData;
    }
}