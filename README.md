
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

---

### ☕ 5. Install Java 17 (Required for the GUI)

The GUI for Price Visualizer is written in Java and requires **Java 17** to be installed on your system.

#### 🔹 Step-by-Step: Install Java 17 on Windows

1. Go to the official Temurin Java download page:  
   👉 [https://adoptium.net/en-GB/temurin/releases/?version=17](https://adoptium.net/en-GB/temurin/releases/?version=17)

2. Under **Temurin 17**, select:
   - Operating System: `Windows`
   - Architecture: `x64` (most systems)
   - Package Type: `MSI Installer`

3. Click **Download** and run the installer.

4. During installation:
   - ✔️ Make sure **"Set JAVA_HOME environment variable"** is checked.
   - ✔️ Ensure the **PATH variable** is updated so you can run `java` from the command line.

5. To confirm the installation worked:
   - Open **Command Prompt**
   - Type:
     ```bash
     java -version
     ```
   - You should see output that starts with something like:
     ```
     openjdk version "17.0.x"
     ```

📌 Once Java is installed, you’ll be able to launch the GUI using the included `.bat` file (instructions to follow next).

---

### 🚀 6. Launch the GUI Application

Once Java 17 is installed and your database is fully set up, you can launch the Price Visualizer application using the provided batch file.

#### 🔹 How to Run the GUI

1. Open **File Explorer** and navigate to the project directory:
   ```
   Price-Visualizer-master/Price-Visualizer-master/
   ```

2. Look for a file named:
   ```
   runVisualizer.bat
   ```

3. Double-click the `.bat` file to run the application.

💡 If the terminal flashes and disappears, try running it via Command Prompt:

- Open **Command Prompt**
- Navigate to the folder:
  ```cmd
  cd C:\Users\[YOUR USERNAME]\Downloads\Price-Visualizer-master\Price-Visualizer-master
  ```
- Then run:
  ```cmd
  runVisualizer.bat
  ```

#### ✅ What Should Happen

- The Java application will launch with a GUI window.
- You should see a dropdown menu, query options, and a table/chart panel.
- If you get a database connection error, double-check that:
  - Your MySQL server is **running**
  - The username is `root` and password is `pass` (or whatever you set). This can be found if you open PriceDataVisualizer.java (src/main/java/org/FinalProject/PriceDataVisualizer.java) and look iin setupConnectionPool();
  - <img src="https://github.com/user-attachments/assets/aaec1686-0eed-4da1-95a7-455f82096500" width="600">
  - The schema `foodprices` exists and contains the expected tables

If everything worked correctly, this should be the window that pops up...
<img src="https://github.com/user-attachments/assets/36113090-9869-48b3-854c-d0be84339518" width="1000">

---

### 📚 References

This project utilizes the following open-source libraries:

- **HikariCP**
  - Purpose: Efficient connection pooling for MySQL database access.
  - Source: [https://github.com/brettwooldridge/HikariCP](https://github.com/brettwooldridge/HikariCP)

- **JFreeChart**
  - Purpose: Creating charts and visualizations including time series and bar charts.
  - Source: [https://www.jfree.org/jfreechart/](https://www.jfree.org/jfreechart/)

- **MySQL Connector/J**
  - Purpose: JDBC driver for connecting Java applications to MySQL databases.
  - Source: [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)

- **Java Swing**
  - Purpose: GUI framework for building the user interface of the application.
  - Source: Part of the standard Java Development Kit (JDK)
 
- **Food Price Outlook Dataset**
  - **Purpose**: Core dataset used for all querying, analysis, and visualization within the application.
  - **Source**: [USDA Economic Research Service – Food Price Outlook](https://www.ers.usda.gov/data-products/food-price-outlook)

All external libraries are either bundled with the project or should be included via Maven or manual JAR management.

---

### 🧠 Application Features & How to Use Them

The Price Visualizer application includes a variety of powerful features that help you interact with and analyze food price data effectively. Below is a complete list of every core feature and how to use it:

---

#### 📊 1. Full Data Viewer
- **What it does**: Displays raw data from any CPI or PPI table in tabular and chart form.
- **How to use**:
  1. Select **"Show Full Data"** from the Query dropdown.
  2. Choose either **Consumer Price Index** or **Producer Price Index**.
  3. Use the **Table dropdown** to select a dataset (e.g., `CPIForecast`, `historicalppi`, `cpiforecastarchived`, etc.).
  4. The table appears below, and if applicable, a time series chart is generated.
  5. Use checkboxes on the left to filter which items appear in the chart.
  6. Use **Select All / Deselect All / Apply Filter** buttons to manage checkboxes.

---

#### 📈 2. Forecast Bounds (Midpoint, Lower, Upper)
- **What it does**: Visualizes 2025 CPI/PPI forecast bounds for each item.
- **How to use**:
  1. Choose **"Show Full Data"** and select `CPIForecast` or `PPIForecast`.
  2. The bar chart will automatically load forecast bounds (Lower & Upper) for 2025.
  3. Use the left-hand checkboxes to include/exclude specific categories.

---

#### 📉 3. Volatility Analysis
- **What it does**: Measures volatility using average annual percent change for each category through the use of embedded SQL.
- **How to use**:
  1. Select **"Volatility"** from the Query dropdown.
  2. Choose CPI or PPI.
  3. Set the **Year Range** using the two spinners.
  4. Click **Run Query** to generate the volatility chart and table.
  5. Use checkboxes to filter items from the chart and table.
  6. Click **Apply Filter** to refresh the view based on your selections.

---

#### 🎯 4. Forecast Accuracy Evaluation
- **What it does**: Compares forecasted vs. actual values using mean absolute error.
- **How to use**:
  1. Select **"Forecast Accuracy"** from the Query dropdown.
  2. Choose CPI or PPI.
  3. Click **Run Query**.
  4. A line chart and table will show the average forecast error per category and year.
  5. Use checkboxes to explore trends for specific items.

---

#### 🔬 5. Methodology Comparison (Old vs. New)
- **What it does**: Compares archived vs. updated forecast models for CPI and PPI.
- **How to use**:
  1. Select **"Methodology Comparison"**.
  2. Choose CPI or PPI.
  3. Click **Run Query**.
  4. A time series chart will show both forecasts (old and new) per item.
  5. Use the left-hand checkboxes to control which categories are shown.

---

#### 🕰️ 6. Query History Panel
- **What it does**: Logs every query you've executed.
- **How to use**:
  1. View the **History panel** on the right side of the window.
  2. Click on any previous query to re-run it and display the result.
  3. Click **Clear History** to remove all logs.

---

#### 🗂️ 7. Data Filtering with Checkboxes
- **What it does**: Provides dynamic control over which data series appear in charts.
- **How to use**:
  1. Use checkboxes on the left-hand panel to select or deselect categories.
  2. Click **Apply Filter** to update both the chart and the table.
  3. Use **Select All** or **Deselect All** to quickly manage selections.

---

#### 📄 8. Table Viewer with Scroll and Export-Friendly Layout
- **What it does**: Displays query results in a scrollable, export-friendly format.
- **How to use**:
  - Scroll through rows in the lower section of the application window.
  - You can copy-paste or export this data manually for further use.

---

This feature set was designed to make food price data more transparent, explorable, and insightful for analysis.

### Video Demonstration
Video Demonstration WIP
