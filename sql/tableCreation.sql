# ----------------- CREATE TABLES FROM CSV -----------------
-- These CREATE TABLE statements define the database schema used in the
-- CPI and PPI Forecast Visualization project. Each table corresponds to a
-- specific dataset related to food price forecasts and historical trends.
-- 
-- - ppiforecast / cpiforecast: Current USDA forecasts for Producer and Consumer Price Indices.
-- - historicalppi / historicalcpi: Actual historical percent changes for each index.
-- - ppiforecastarchived / cpiforecastarchived: Archived forecasts made using the previous USDA methodology.
-- - ppihistoricalforecast / cpihistoricalforecast: Forecasts published using the current USDA methodology.
--
-- These tables support querying, accuracy analysis, and chart generation within the application.

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

