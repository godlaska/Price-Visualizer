# The purpose of these auto incremented keys is to add a primary key
# to all the tables as they don't explicitly have one. More specific details
# can be found in the ER diagram.

# ----------------- ADD INCREMENTING ID (PRIMARY KEY) -----------------
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

