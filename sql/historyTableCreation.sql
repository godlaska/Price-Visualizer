-- This table stores a log of all SQL queries run through the application,
-- including the full query text and the timestamp when it was executed.
-- It enables tracking user interactions for auditing, debugging,
-- and providing a query history view within the CPI/PPI forecast tool.
CREATE TABLE query_history (
  id INT AUTO_INCREMENT PRIMARY KEY,
  query_text TEXT NOT NULL,
  run_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
