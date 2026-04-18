package com.arcade.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArcadeDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArcadeDashboardApplication.class, args);
        System.out.println("\n====================================");
        System.out.println("  🕹️  Arcade Dashboard is RUNNING!");
        System.out.println("  🌐  http://localhost:8080");
        System.out.println("  🗄️  DB Console: http://localhost:8080/h2-console");
        System.out.println("====================================\n");
    }
}
