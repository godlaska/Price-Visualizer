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
