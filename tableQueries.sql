# ----------------- JOIN QUERIES -----------------
# !! make sure very detailed comments are written on the exact goal and purpose of the join statement
# also make all comments with # and query names with --

-- This query compares the lower and upper bound CPI forecasts 
-- from the archived dataset to the actual historical CPI values 
-- for each year and item. It calculates the percent error from both bounds, 
-- which lets me create a forecast uncertainty range graph.
SELECT 
    a.yearBeingForecast AS year,
    a.consumerPriceIndexItem AS series,
    MIN(CASE WHEN a.attribute = 'Lower bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) AS lowerBoundForecast,
    MIN(CASE WHEN a.attribute = 'Upper bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) AS upperBoundForecast,
    h.percentChange AS actualValue,
    
    -- Percent error calculations
    ROUND(ABS((MIN(CASE WHEN a.attribute = 'Lower bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) - h.percentChange) / h.percentChange) * 100, 2) AS lowerBoundPercentError,
    ROUND(ABS((MIN(CASE WHEN a.attribute = 'Upper bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) - h.percentChange) / h.percentChange) * 100, 2) AS upperBoundPercentError
FROM 
    foodprices.cpiforecastarchived a
JOIN 
    foodprices.historicalcpi h 
    ON a.yearBeingForecast = h.year
    AND a.consumerPriceIndexItem = h.consumerPriceIndexItem
WHERE 
    a.attribute IN ('Lower bound of forecast range', 'Upper bound of forecast range')
    AND h.percentChange IS NOT NULL
GROUP BY 
    a.yearBeingForecast, a.consumerPriceIndexItem, h.percentChange;


-- This query compares the lower and upper bound archived 
-- PPI forecasts to the actual historical PPI percent changes 
-- by year and index item. It computes the percent error from 
-- both bounds, allowing you to visualize a confidence-style forecast range for PPI trends.
SELECT 
    a.yearBeingForecast AS year,
    a.producerPriceIndexItem AS series,
    MIN(CASE WHEN a.attribute = 'Lower bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) AS lowerBoundForecast,
    MIN(CASE WHEN a.attribute = 'Upper bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) AS upperBoundForecast,
    h.percentChange AS actualValue,
    
    -- Percent error calculations
    ROUND(ABS((MIN(CASE WHEN a.attribute = 'Lower bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) - h.percentChange) / h.percentChange) * 100, 2) AS lowerBoundPercentError,
    ROUND(ABS((MIN(CASE WHEN a.attribute = 'Upper bound of forecast range' THEN CAST(a.forecastPercentChange AS DECIMAL(5,2)) END) - h.percentChange) / h.percentChange) * 100, 2) AS upperBoundPercentError

FROM 
    foodprices.ppiforecastarchived a
JOIN 
    foodprices.historicalppi h 
    ON a.yearBeingForecast = h.year
    AND a.producerPriceIndexItem = h.producerPriceIndexItem

WHERE 
    a.attribute IN ('Lower bound of forecast range', 'Upper bound of forecast range')
    AND h.percentChange IS NOT NULL

GROUP BY 
    a.yearBeingForecast, a.producerPriceIndexItem, h.percentChange;


-- This query compares monthly CPI forecasts from the old 
-- and new methods for each year and category, showing how 
-- the updated estimation process has changed projected percent changes over time.
SELECT 
    hist.monthOfForecast,
    hist.yearOfForecast,
    hist.yearBeingForecast AS year,
    hist.consumerPriceIndexItem AS series,
    CAST(hist.forecastPercentChange AS DECIMAL(5,2)) AS newForecast,
    CAST(arch.forecastPercentChange AS DECIMAL(5,2)) AS oldForecast,
    ROUND(CAST(hist.forecastPercentChange AS DECIMAL(5,2)) - CAST(arch.forecastPercentChange AS DECIMAL(5,2)), 2) AS forecastDifference
FROM 
    foodprices.cpihistoricalforecast hist
JOIN 
    foodprices.cpiforecastarchived arch 
    ON hist.monthOfForecast = arch.monthOfForecast
    AND hist.yearOfForecast = arch.yearOfForecast
    AND hist.yearBeingForecast = arch.yearBeingForecast
    AND hist.attribute = arch.attribute
    AND hist.consumerPriceIndexItem = arch.consumerPriceIndexItem
WHERE 
    hist.forecastPercentChange IS NOT NULL
    AND arch.forecastPercentChange IS NOT NULL;

-- This query highlights the difference between old 
-- and new PPI forecasts by month, year, and item, allowing 
-- you to see how the revised method has altered producer price expectations.
SELECT 
    hist.monthOfForecast,
    hist.yearOfForecast,
    hist.yearBeingForecast AS year,
    hist.producerPriceIndexItem AS series,
    CAST(hist.forecastPercentChange AS DECIMAL(5,2)) AS newForecast,
    CAST(arch.forecastPercentChange AS DECIMAL(5,2)) AS oldForecast,
    ROUND(CAST(hist.forecastPercentChange AS DECIMAL(5,2)) - CAST(arch.forecastPercentChange AS DECIMAL(5,2)), 2) AS forecastDifference
FROM 
    foodprices.ppihistoricalforecast hist
JOIN 
    foodprices.ppiforecastarchived arch 
    ON hist.monthOfForecast = arch.monthOfForecast
    AND hist.yearOfForecast = arch.yearOfForecast
    AND hist.yearBeingForecast = arch.yearBeingForecast
    AND hist.attribute = arch.attribute
    AND hist.producerPriceIndexItem = arch.producerPriceIndexItem
WHERE 
    hist.forecastPercentChange IS NOT NULL
    AND arch.forecastPercentChange IS NOT NULL;

-- See how actual consumer food prices have changed over time across categories.
SELECT 
    year,
    consumerPriceIndexItem AS category,
    ROUND(AVG(percentChange), 2) AS avgAnnualChange
FROM 
    foodprices.historicalcpi
GROUP BY 
    year, category
ORDER BY 
    year, avgAnnualChange DESC;

-- See how actual producer food prices have changed over time across categories.
SELECT 
    year,
    producerPriceIndexItem AS category,
    ROUND(AVG(percentChange), 2) AS avgAnnualChange
FROM 
    foodprices.historicalppi
GROUP BY 
    year, category
ORDER BY 
    year, avgAnnualChange DESC;

-- Identify which categories show the most variability in cosumer prices.
SELECT 
    consumerPriceIndexItem AS category,
    ROUND(STDDEV(percentChange), 2) AS volatility
FROM 
    foodprices.historicalcpi
GROUP BY 
    category
ORDER BY 
    volatility ASC;
    
-- Identify which categories show the most variability in producer prices.
SELECT 
    producerPriceIndexItem AS category,
    ROUND(STDDEV(percentChange), 2) AS volatility
FROM 
    foodprices.historicalppi
GROUP BY 
    category
ORDER BY 
    volatility ASC;

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
