# 🕹️ ArcadeOS Dashboard — Complete Setup Guide

## What This Is
A Spring Boot web dashboard to manage RFID-based arcade machines. Tracks plays, calculates revenue, shows charts, and detects offline machines.

---

## ✅ PREREQUISITES (Install These First)

### 1. Java 17 or higher
- Download: https://adoptium.net/
- After install, verify: `java -version` → should show `17.x.x`

### 2. IntelliJ IDEA
- Download Community (free) or Ultimate: https://www.jetbrains.com/idea/download/
- Recommended: IntelliJ IDEA Community Edition (free)

### 3. Maven (usually bundled with IntelliJ — no separate install needed)

---

## 🚀 STEP-BY-STEP SETUP IN INTELLIJ

### STEP 1 — Open the Project
1. Open IntelliJ IDEA
2. Click **"Open"** (or File → Open)
3. Navigate to and select the `arcade-dashboard` folder
4. Click **OK / Open**
5. IntelliJ will detect it as a Maven project — click **"Load Maven Project"** if prompted

### STEP 2 — Wait for Dependencies to Download
- IntelliJ will start downloading Maven dependencies automatically
- Watch the progress bar at the bottom right
- This may take 1–3 minutes on first run (needs internet)

### STEP 3 — Verify Project Structure
Your project should look like this in IntelliJ:
```
arcade-dashboard/
├── pom.xml
└── src/
    └── main/
        ├── java/com/arcade/dashboard/
        │   ├── ArcadeDashboardApplication.java  ← Main class
        │   ├── controller/
        │   │   ├── ApiController.java
        │   │   └── DashboardController.java
        │   ├── model/
        │   │   ├── Machine.java
        │   │   └── PlayEvent.java
        │   ├── repository/
        │   │   ├── MachineRepository.java
        │   │   └── PlayEventRepository.java
        │   ├── service/
        │   │   └── MachineService.java
        │   └── config/
        │       └── DataInitializer.java
        └── resources/
            ├── application.properties
            ├── templates/
            │   ├── layout.html
            │   ├── dashboard.html
            │   ├── machines.html
            │   ├── edit-machine.html
            │   └── analytics.html
            └── static/css/
                └── style.css
```

### STEP 4 — Run the Application
**Option A — Using IntelliJ Run Button (Easiest):**
1. Open `ArcadeDashboardApplication.java`
2. Click the green ▶ Run button next to the `main()` method
3. Or press `Shift + F10`

**Option B — Using Maven Terminal:**
1. Open Terminal in IntelliJ (View → Tool Windows → Terminal)
2. Run: `mvn spring-boot:run`

### STEP 5 — Open the Dashboard
Once you see this in the console:
```
====================================
  🕹️  Arcade Dashboard is RUNNING!
  🌐  http://localhost:8080
  🗄️  DB Console: http://localhost:8080/h2-console
====================================
```
Open your browser and go to: **http://localhost:8080**

---

## 🌐 PAGES & FEATURES

| Page | URL | What it does |
|------|-----|-------------|
| Dashboard | http://localhost:8080/ | Overview, stats, table, chart, activity feed |
| Machines | http://localhost:8080/machines | Add, edit, delete, reset machines |
| Analytics | http://localhost:8080/analytics | Detailed charts and revenue table |
| DB Console | http://localhost:8080/h2-console | Browse the database directly |
| API Health | http://localhost:8080/api/health | Check if API is running |

---

## 📡 REST API ENDPOINTS

### For ESP32 (send play data)
```
GET  /api/play?machineId=MACHINE_001&count=1
GET  /api/play?machineId=MACHINE_001&count=1&rfidTag=A3F5B2

POST /api/play
Body: { "machineId": "MACHINE_001", "count": 1, "rfidTag": "A3F5B2" }
```

### Dashboard data
```
GET /api/stats         → All machine stats + totals
GET /api/chart-data    → Chart labels + sales + play counts
GET /api/events        → Last 20 play events
GET /api/health        → { "status": "UP" }
```

---

## 🧪 TESTING WITHOUT ESP32

### Option A — Browser URL (easiest)
Open your browser and visit:
```
http://localhost:8080/api/play?machineId=MACHINE_001&count=1
```
This simulates 1 play from Street Fighter II.

### Option B — Using curl (Terminal)
```bash
# Simulate 1 play
curl "http://localhost:8080/api/play?machineId=MACHINE_001&count=1"

# Simulate 3 plays at once
curl "http://localhost:8080/api/play?machineId=MACHINE_002&count=3"

# POST with JSON body
curl -X POST http://localhost:8080/api/play \
  -H "Content-Type: application/json" \
  -d '{"machineId":"MACHINE_003","count":2,"rfidTag":"AB12CD"}'
```

### Option C — Postman
1. Install Postman: https://www.postman.com/
2. GET request to: `http://localhost:8080/api/play?machineId=MACHINE_001&count=1`

---

## 🗄️ DATABASE CONSOLE

1. Go to: http://localhost:8080/h2-console
2. Set JDBC URL to: `jdbc:h2:mem:arcadedb`
3. Username: `sa`
4. Password: (leave blank)
5. Click Connect

Useful SQL queries:
```sql
SELECT * FROM MACHINES;
SELECT * FROM PLAY_EVENTS ORDER BY RECEIVED_AT DESC;
SELECT NAME, PLAY_COUNT, PLAY_COUNT * PRICE_PER_PLAY AS REVENUE FROM MACHINES;
```

---

## ⚙️ CONFIGURATION

Edit `src/main/resources/application.properties`:

```properties
server.port=8080                          # Change port if needed
arcade.offline.threshold.minutes=5       # Minutes before machine marked OFFLINE
```

---

## 🔌 CONNECTING ESP32 (Later Step)

In your ESP32 Arduino code, add HTTP GET request:
```cpp
#include <WiFi.h>
#include <HTTPClient.h>

const char* serverUrl = "http://192.168.1.100:8080"; // Your PC's IP

void sendPlayData(String machineId, String rfidTag) {
  HTTPClient http;
  String url = String(serverUrl) + "/api/play?machineId=" + machineId + "&count=1&rfidTag=" + rfidTag;
  http.begin(url);
  int httpCode = http.GET();
  http.end();
}
```
**Important:** Replace `192.168.1.100` with your PC's actual IP address.
Find your IP: Windows → `ipconfig` | Mac/Linux → `ifconfig`

---

## 🛠️ TROUBLESHOOTING

| Problem | Solution |
|---------|----------|
| Port 8080 in use | Change `server.port=9090` in application.properties |
| Maven dependencies not downloading | Check internet, File → Invalidate Caches → Restart |
| `java.version` not 17 | Install Java 17, set in IntelliJ: File → Project Structure → SDK |
| White label error page | Check console for red error text |
| Database error on restart | H2 is in-memory, data resets on restart (this is normal) |

---

## 💾 USING PERSISTENT DATABASE (Optional)

To keep data after restart, change in `application.properties`:
```properties
# Replace in-memory H2 with file-based H2
spring.datasource.url=jdbc:h2:file:./arcadedb;DB_CLOSE_DELAY=-1
```
Data will be saved to `arcadedb.mv.db` in the project folder.

---

## 📦 BUILDING FOR PRODUCTION (JAR File)

```bash
mvn clean package -DskipTests
java -jar target/dashboard-1.0.0.jar
```

---

*ArcadeOS Dashboard — Built with Spring Boot 3.2, Thymeleaf, H2, Bootstrap 5, Chart.js*
