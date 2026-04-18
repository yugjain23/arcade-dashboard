package com.arcade.dashboard.config;

import com.arcade.dashboard.model.Machine;
import com.arcade.dashboard.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds the database with demo machines when the app first starts.
 * Safe to run multiple times (checks if data already exists).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MachineRepository machineRepository;

    @Override
    public void run(String... args) {
        if (machineRepository.count() == 0) {
            // Seed demo data
            Machine m1 = new Machine("Street Fighter II", "MACHINE_001", 10.00);
            m1.setLocation("Zone A");
            m1.setPlayCount(142L);
            m1.setLastSeen(LocalDateTime.now().minusMinutes(2));
            m1.setStatus("ONLINE");

            Machine m2 = new Machine("Pac-Man Classic", "MACHINE_002", 5.00);
            m2.setLocation("Zone A");
            m2.setPlayCount(289L);
            m2.setLastSeen(LocalDateTime.now().minusMinutes(1));
            m2.setStatus("ONLINE");

            Machine m3 = new Machine("Tekken 7", "MACHINE_003", 15.00);
            m3.setLocation("Zone B");
            m3.setPlayCount(98L);
            m3.setLastSeen(LocalDateTime.now().minusHours(1));
            m3.setStatus("OFFLINE");

            Machine m4 = new Machine("Dance Dance Revolution", "MACHINE_004", 20.00);
            m4.setLocation("Zone B");
            m4.setPlayCount(56L);
            m4.setLastSeen(LocalDateTime.now().minusMinutes(4));
            m4.setStatus("ONLINE");

            Machine m5 = new Machine("Mortal Kombat 11", "MACHINE_005", 12.00);
            m5.setLocation("Zone C");
            m5.setPlayCount(0L);
            m5.setStatus("OFFLINE");

            machineRepository.save(m1);
            machineRepository.save(m2);
            machineRepository.save(m3);
            machineRepository.save(m4);
            machineRepository.save(m5);

            System.out.println("✅ Sample machines loaded into database.");
        }
    }
}
