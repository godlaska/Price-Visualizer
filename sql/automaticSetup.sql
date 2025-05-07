-- START OF tableCreation.sql --
# ----------------- CREATE TABLES FROM CSV -----------------
CREATE TABLE ppiforecast (
    `Producer Price Index item` VARCHAR(255),
    `Attribute` VARCHAR(255),
    `Unit` VARCHAR(255),
    `Value` DECIMAL(10, 2)
);

CREATE TABLE cpiforecast (
    `Top-level` VARCHAR(255),
    `Aggregate` VARCHAR(255),
    `Mid-level` VARCHAR(255),
    `Low-level` VARCHAR(255),
    `Disaggregate` VARCHAR(255),
    `Attribute` VARCHAR(255),
    `Unit` VARCHAR(255),
    `Value` DECIMAL(10, 2)
);

CREATE TABLE historicalppi (
    `Producer Price Index item` VARCHAR(255),
    `Year` INT,
    `Percent change` DECIMAL(10, 2)
);

CREATE TABLE historicalcpi (
    `Consumer Price Index item` VARCHAR(255),
    `Year` INT,
    `Percent change` DECIMAL(10, 2)
);

CREATE TABLE ppiforecastarchived (
    `Producer Price Index item` VARCHAR(255),
    `Month of forecast` VARCHAR(255),
    `Year of forecast` INT,
    `Year being forecast` INT,
    `Attribute` VARCHAR(255),
    `Forecast percent change` DECIMAL(10, 2)
);

CREATE TABLE cpiforecastarchived (
    `Consumer Price Index item` VARCHAR(255),
    `Month of forecast` VARCHAR(255),
    `Year of forecast` INT,
    `Year being forecast` INT,
    `Attribute` VARCHAR(255),
    `Forecast percent change` DECIMAL(10, 2)
);

CREATE TABLE ppihistoricalforecast (
    `Producer Price Index item` VARCHAR(255),
    `Month of forecast` VARCHAR(255),
    `Year of forecast` INT,
    `Year being forecast` INT,
    `Attribute` VARCHAR(255),
    `Forecast percent change` DECIMAL(10, 2)
);

CREATE TABLE cpihistoricalforecast (
    `Consumer Price Index item` VARCHAR(255),
    `Month of forecast` VARCHAR(255),
    `Year of forecast` INT,
    `Year being forecast` INT,
    `Attribute` VARCHAR(255),
    `Forecast percent change` DECIMAL(10, 2)
);


-- END OF tableCreation.sql --

# !! Make sure to import the CSV data before going to the next step!

-- START OF IDCreation.sql --
# ----------------- ADD INCREMENTING ID (PRIMARY KEY) -----------------

# The purpose of these auto incremented keys is to add a primary key
# to all the tables as they don't explicitly have one. More details
# can be found in the documentation.

ALTER TABLE cpiforecast
ADD COLUMN cpiforecast_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE cpiforecastarchived
ADD COLUMN cpiforecastarchived_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE cpihistoricalforecast
ADD COLUMN cpihistoricalforecast_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE historicalcpi
ADD COLUMN historicalcpi_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE historicalppi
ADD COLUMN historicalppi_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE ppiforecast
ADD COLUMN ppiforecast_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE ppiforecastarchived
ADD COLUMN ppiforecastarchived_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE ppihistoricalforecast
ADD COLUMN ppihistoricalforecast_id INT AUTO_INCREMENT PRIMARY KEY FIRST;


-- END OF IDCreation.sql --

-- START OF columnFormatting.sql --
# ----------------- FORMATS THE ORIGINAL COLUMN HEADERS -----------------
# PPI Forecast
ALTER TABLE PPIForecast CHANGE `Producer Price Index item` `producerPriceIndexItem` VARCHAR(255);
ALTER TABLE PPIForecast CHANGE `Attribute` `attribute` VARCHAR(255);
ALTER TABLE PPIForecast CHANGE `Unit` `unit` VARCHAR(255);
ALTER TABLE PPIForecast CHANGE `Value` `value` VARCHAR(255);

# CPI Forecast
ALTER TABLE CPIForecast CHANGE `Top-level` `topLevel` VARCHAR(255);
ALTER TABLE CPIForecast CHANGE `Aggregate` `aggregate` VARCHAR(255);
ALTER TABLE CPIForecast CHANGE `Mid-level` `midLevel` VARCHAR(255);
ALTER TABLE CPIForecast CHANGE `Low-level` `lowLevel` VARCHAR(255);
ALTER TABLE CPIForecast CHANGE `Disaggregate` `disaggregate` VARCHAR(255);
ALTER TABLE CPIForecast CHANGE `Attribute` `attribute` VARCHAR(255);
ALTER TABLE CPIForecast CHANGE `Unit` `unit` VARCHAR(255);
ALTER TABLE CPIForecast CHANGE `Value` `value` VARCHAR(255);

