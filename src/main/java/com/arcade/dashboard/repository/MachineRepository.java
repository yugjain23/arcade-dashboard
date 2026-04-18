package com.arcade.dashboard.repository;

import com.arcade.dashboard.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    Optional<Machine> findByMachineId(String machineId);

    boolean existsByMachineId(String machineId);

    boolean existsByName(String name);

    // Find machines that haven't reported in since a given time
    List<Machine> findByLastSeenBeforeAndStatusNot(LocalDateTime threshold, String status);

    // Find all online machines
    List<Machine> findByStatus(String status);

    // Sum of all play counts
    @Query("SELECT COALESCE(SUM(m.playCount), 0) FROM Machine m")
    Long sumAllPlayCounts();

    // Sum of all sales
    @Query("SELECT COALESCE(SUM(m.playCount * m.pricePerPlay), 0.0) FROM Machine m")
    Double sumAllTotalSales();
}
