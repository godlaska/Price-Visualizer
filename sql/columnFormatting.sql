# Purpose: Applies final formatting and type casting to selected columns across tables.
# Used after raw data import to ensure consistent column types and values for queries.

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
