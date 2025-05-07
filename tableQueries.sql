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
FROM CPIHistoricalForecast f  # naming simplification
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
FROM PPIHistoricalForecast f  # naming simplification
JOIN historicalppi h
  ON f.producerPriceIndexItem = h.producerPriceIndexItem 
 AND f.yearBeingForecast = h.year
WHERE LOWER(f.attribute) LIKE '%mid%'
GROUP BY f.producerPriceIndexItem, f.yearBeingForecast
ORDER BY f.producerPriceIndexItem, f.yearBeingForecast;
