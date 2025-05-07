-- Bulk data import section for CPI and PPI datasets.
-- These commands load all relevant CSV files into their corresponding MySQL tables.
-- Each file is located in the user's Downloads folder under FinalProject/dataset.
-- This step is essential to initialize the database with both current and archived
-- forecast data, as well as historical actuals, for Consumer Price Index (CPI)
-- and Producer Price Index (PPI). These tables are used for the application's forecasting,
-- accuracy analysis, and data visualization features.

-- Load PPIForecast
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\PPIForecast.csv'
INTO TABLE PPIForecast
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

-- Load CPIForecast
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\CPIForecast.csv'
INTO TABLE CPIForecast
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

-- Load HistoricalPPI
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\historicalppi.csv'
INTO TABLE HistoricalPPI
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

-- Load HistoricalCPI
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\historicalcpi.csv'
INTO TABLE HistoricalCPI
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

-- Load PPIForecastArchived
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\PPIForecast_Archived.csv'
INTO TABLE PPIForecastArchived
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

-- Load CPIForecastArchived
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\CPIForecast_Archived.csv'
INTO TABLE CPIForecastArchived
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

-- Load PPIHistoricalForecast
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\PPIHistoricalForecast.csv'
INTO TABLE PPIHistoricalForecast
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

-- Load CPIHistoricalForecast
LOAD DATA LOCAL INFILE 'C:\\Users\\user\\Downloads\\FinalProject\\dataset\\CPIHistoricalForecast.csv'
INTO TABLE CPIHistoricalForecast
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;