# Historical PPI
ALTER TABLE historicalppi CHANGE `Producer Price Index item` `producerPriceIndexItem` VARCHAR(255);
ALTER TABLE historicalppi CHANGE `Year` `year` VARCHAR(255);
ALTER TABLE historicalppi CHANGE `Percent change` `percentChange` VARCHAR(255);

# Historical CPI
ALTER TABLE historicalcpi CHANGE `Consumer Price Index item` `consumerPriceIndexItem` VARCHAR(255);
ALTER TABLE historicalcpi CHANGE `Year` `year` VARCHAR(255);
ALTER TABLE historicalcpi CHANGE `Percent change` `percentChange` VARCHAR(255);

# CPI Forecast Archived
ALTER TABLE cpiforecastarchived CHANGE `Consumer Price Index item` `consumerPriceIndexItem` VARCHAR(255);
ALTER TABLE cpiforecastarchived CHANGE `Month of forecast` `monthOfForecast` VARCHAR(255);
ALTER TABLE cpiforecastarchived CHANGE `Year of forecast` `yearOfForecast` VARCHAR(255);
ALTER TABLE cpiforecastarchived CHANGE `Year being forecast` `yearBeingForecast` VARCHAR(255);
ALTER TABLE cpiforecastarchived CHANGE `Attribute` `attribute` VARCHAR(255);
ALTER TABLE cpiforecastarchived CHANGE `Forecast percent change` `forecastPercentChange` VARCHAR(255);

# PPI Forecast Archived
ALTER TABLE ppiforecastarchived CHANGE `Producer Price Index item` `producerPriceIndexItem` VARCHAR(255);
ALTER TABLE ppiforecastarchived CHANGE `Month of forecast` `monthOfForecast` VARCHAR(255);
ALTER TABLE ppiforecastarchived CHANGE `Year of forecast` `yearOfForecast` VARCHAR(255);
ALTER TABLE ppiforecastarchived CHANGE `Year being forecast` `yearBeingForecast` VARCHAR(255);
ALTER TABLE ppiforecastarchived CHANGE `Attribute` `attribute` VARCHAR(255);
ALTER TABLE ppiforecastarchived CHANGE `Forecast percent change` `forecastPercentChange` VARCHAR(255);

# PPI Historical Forecast
ALTER TABLE ppihistoricalforecast CHANGE `Producer Price Index item` `producerPriceIndexItem` VARCHAR(255);
ALTER TABLE ppihistoricalforecast CHANGE `Month of forecast` `monthOfForecast` VARCHAR(255);
ALTER TABLE ppihistoricalforecast CHANGE `Year of forecast` `yearOfForecast` VARCHAR(255);
ALTER TABLE ppihistoricalforecast CHANGE `Year being forecast` `yearBeingForecast` VARCHAR(255);
ALTER TABLE ppihistoricalforecast CHANGE `Attribute` `attribute` VARCHAR(255);
ALTER TABLE ppihistoricalforecast CHANGE `Forecast percent change` `forecastPercentChange` VARCHAR(255);

# CPI Historical Forecast
ALTER TABLE CPIHistoricalForecast CHANGE `Consumer Price Index item` `consumerPriceIndexItem` VARCHAR(255);
ALTER TABLE CPIHistoricalForecast CHANGE `Month of forecast` `monthOfForecast` VARCHAR(255);
ALTER TABLE CPIHistoricalForecast CHANGE `Year of forecast` `yearOfForecast` VARCHAR(255);
ALTER TABLE CPIHistoricalForecast CHANGE `Year being forecast` `yearBeingForecast` VARCHAR(255);
ALTER TABLE CPIHistoricalForecast CHANGE `Attribute` `attribute` VARCHAR(255);
ALTER TABLE CPIHistoricalForecast CHANGE `Forecast percent change` `forecastPercentChange` VARCHAR(255);

-- END OF columnFormatting.sql --

-- START OF historyTableCreation.sql --
CREATE TABLE query_history (
  id INT AUTO_INCREMENT PRIMARY KEY,
  query_text TEXT NOT NULL,
  run_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- END OF historyTableCreation.sql --