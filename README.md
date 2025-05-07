
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
   Price-Visualizer-master/Price-Visualizer-master/sql/automaticSetup.sql
   ```
3. Open `automaticSetup.sql` and copy all the `CREATE TABLE` statements inside.
4. Go back to your MySQL Workbench window (this should you have CREATE SCHEMA command at the top) and make sure the `foodprices` schema is bolded.
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
5. Enable file loading
   ```
   set global local_infile=true;
   ```
7. Select your schema:
   ```sql
   USE foodprices;
   ```

8. Go back to the file explorer and double click **load_all_data.sql**. This should open mySQLWorkbench.

9. You'll notice each table has "LOAD DATA LOCAL INFILE" then a path to a .csv. Make sure you change the path for each LOAD DATA query. All the data tables can be found in the /dataset folder.
  - 💡Tip: Right click on each CSV and click 'Copy as Path'. Paste that into the SQL file. From there, you can use that path for the rest of the CSVs. **Make sure any backslashes (\\) are changed to forwardslashes (/)!**
  - If your file path is similar to mine, feel free to use this template...
  ```
  'C:/Users/[YOUR USERNAME]/Downloads/Price-Visualizer-master/Price-Visualizer-master/dataset/[datatable-name].csv'
  ```
  - Once all of the paths are correct, press Ctrl+S to save. This should update the load_all_data.sql file in the folder. Yours should look similar to the image below...
<img src="https://github.com/user-attachments/assets/afeda17c-06f7-467f-861d-0e9c40e540c2" width="1000"/>

  
10. To load the data, run the following in command prompt:
   ```sql
   SOURCE C:\Users\[YOUR USERNAME]\Downloads\Price-Visualizer-master\Price-Visualizer-master\sql\load_all_data.sql
   ```
   - Make sure to **adjust the path** if your SQL file is in a different location.
   - 💡 Tip: once you navigate to the load_all_data.sql file, right click and click "Copy as Path". You can use this in front of SOURCE.
   - This script contains all `LOAD DATA LOCAL INFILE` commands to populate your tables from CSV files.
   - If everything is correct, you should see 8 lines that say 'QUERY OK'.

<img src="https://github.com/user-attachments/assets/bf36fa88-f574-469d-afe6-fa71f3081395" width="1000"/>

You can check if this worked by going back to your local instance of MySQL and clicking refresh, hovering over a table, and clicking the little lightning bolt in the window.

<img src="https://github.com/user-attachments/assets/ed5901d2-95a6-4bfd-a02f-ca63b1eb721c" width="1000"/>

There should be data shown in the table. Now we're ready to continue with the rest of the setup!

---

### 4. Add Primary Keys and ID Columns

1. Go back to the file explorer and open the `automaticSetup.sql` file again.
2. Scroll down until you see the comment:
   ```sql
   -- START OF IDCreation.sql --
   ```
3. Copy **everything from that line to the end of the file**.
4. Return to your SQL tab in MySQL Workbench — the same one where you created the schema and tables.
5. Paste the copied SQL **below** your existing `CREATE TABLE` statements.
6. Highlight all the newly pasted lines and click the ⚡ (lightning bolt) icon to execute them.
   
<img src="https://github.com/user-attachments/assets/41a67342-9bee-454e-9984-d444bfcbdfa5" width="1000">

📌 This will:
- Add primary keys to each table  
- Rename columns to be more SQL-friendly  
- Create a query history table for tracking your SQL execution

### 🧩 Design Rationale: Adding Auto-Incrementing IDs

### Why We Added Auto-Incrementing IDs to Each Table

None of the original datasets contained natural primary keys-- the values in columns like `Aggregate`, `Disaggregate`, or `Attribute` were often repeated, nullable, or not guaranteed to be unique. This made it impossible to uniquely identify rows, which is essential for reliable data manipulation, joining, and referencing.

To resolve this, we added an `AUTO_INCREMENT` primary key (e.g., `cpiforecast_id`, `ppiforecast_id`, etc.) to every table. This had several purposes specific to this project:

- ✅ **Normalization**: Assigning a unique ID to each row ensures that we follow best practices in relational database design. It also allows for future decomposition into related tables without relying on ambiguous composite keys.
- 🔗 **Reliable joins**: Many of our queries (as seen in `tableQueries.sql`) involve filtering or aggregating rows across multiple datasets. Having a unique ID allows for clean and efficient joins or references between tables.
- 📊 **Query tracking and logging**: The `query_history` table we created references specific queries. Having IDs on each source table supports tracking which rows were accessed or transformed during each logged query.
- 🛠️ **Data safety**: With auto-incrementing IDs, even if multiple rows contain identical data (e.g., two rows with the same item and forecast value), we can still identify and manipulate each row independently without conflict.

In short, adding `AUTO_INCREMENT` IDs was a structural improvement that enabled normalization, future-proofed our design, and supported the advanced querying work seen in this project.
