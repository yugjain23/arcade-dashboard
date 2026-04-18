package com.arcade.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Machine name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Machine ID is required")
    @Column(nullable = false, unique = true)
    private String machineId;

    @NotNull(message = "Price per play is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false)
    private Double pricePerPlay;

    @Column(nullable = false)
    private Long playCount = 0L;

    @Column(nullable = false)
    private Long dailyPlayCount = 0L;

    @Column
    private LocalDateTime lastDailyReset;

    @Column(nullable = false)
    private String status = "OFFLINE";

    @Column
    private LocalDateTime lastSeen;

    @Column
    private LocalDateTime createdAt;

    @Column
    private String location;

    @Column
    private String notes;

    @Column(nullable = false)
    private String rfidStatus = "UNKNOWN";

    @Column(nullable = false)
    private String relayStatus = "UNKNOWN";

    @Column(nullable = false)
    private String lcdStatus = "UNKNOWN";

    @Column
    private String lastFaultType;

    @Column
    private String lastFaultMessage;

    @Column
    private LocalDateTime lastFaultTime;

    @Column(nullable = false)
    private Long pendingOfflineCount = 0L;

    @Column
    private Integer wifiRssi;

    @Column
    private Long freeHeap;

    @Column
    private Long uptimeSeconds;

    // ── Constructors ───────────────────────────────────────────
    public Machine() {
        this.createdAt            = LocalDateTime.now();
        this.playCount            = 0L;
        this.dailyPlayCount       = 0L;
        this.status               = "OFFLINE";
        this.rfidStatus           = "UNKNOWN";
        this.relayStatus          = "UNKNOWN";
        this.lcdStatus            = "UNKNOWN";
        this.pendingOfflineCount  = 0L;
    }

    public Machine(String name, String machineId, Double pricePerPlay) {
        this();
        this.name         = name;
        this.machineId    = machineId;
        this.pricePerPlay = pricePerPlay;
    }

    // ── Computed Fields ────────────────────────────────────────
    public Double getTotalSales() {
        return playCount * pricePerPlay;
    }

    public Double getDailySales() {
        return dailyPlayCount * pricePerPlay;
    }

    public boolean isOnline() {
        return "ONLINE".equals(status);
    }

    public boolean hasFault() {
        return "FAULT".equals(rfidStatus)  ||
                "FAULT".equals(relayStatus) ||
                "FAULT".equals(lcdStatus)   ||
                "FAULT".equals(status);
    }

    public String getWifiSignalStrength() {
        if (wifiRssi == null)    return "Unknown";
        if (wifiRssi >= -50)     return "Excellent";
        if (wifiRssi >= -60)     return "Good";
        if (wifiRssi >= -70)     return "Fair";
        return "Weak";
    }

    // ── Getters and Setters ────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public Double getPricePerPlay() { return pricePerPlay; }
    public void setPricePerPlay(Double pricePerPlay) { this.pricePerPlay = pricePerPlay; }

    public Long getPlayCount() { return playCount; }
    public void setPlayCount(Long playCount) { this.playCount = playCount; }

    public Long getDailyPlayCount() { return dailyPlayCount; }
    public void setDailyPlayCount(Long dailyPlayCount) { this.dailyPlayCount = dailyPlayCount; }

    public LocalDateTime getLastDailyReset() { return lastDailyReset; }
    public void setLastDailyReset(LocalDateTime lastDailyReset) { this.lastDailyReset = lastDailyReset; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRfidStatus() { return rfidStatus; }
    public void setRfidStatus(String rfidStatus) { this.rfidStatus = rfidStatus; }

    public String getRelayStatus() { return relayStatus; }
    public void setRelayStatus(String relayStatus) { this.relayStatus = relayStatus; }

    public String getLcdStatus() { return lcdStatus; }
    public void setLcdStatus(String lcdStatus) { this.lcdStatus = lcdStatus; }

    public String getLastFaultType() { return lastFaultType; }
    public void setLastFaultType(String lastFaultType) { this.lastFaultType = lastFaultType; }

    public String getLastFaultMessage() { return lastFaultMessage; }
    public void setLastFaultMessage(String lastFaultMessage) { this.lastFaultMessage = lastFaultMessage; }

    public LocalDateTime getLastFaultTime() { return lastFaultTime; }
    public void setLastFaultTime(LocalDateTime lastFaultTime) { this.lastFaultTime = lastFaultTime; }

    public Long getPendingOfflineCount() { return pendingOfflineCount; }
    public void setPendingOfflineCount(Long pendingOfflineCount) { this.pendingOfflineCount = pendingOfflineCount; }

    public Integer getWifiRssi() { return wifiRssi; }
    public void setWifiRssi(Integer wifiRssi) { this.wifiRssi = wifiRssi; }

    public Long getFreeHeap() { return freeHeap; }
    public void setFreeHeap(Long freeHeap) { this.freeHeap = freeHeap; }

    public Long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(Long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }
}