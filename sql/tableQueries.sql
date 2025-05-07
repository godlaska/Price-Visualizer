# ----------------- JOIN QUERIES -----------------
# !! make sure very detailed comments are written on the exact goal and purpose of the join statement
# also make all comments with # and query names with --

-- full_data_CPIForecast_for_2025_bounds
SELECT cpiforecast_id, aggregate, midLevel, disaggregate, attribute, unit, value
FROM CPIForecast
WHERE unit = 'Percent change'
  AND (
    attribute LIKE 'Lower bound of prediction interval 2025%' OR
    attribute LIKE 'Upper bound of prediction interval 2025%'
  );

-- full_data_CPIHistoricalForecast
SELECT * FROM CPIHistoricalForecast;

-- full_data_historicalcpi
SELECT * FROM historicalcpi;

-- full_data_cpiforecastarchived
SELECT * FROM cpiforecastarchived;

-- full_data_PPIForecast_for_2025_bounds
SELECT ppiforecast_id, producerPriceIndexItem, attribute, unit, value
FROM PPIForecast
WHERE unit = 'Percent change'
  AND (
    attribute LIKE '%Lower bound of prediction interval 2025%' OR
    attribute LIKE '%Upper bound of prediction interval 2025%'
  );

-- full_data_PPIHistoricalForecast
SELECT * FROM PPIHistoricalForecast;

-- full_data_historicalppi
SELECT * FROM historicalppi;

-- full_data_ppiforecastarchived
SELECT * FROM ppiforecastarchived;

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

-- old_vs_new_methodology_cpi
# Compares percent forecasts between old (archived) and new (historical) CPI forecast methods
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

-- old_vs_new_methodology_ppi
# Compares percent forecasts between old (archived) and new (historical) PPI forecast methods
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