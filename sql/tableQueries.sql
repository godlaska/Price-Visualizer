# ----------------- TABLE QUERIES -----------------
# !! make sure very detailed comments are written on the exact goal and purpose of the join statement
# also make all comments with # and query names with --

# Retrieves CPI forecast bounds (lower and upper) for the year 2025 across all categories.
# Focuses on "Percent change" unit and filters only for prediction interval attributes.
-- full_data_CPIForecast_for_2025_bounds
SELECT cpiforecast_id, aggregate, midLevel, disaggregate, attribute, unit, value
FROM CPIForecast
WHERE unit = 'Percent change'
  AND (
    attribute LIKE 'Lower bound of prediction interval 2025%' OR
    attribute LIKE 'Upper bound of prediction interval 2025%'
  );

# Returns the full CPI historical forecast dataset.
# This includes forecasts from the new methodology used since September 2023.
-- full_data_CPIHistoricalForecast
SELECT * FROM CPIHistoricalForecast;

# Returns actual historical CPI data with real observed percent changes per item per year.
-- full_data_historicalcpi
SELECT * FROM historicalcpi;

# Returns CPI forecast data published under the old methodology (pre-Sept 2023).
-- full_data_cpiforecastarchived
SELECT * FROM cpiforecastarchived;

# Retrieves PPI forecast bounds (lower and upper) for 2025 across all categories.
# Filters for "Percent change" unit and 2025 forecast intervals only.
-- full_data_PPIForecast_for_2025_bounds
SELECT ppiforecast_id, producerPriceIndexItem, attribute, unit, value
FROM PPIForecast
WHERE unit = 'Percent change'
  AND (
    attribute LIKE '%Lower bound of prediction interval 2025%' OR
    attribute LIKE '%Upper bound of prediction interval 2025%'
  );

# Returns the full PPI historical forecast dataset.
-- full_data_PPIHistoricalForecast
SELECT * FROM PPIHistoricalForecast;

# Returns actual historical PPI data with real percent changes per item per year.
-- full_data_historicalppi
SELECT * FROM historicalppi;

# Returns PPI forecast data made under the old methodology prior to September 2023.
-- full_data_ppiforecastarchived
SELECT * FROM ppiforecastarchived;

# Calculates the forecast accuracy for each CPI item and year using Mean Absolute Error (MAE).
# Joins historical CPI forecasts (new method) to actuals on item and year.
# Filters to only include mid-point forecasts for meaningful accuracy evaluation.
-- forecast_accuracy_cpi
SELECT 
    f.consumerPriceIndexItem AS item,
    f.yearBeingForecast AS year,
    AVG(ABS(f.forecastPercentChange - h.percentChange)) AS mean_absolute_error
FROM CPIHistoricalForecast f
JOIN historicalcpi h 
  ON f.consumerPriceIndexItem = h.consumerPriceIndexItem 
 AND f.yearBeingForecast = h.year
WHERE LOWER(f.attribute) LIKE '%mid%'
GROUP BY f.consumerPriceIndexItem, f.yearBeingForecast
ORDER BY f.consumerPriceIndexItem, f.yearBeingForecast;

# Calculates the forecast accuracy for each PPI item and year using Mean Absolute Error (MAE).
# Joins historical PPI forecasts (new method) to actuals on item and year.
# Filters for mid-point forecasts to ensure consistency in error evaluation.
-- forecast_accuracy_ppi
SELECT 
    f.producerPriceIndexItem AS item,
    f.yearBeingForecast AS year,
    AVG(ABS(f.forecastPercentChange - h.percentChange)) AS mean_absolute_error
FROM PPIHistoricalForecast f
JOIN historicalppi h
  ON f.producerPriceIndexItem = h.producerPriceIndexItem 
 AND f.yearBeingForecast = h.year
WHERE LOWER(f.attribute) LIKE '%mid%'
GROUP BY f.producerPriceIndexItem, f.yearBeingForecast
ORDER BY f.producerPriceIndexItem, f.yearBeingForecast;

# Compares CPI forecasts from the old (archived) and new (historical) methodology.
# Joins on item, year, and forecast month. Filters for valid mid-point numeric forecasts.
# Helps evaluate how forecast predictions changed between methodologies.
# Regex documentation found at https://www.geeksforgeeks.org/mysql-regular-expressions-regexp/
-- old_vs_new_methodology_cpi
SELECT 
    a.consumerPriceIndexItem AS item,
    a.yearBeingForecast AS year,
    a.monthOfForecast AS month,
    CAST(a.forecastPercentChange AS DECIMAL(5,2)) AS old_forecast,
    CAST(h.forecastPercentChange AS DECIMAL(5,2)) AS new_forecast
FROM cpiforecastarchived a
JOIN CPIHistoricalForecast h 
  ON a.consumerPriceIndexItem = h.consumerPriceIndexItem
  AND a.yearBeingForecast = h.yearBeingForecast
  AND a.monthOfForecast = h.monthOfForecast
WHERE LOWER(a.attribute) LIKE '%mid%'
  AND LOWER(h.attribute) LIKE '%mid%'
  AND a.forecastPercentChange REGEXP '^-?[0-9]+(\\.[0-9]+)?$'
  AND h.forecastPercentChange REGEXP '^-?[0-9]+(\\.[0-9]+)?$'
ORDER BY item, year, month;

# Compares PPI forecasts from the old (archived) and new (historical) methodology.
# Joins on item, year, and forecast month. Focuses on valid mid-point numeric forecasts.
# Enables analysis of shifts in prediction strategies or forecast accuracy post-revision.
# Regex documentation found at https://www.geeksforgeeks.org/mysql-regular-expressions-regexp/.
-- old_vs_new_methodology_ppi
SELECT 
    a.producerPriceIndexItem AS item,
    a.yearBeingForecast AS year,
    a.monthOfForecast AS month,
    CAST(a.forecastPercentChange AS DECIMAL(5,2)) AS old_forecast,
    CAST(h.forecastPercentChange AS DECIMAL(5,2)) AS new_forecast
FROM ppiforecastarchived a
JOIN PPIHistoricalForecast h 
  ON a.producerPriceIndexItem = h.producerPriceIndexItem
  AND a.yearBeingForecast = h.yearBeingForecast
  AND a.monthOfForecast = h.monthOfForecast
WHERE LOWER(a.attribute) LIKE '%mid%'
  AND LOWER(h.attribute) LIKE '%mid%'
  AND a.forecastPercentChange REGEXP '^-?[0-9]+(\\.[0-9]+)?$'
  AND h.forecastPercentChange REGEXP '^-?[0-9]+(\\.[0-9]+)?$'
ORDER BY item, year, month;