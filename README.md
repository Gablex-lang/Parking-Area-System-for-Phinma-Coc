# PHINMA COC Parking System

## Description

The PHINMA COC Parking System is a desktop parking management system for PHINMA Cagayan de Oro College, built in pure Java with Java Swing in Apache NetBeans IDE.

It helps a parking operator record who parks on campus, when a vehicle enters and leaves, which slot it uses, and how many parking spaces are still available. The operator can search a driver by ID number, save driver and vehicle details, log Park In and Park Out, view the parking area map, and generate a transaction report.

## Features

### Login and user roles

- Login with username and password, with a Register option for new users (`Login.java`, `AdminReg.java`).
- After login, a loading screen appears, then the system opens a different main screen by role:
  - **Admin** — username starting with `admin` opens `Parking.java` (full controls).
  - **User / Operator** — any other username opens `Parking1.java` (parking operations only).
- Admin main screen has Register, Update, Archives, Archive History, Reports, Refresh, Clear, and User Mode. The user screen has Search, Map, Clear, Refresh, Park In, Park Out, and Logout.

### Parking operations

- Search a driver by ID number and auto-fill driver and vehicle details.
- Register a new driver and vehicle, and update existing driver and vehicle records.
- Park In and Park Out with date and time pickers and a parking slot number.
- Live table of parked cars and motorcycles: ID number, surname, plate number, vehicle type, parking in, and parking out, sorted by latest parking in.
- Capacity monitoring, loaded from the `settings` table:
  - Cars: max 20 slots
  - Motorcycles: max 20 slots
  - Shows parked and available slots for each type, plus total vehicles parked.
- Capacity limits can be changed and saved from the main screen; the values are stored in the `settings` table (`maxCarSlots`, `maxMcSlots`).

### Map, reports, and archives

- **Parking Area Map** (`Map.java`) — visual layout of the PHINMA COC parking area for cars and motorcycles, with a Back button.
- **Parking Transaction Report** (`ReportFrame.java`) — search transactions by a From and To date range, then Print or Export.
  - Export saves the report table as a `.csv` file using a file chooser.
- **Archive** (`Archive.java`) — archived parking records can be viewed and restored. Restoring puts the driver, vehicle, and transaction records back from the `archive` table.

## Screens

1. **Login** — username and password login.
2. **Loading** — progress bar, then opens the admin or user main screen.
3. **Main Parking Screen (Admin / User)** — driver and vehicle details, Park In / Park Out, parked vehicles table, and capacity summary.
4. **Parking Area Map** — visual layout of car and motorcycle slots.
5. **Parking Transaction Report** — transactions in a selected date range, with Print and Export.
6. **Archive** — archived records, with Restore.

## Technologies Used

- Java 17 — pure Java, no web framework
- Java Swing — graphical user interface, Nimbus look and feel
- Apache NetBeans IDE 28 — project type: Java with Ant (`PHINMA COC Parking System`)
- MySQL / MariaDB — database `parkingdb`, via JDBC
- MySQL Connector/J 9.3.0 — `mysql-connector-j-9.3.0.jar`
- AbsoluteLayout — NetBeans layout library
- LGoodDatePicker 11.2.1 — date and date-time pickers
- JCalendar 1.4 — referenced in the project libraries

## Database

Database file: `parkingdb.sql` (phpMyAdmin export, MariaDB 10.4.32, generated 23 October 2025)

Connection used in the code:

- URL: `jdbc:mysql://localhost:3306/parkingdb`
- Driver: `com.mysql.cj.jdbc.Driver`
- Username: `root`
- Password: (blank)

Tables (6):

| Table | Purpose |
|---|---|
| `users` | Login accounts: `id`, `username` (unique), `password` |
| `driver` | Driver details: `driver_id`, `id_number` (unique), first / middle / last name, `affiliation`, `contact`, `address` |
| `vehicle` | Vehicle details: `vehicle_id`, `driver_id`, `plate_number` (unique), `vehicle_brand`, `vehicle_type`, `color` |
| `parkingtransaction` | Parking records: `transaction_id`, `vehicle_id`, `slot_number`, `parking_in`, `parking_out`, `created_at` |
| `settings` | Capacity settings: `maxCarSlots = 20`, `maxMcSlots = 20` |
| `archive` | Archived driver / vehicle / transaction records, with `slot_number`, `parking_in`, `parking_out`, and `deleted_at` |

