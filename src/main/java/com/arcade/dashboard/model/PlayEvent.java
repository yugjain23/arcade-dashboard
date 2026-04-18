package com.arcade.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "play_events")
public class PlayEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String machineId;

    @Column(nullable = false)
    private Long count;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column
    private String rfidTag;   // optional: which RFID card triggered it

    public PlayEvent() {
        this.receivedAt = LocalDateTime.now();
    }

    public PlayEvent(String machineId, Long count) {
        this();
        this.machineId = machineId;
        this.count = count;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public String getRfidTag() { return rfidTag; }
    public void setRfidTag(String rfidTag) { this.rfidTag = rfidTag; }
}
