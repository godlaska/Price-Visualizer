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