Relationships: `vehicle.driver_id` → `driver.driver_id`, `parkingtransaction.vehicle_id` → `vehicle.vehicle_id`, and `archive` links to `driver`, `vehicle`, and `parkingtransaction`. The dump includes sample data: 1 driver, 1 vehicle, 2 parking transactions, and 3 user accounts.

Note: passwords in the `users` table are stored as plain text in this version. Change the sample accounts before a real deployment.

## Project Structure

From `PHINMA_COC_PARKING_SYS_V12wDDB.zip`:

```text
PHINMA PARKING SYS V12/
├── parkingdb.sql
├── mysql-connector-j-9.3.0.jar
├── LGoodDatePicker-11.2.1-executable-demo.jar
├── AbsoluteLayout.jar
└── PHINMA PARKING SYS V12/
    ├── src/
    │   ├── Loading.java        # Loading screen, main class in project.properties
    │   ├── Login.java          # Login screen, real entry point
    │   ├── AdminReg.java       # User registration
    │   ├── Parking.java        # Admin main screen (2,008 lines)
    │   ├── Parking1.java       # User main screen (1,613 lines)
    │   ├── Map.java            # Parking area map
    │   ├── ReportFrame.java    # Transaction report, print / CSV export
    │   ├── Archive.java        # Archived records, restore
    │   ├── NewParking.java     # Parking form
    │   ├── sumakses.java       # Unused blank form
    │   └── *.png / *.gif       # Icons and images used by the forms
    ├── nbproject/
    ├── build/
    └── test/
```

All Java files are in the default package (no `package` line). The built JAR name is `PHINMA_COC_Parking_System.jar`.

## How to Run

### 1. Set up the database

1. Start XAMPP, then start Apache and MySQL.
2. Open phpMyAdmin at `http://localhost/phpmyadmin`.
3. Create a database named `parkingdb`, or import directly.
4. Import `parkingdb.sql`. This creates the 6 tables above with sample data.

### 2. Open the project in NetBeans

1. Open Apache NetBeans IDE 28.
2. Go to **File > Open Project**.
3. Select the `PHINMA PARKING SYS V12` project folder.
4. If NetBeans reports missing libraries, re-add them from the project folder:
   `mysql-connector-j-9.3.0.jar`, `AbsoluteLayout.jar`, `LGoodDatePicker-11.2.1-executable-demo.jar`, and `jcalendar-1.4.jar`.
   The saved library paths in `nbproject/project.properties` point to the original computer (`C:\Users\User\Downloads\...`), so they need to be fixed on a different computer.

### 3. Fix the main class (important)

The project is set to run `Loading`, but `Loading.main()` is empty, so pressing Run only shows `BUILD SUCCESSFUL` and no window opens.

Set the main class to `Login`:

1. Right-click the project > **Properties > Run**.
2. Set **Main Class** to `Login`.
3. Click OK, then press **F6** or click the green **Run** button.

To run a single file instead, right-click `Login.java` and choose **Run File** (Shift+F6).

### 4. Log in

Log in with an account from the `users` table in `parkingdb`. A username starting with `admin` opens the admin screen; any other username opens the user screen.

## How to Use

1. Log in as admin or user.
2. Enter the driver's ID number and click **SEARCH**.
3. Check or complete the driver details (name, affiliation, contact, address) and vehicle details (plate number, brand, type, color).
4. Enter the parking slot number and the parking in / out date and time.
5. Click **PARK IN** when the vehicle enters, and **PARK OUT** when it leaves.
6. Click **MAP** to view the parking area layout.
7. Admin: click **REPORTS** to search transactions by date, then **PRINT** or **EXPORT** (CSV). Click **ARCHIVES** to view or restore archived records.
8. Click **LOGOUT** when done.

## Files in This Zip

- `PHINMA_COC_PARKING_SYS_V12wDDB.zip` (7.1 MB) — full NetBeans project, including `src`, `build`, `nbproject`, the 3 library JARs above, and `parkingdb.sql`.
