package com.arcade.dashboard.repository;

import com.arcade.dashboard.model.PlayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {

    List<PlayEvent> findByMachineIdOrderByReceivedAtDesc(String machineId);

    // Recent events for activity log
    List<PlayEvent> findTop20ByOrderByReceivedAtDesc();

    // Events in time range
    List<PlayEvent> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);

    // Count events by machine
    @Query("SELECT p.machineId, COUNT(p) FROM PlayEvent p GROUP BY p.machineId")
    List<Object[]> countEventsByMachine();
}
