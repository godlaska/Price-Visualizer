
# 📊 Price Visualizer

A Java-based data visualization tool for analyzing historical and forecasted food price trends using CPI and PPI data. This application connects to a MySQL database and provides interactive charts and tables to support economic insights.

---

## 🔧 Installation Instructions (Windows)

### 1. Install MySQL Workbench and MySQL Server

Before you can run the Price Visualizer application, you must install a Database Management Software (DBMS). We will be using **MySQL**, which requires two components:

- **MySQL Workbench** – A GUI to interface with the database  
- **MySQL Server** – The actual database server running locally on your machine

#### 🔹 Step 1: Install MySQL Workbench

Download the MySQL Workbench for Windows from the following link:

👉 [MySQL Workbench Download](https://dev.mysql.com/downloads/workbench/)

- Choose your operating system (Windows) from the drop-down menu.
- Follow the installation instructions provided.
- This GUI tool will allow you to run queries, visualize schema, and manage your database easily.

#### 🔹 Step 2: Install MySQL Server (Local)

Download the MySQL Server using the MySQL Installer:

👉 [MySQL Installer for Windows](https://dev.mysql.com/downloads/windows/installer/8.0.html)

- Select the "Developer Default" option during installation to include the necessary tools.
- When prompted, choose **a simple password** for the root user (e.g., `pass`).
  - ⚠️ **Important:** Don’t forget this password. You’ll need it to connect the Java application to the database.
  - This server will run **locally** and is **not internet-accessible**, so security complexity is not necessary.

#### 📺 Video Walkthrough

For a complete visual guide of this step, follow this video tutorial created by John Mattox:  
👉 [Installation Video](https://youtu.be/23SySnsKln0)

---

## 🗃️ Importing Data into MySQL

Once MySQL Workbench and Server are installed, follow these steps to set up your database schema and import the project data.

### 1. Create the Schema

1. Open **MySQL Workbench** and start the **local MySQL server**.
2. Go to `File > New Query Tab`.
3. In the editor, paste the following SQL command:
   ```sql
   CREATE SCHEMA foodprices;
   ```
4. Click the ⚡ (lightning bolt) icon to run the command.
5. Click the gray refresh arrow next to Schemas.
6. You should now see a new schema named **foodprices**.
7. Right click the schema and click 'Set as Default Schema'
 
<img src="https://github.com/user-attachments/assets/4956aee3-a4a3-4275-ab4c-4131d3092207" width="600"/>
<img src="https://github.com/user-attachments/assets/01ef6e3c-35f8-4ae5-ac42-8c795e1d62f7" width="400"/>

---

### 2. Run the Table Setup Script

1. Download the repository as a .zip file. Extract the folder in any place. This tutorial will assume it's in the Downloads folder.
2. Navigate to the following folder in your file explorer:
   ```
   FinalProject/sql/automaticSetup.sql
   ```
3. Open `automaticSetup.sql` and copy all the `CREATE TABLE` statements inside.
4. Go back to your MySQL Workbench window and make sure the `foodprices` schema is selected.
5. Paste the copied SQL commands into the query editor at the top.
6. Highlight all the `CREATE TABLE` commands and click the ⚡ (lightning bolt) icon to run them.

<img src="https://github.com/user-attachments/assets/d23f2ec8-787c-4372-9f3c-fb3b51adfbf4" width="1200"/>

<p>Hit the refresh arrow in schemas and your tables should look like this:</p>

<img src="https://github.com/user-attachments/assets/8772d148-d1ce-4ce9-9ab4-aed4cbbb8d06" width="400"/>

---

### 3. Load CSV Data into Tables

1. Open **Command Prompt**.
2. Use `cd` to navigate to the MySQL binary directory:
   - You can find it by launching MySQL, opening **Task Manager**, right-clicking on MySQL Workbench, and selecting **Open file location**.
   - Example path:
     ```
     cd "C:\Program Files\MySQL\MySQL Workbench 8.0 CE"
     ```

3. Launch the MySQL CLI with file loading enabled:
   ```
   mysql --local_infile=1 -u root -p
   ```
4. Enter your password (e.g., `pass`).
5. Select your schema:
   ```sql
   USE foodprices;
   ```

6. To load the data, run the following:
   ```sql
   SOURCE C:/Users/user/Downloads/FinalProject/sql/load_all_data.sql;
   ```
   - Make sure to **adjust the path** if your SQL file is in a different location.
   - 💡 Tip: once you navigate to the load_all_data.sql file, right click and click "Copy as Path". You can use this in front of SOURCE.
   - This script contains all `LOAD DATA LOCAL INFILE` commands to populate your tables from CSV files.

📸 *(Insert screenshot of terminal commands and successful import here)*

---
