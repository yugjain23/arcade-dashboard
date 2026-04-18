package com.arcade.dashboard.controller;

import com.arcade.dashboard.service.MachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private MachineService machineService;

    // ── Play data POST ─────────────────────────────────────────

    @PostMapping("/play")
    public ResponseEntity<Map<String, Object>> receivePlayDataPost(
            @RequestBody Map<String, Object> payload) {

        String  machineId  = (String) payload.get("machineId");
        Long    count      = Long.parseLong(
                payload.getOrDefault("count", 1).toString());
        String  rfidTag    = (String) payload.get("rfidTag");
        Long    dailyCount = payload.get("dailyCount") != null ?
                Long.parseLong(payload.get("dailyCount").toString()) : null;
        Long    totalCount = payload.get("totalCount") != null ?
                Long.parseLong(payload.get("totalCount").toString()) : null;
        Boolean relayOk    = payload.get("relayOk") != null ?
                Boolean.parseBoolean(payload.get("relayOk").toString()) : null;
        Boolean rfidOk     = payload.get("rfidOk") != null ?
                Boolean.parseBoolean(payload.get("rfidOk").toString()) : null;
        Boolean lcdOk      = payload.get("lcdOk") != null ?
                Boolean.parseBoolean(payload.get("lcdOk").toString()) : null;

        if (machineId == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "machineId is required"));
        }

        Map<String, Object> result = machineService.receivePlayData(
                machineId, count, rfidTag, dailyCount,
                totalCount, relayOk, rfidOk, lcdOk);

        return Boolean.TRUE.equals(result.get("success"))
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    // ── Play data GET (easier for ESP32) ───────────────────────

    @GetMapping("/play")
    public ResponseEntity<Map<String, Object>> receivePlayDataGet(
            @RequestParam String machineId,
            @RequestParam(defaultValue = "1") Long count,
            @RequestParam(required = false) String rfidTag,
            @RequestParam(required = false) Long dailyCount,
            @RequestParam(required = false) Long totalCount,
            @RequestParam(required = false) Boolean relayOk,
            @RequestParam(required = false) Boolean rfidOk,
            @RequestParam(required = false) Boolean lcdOk) {

        Map<String, Object> result = machineService.receivePlayData(
                machineId, count, rfidTag, dailyCount,
                totalCount, relayOk, rfidOk, lcdOk);

        return Boolean.TRUE.equals(result.get("success"))
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    // ── Hardware status heartbeat ──────────────────────────────

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> receiveHardwareStatus(
            @RequestParam String machineId,
            @RequestParam(required = false) Boolean rfidOk,
            @RequestParam(required = false) Boolean lcdOk,
            @RequestParam(required = false) Boolean relayOk,
            @RequestParam(required = false) Integer wifiRssi,
            @RequestParam(required = false) Long freeHeap,
            @RequestParam(required = false) Long uptime) {

        Map<String, Object> result = machineService.updateHardwareStatus(
                machineId, rfidOk, lcdOk, relayOk,
                wifiRssi, freeHeap, uptime);
        return ResponseEntity.ok(result);
    }

    // ── Fault report ───────────────────────────────────────────

    @GetMapping("/fault")
    public ResponseEntity<Map<String, Object>> receiveFault(
            @RequestParam String machineId,
            @RequestParam String faultType,
            @RequestParam(required = false, defaultValue = "") String message) {

        Map<String, Object> result =
                machineService.receiveFaultReport(machineId, faultType, message);
        return ResponseEntity.ok(result);
    }

    // ── Fault clear ────────────────────────────────────────────

    @GetMapping("/fault/clear")
    public ResponseEntity<Map<String, Object>> clearFault(
            @RequestParam String machineId,
            @RequestParam String faultType,
            @RequestParam(required = false, defaultValue = "") String message) {

        Map<String, Object> result =
                machineService.clearFault(machineId, faultType, message);
        return ResponseEntity.ok(result);
    }

    // ── Machine reset (dashboard reset card) ───────────────────

    @GetMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetMachine(
            @RequestParam String machineId) {

        Map<String, Object> result =
                machineService.resetMachineCountsById(machineId);
        return ResponseEntity.ok(result);
    }

    // ── Dashboard stats ────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(machineService.getDashboardStats());
    }

    // ── Chart data ─────────────────────────────────────────────

    @GetMapping("/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData() {
        return ResponseEntity.ok(machineService.getChartData());
    }

    // ── Recent events ──────────────────────────────────────────

    @GetMapping("/events")
    public ResponseEntity<?> getRecentEvents() {
        return ResponseEntity.ok(machineService.getRecentEvents());
    }

    // ── Health check ───────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "Arcade Dashboard API"
        ));
    }
}