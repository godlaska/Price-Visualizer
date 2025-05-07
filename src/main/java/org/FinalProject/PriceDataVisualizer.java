/**
 * PriceDataVisualizer.java
 * @author Keigen Godlaski
 * @copyright (c) 2025 Keigen Godlaski
 * @version 1.0
 * @date 2025-05-06
 */

package org.FinalProject;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * PriceDataVisualizer is a Java application that connects to a MySQL database
 * to visualize food price data. It allows users to run various SQL queries
 * and display the results in charts and tables. The application uses
 * HikariCP for database connection pooling and JFreeChart for charting.
 * It also provides a GUI for user interaction, including
 * checkboxes for selecting categories and a history of executed queries.
 */
public class PriceDataVisualizer {
    private static HikariDataSource dataSource;
    private static Map<String, String> queryMap = new HashMap<>();
    private static JPanel chartPanel;
    private static JPanel checkboxPanel;
    private static final Map<String, JCheckBox> categoryCheckboxes =
            new LinkedHashMap<>();
    private static JTable displayDataTable;
    private static DefaultListModel<String> historyModel = new DefaultListModel<>();
    private static JList<String> historyList = new JList<>(historyModel);

    /**
     * Starts the application by setting up the database connection, loading
     * queries, and launching the GUI.
     * @param args command-line arguments passed to the program
     */
    public static void main(String[] args) {
        setupConnectionPool();
        try {
            queryMap = loadQueriesFromFile("sql/tableQueries.sql");
        } catch (IOException e) {
            System.err.println("Failed to load queries from file: tableQueries.sql");
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(PriceDataVisualizer::closeConnectionPool));
        SwingUtilities.invokeLater(PriceDataVisualizer::createAndShowGUI);
    }

    /**
     * Sets up the HikariCP connection pool for MySQL database access.
     */
    private static void setupConnectionPool() {
        HikariConfig config = new HikariConfig();
        // Make sure the table name reflects the actual database name. If you
        // followed the documentation exactly, this shouldn't need changed
        config.setJdbcUrl("jdbc:mysql://localhost:3306/foodprices");
        // Username and password should be replaced with actual credentials
        config.setUsername("root");
        config.setPassword("pass");
        config.setMaximumPoolSize(5);
        dataSource = new HikariDataSource(config);
    }

    /**
     * Establishes a connection to the database using the HikariCP connection pool.
     * @return a Connection object to interact with the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Loads SQL queries from a specified file into a map.
     * @param filePath the path to the file containing SQL queries
     * @return a map of query names to SQL strings
     * @throws IOException if an error occurs while reading the file
     */
    public static Map<String, String> loadQueriesFromFile(String filePath) throws IOException {
        Map<String, String> queries = new HashMap<>();
        StringBuilder currentQuery = new StringBuilder();
        String currentKey = null;

        // Read SQL file, add all queries to map, indicated by a --
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--")) {
                    if (currentKey != null && !currentQuery.isEmpty()) {
                        queries.put(currentKey, currentQuery.toString().trim());
                    }
                    currentKey = line.substring(2).trim();
                    currentQuery = new StringBuilder();
                // Skip empty lines and comments
                } else if (!line.isEmpty() && !line.startsWith("#")) {
                    currentQuery.append(line).append(" ");
                }
            }
            // Add the last query if it exists
            if (currentKey != null && !currentQuery.isEmpty()) {
                queries.put(currentKey, currentQuery.toString().trim());
            }
        }
        return queries;
    }

    /**
     * Logs a given SQL query to the query_history table in the database.
     * @param sql the SQL query string to log
     */
    public static void logQueryToHistory(String sql) {
        // Log the query to the history table
        String insert = "INSERT INTO query_history (query_text) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the query history from the database and populates the history
     * list model.
     */
    public static void loadQueryHistory() {
        historyModel.clear();
        // Load the query history from the database
        String select = "SELECT query_text FROM query_history ORDER BY run_timestamp DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {
            // Populate the history model with the results
            while (rs.next()) {
                String query = rs.getString("query_text").trim();
                if (!query.isEmpty()) historyModel.addElement(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and displays the main GUI for the application.
     */
    private static void createAndShowGUI() {
        // Set up the main frame
        JFrame frame = new JFrame("Price Data Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Set up the menu bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> querySelector = new JComboBox<>(new String[]{"", "Show Full Data", "Volatility", "Forecast Accuracy", "Methodology Comparison"});
        JComboBox<String> typeSelector = new JComboBox<>(new String[]{"Consumer Price Index", "Producer Price Index"});
        JComboBox<String> tableSelector = new JComboBox<>();
        tableSelector.setVisible(false);

        // Initialize the data table
        displayDataTable = new JTable();

        // Set up year range query components
        JLabel yearRangeLabel = new JLabel("Year Range: ");
        JLabel yearToLabel = new JLabel("to");
        yearRangeLabel.setVisible(false);
        yearToLabel.setVisible(false);
        JSpinner yearFromSpinner = new JSpinner(new SpinnerNumberModel(2015, 1974, 2023, 1));
        JSpinner yearToSpinner = new JSpinner(new SpinnerNumberModel(2023, 1974, 2023, 1));
        yearFromSpinner.setVisible(false);
        yearToSpinner.setVisible(false);

        JButton runQueryButton = new JButton("Run Query");
        runQueryButton.setVisible(false);

        // Table selection
        String[] cpiTables = {"CPIForecast", "CPIHistoricalForecast", "historicalcpi", "cpiforecastarchived"};
        String[] ppiTables = {"PPIForecast", "PPIHistoricalForecast", "historicalppi", "ppiforecastarchived"};

        // Set up description area
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Description"));
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Default description text
        String welcomeSplashText = "Welcome to the Food Price Data Visualizer! Use this " +
                "tool to explore consumer and producer item " +
                "forecasts, track accuracy of price " +
                "predictions, compare forecasting methods, " +
                "analyze volatility, and assess prediction confidence.";
        descriptionArea.setText(welcomeSplashText);

        descriptionArea.setPreferredSize(new Dimension(300, 150));

        // Logic for query selection
        ActionListener refreshTableSelector = e -> {
            String selectedQuery = (String) querySelector.getSelectedItem();
            String selectedType = (String) typeSelector.getSelectedItem();
            tableSelector.removeAllItems();

            boolean isVolatility = "Volatility".equals(selectedQuery);
            boolean isFullData = "Show Full Data".equals(selectedQuery);
            boolean isAccuracy = "Forecast Accuracy".equals(selectedQuery);
            boolean isMethodology = "Methodology Comparison".equals(selectedQuery);

            // Enable/disable components based on query selection
            tableSelector.setVisible(isFullData);
            yearRangeLabel.setVisible(isVolatility);
            yearFromSpinner.setVisible(isVolatility);
            yearToLabel.setVisible(isVolatility);
            yearToSpinner.setVisible(isVolatility);
            runQueryButton.setVisible(isVolatility || isAccuracy || isMethodology);

            // If the query is "Show Full Data", show the table selector with
            // CPI and PPI tables
            if (isFullData) {
                String[] tables = selectedType.equals("Consumer Price Index") ? cpiTables : ppiTables;
                for (String t : tables) tableSelector.addItem(t);
                tableSelector.setSelectedIndex(0);
            }

            // Description text based on selected query
            String description = switch (selectedQuery) {
                case "Show Full Data" ->
                        "Displays the complete dataset for forecasts and historical values. "
                                + "You can explore CPI and PPI tables individually, including archived and current forecast values.";

                case "Volatility" ->
                        "Analyzes volatility by calculating the average annual percent change for each category. "
                                + "This helps identify which food price categories have been most stable or unstable over time.";

                case "Forecast Accuracy" ->
                        "Measures the accuracy of forecasts by comparing predicted percent changes to actual observed values. "
                                + "The chart and table display mean absolute error for each category and year, helping evaluate model reliability.";

                case "Methodology Comparison" ->
                        "Compares old vs. new forecasting methodologies (pre- and post-September 2023) side-by-side. "
                                + "Each category is plotted with two lines: one from the archived (old) method and one from the updated (new) model, "
                                + "highlighting any shifts in forecasting behavior or assumptions.";

                default -> welcomeSplashText;
            };
            descriptionArea.setText(description);
        };

        // Add action listeners to components
        querySelector.addActionListener(refreshTableSelector);
        typeSelector.addActionListener(refreshTableSelector);

        // Table selection logic
        tableSelector.addActionListener(e -> {
            if (!tableSelector.isVisible()) return;
            String tableName = (String) tableSelector.getSelectedItem();
            if (tableName == null || tableName.isEmpty()) return;

            if (tableName.equals("CPIForecast") || tableName.equals("PPIForecast")) {
                showForecastChart(tableName, displayDataTable);
            } else {
                showTableData(tableName, displayDataTable);
            }
        });

        // Run query button action
        runQueryButton.addActionListener(e -> {
            String selectedQuery = (String) querySelector.getSelectedItem();
            String indexType = (String) typeSelector.getSelectedItem();
            JPanel currentCenterPanel = (JPanel) chartPanel.getParent(); // Get centerPanel reference

            // Run the appropriate query based on selection
            if ("Volatility".equals(selectedQuery)) {
                int yearFrom = (int) yearFromSpinner.getValue();
                int yearTo = (int) yearToSpinner.getValue();
                runVolatilityQuery(indexType, yearFrom, yearTo, currentCenterPanel);
            } else if ("Forecast Accuracy".equals(selectedQuery)) {
                runForecastAccuracyQuery(indexType, currentCenterPanel);
            } else if ("Methodology Comparison".equals(selectedQuery)) {
                runMethodologyComparisonQuery(indexType, currentCenterPanel);
            }
        });

        // Top panel layout
        topPanel.add(new JLabel("Select Query: "));
        topPanel.add(querySelector);
        topPanel.add(new JLabel("Select Type: "));
        topPanel.add(typeSelector);
        topPanel.add(new JLabel("Select Table: "));
        topPanel.add(tableSelector);
        topPanel.add(yearRangeLabel);
        topPanel.add(yearFromSpinner);
        topPanel.add(yearToLabel);
        topPanel.add(yearToSpinner);
        topPanel.add(runQueryButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setPreferredSize(new Dimension(700, 400));
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        // Set up the display data table
        JScrollPane dataScrollPane = new JScrollPane(displayDataTable);
        dataScrollPane.setPreferredSize(new Dimension(700, 200));
        centerPanel.add(dataScrollPane, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Filtering checkbox setup
        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBorder(BorderFactory.createTitledBorder("Select Categories"));
        JScrollPane checkboxScrollPane = new JScrollPane(checkboxPanel);
        checkboxScrollPane.setPreferredSize(new Dimension(200, 400));
        checkboxScrollPane.setMinimumSize(new Dimension(200, 400));
        centerPanel.add(checkboxScrollPane, BorderLayout.WEST);

        // History panel setup
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setPreferredSize(new Dimension(300, 0));
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        // Clear history button
        JButton clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearHistoryButton.setBackground(Color.PINK);
        clearHistoryButton.addActionListener(ev -> {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM query_history");
                historyModel.clear();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // History list setup and logic. If the user selects a query
        // from the history, it will run that query.
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedSQL = historyList.getSelectedValue();
                if (selectedSQL != null && !selectedSQL.isBlank()) {
                    try (Connection conn = getConnection();
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(selectedSQL)) {
                        logQueryToHistory(selectedSQL);

                        ResultSetMetaData meta = rs.getMetaData();
                        int columnCount = meta.getColumnCount();
                        Vector<String> columnNames = new Vector<>();
                        for (int i = 1; i <= columnCount; i++) columnNames.add(meta.getColumnName(i));

                        Vector<Vector<Object>> data = new Vector<>();
                        while (rs.next()) {
                            Vector<Object> row = new Vector<>();
                            for (int i = 1; i <= columnCount; i++) row.add(rs.getObject(i));
                            data.add(row);
                        }

                        JTable table = new JTable();
                        table.setModel(new DefaultTableModel(data, columnNames));

                        JScrollPane scrollPane = new JScrollPane(table);
                        JFrame resultFrame = new JFrame("Query Result");
                        resultFrame.setSize(800, 400);
                        resultFrame.add(scrollPane);
                        resultFrame.setVisible(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Setup for the bottom history panel
        JPanel bottomHistoryPanel = new JPanel(new BorderLayout());
        bottomHistoryPanel.setPreferredSize(new Dimension(300, 200));
        bottomHistoryPanel.add(clearHistoryButton, BorderLayout.NORTH);
        bottomHistoryPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        historyPanel.add(bottomHistoryPanel, BorderLayout.SOUTH);
        mainPanel.add(historyPanel, BorderLayout.EAST);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    /**
     * Compares the old and new methodologies for forecasting.
     * @param indexType the type of index (CPI or PPI)
     * @param centerPanel the center panel of the GUI
     */
    private static void runMethodologyComparisonQuery(String indexType, JPanel centerPanel) {
        // Get the SQL query based on the index type
        String sqlKey = indexType.equals("Consumer Price Index") ? "old_vs_new_methodology_cpi" : "old_vs_new_methodology_ppi";
        String sql = queryMap.get(sqlKey);
        if (sql == null) {
            JOptionPane.showMessageDialog(null, "No query found for " + sqlKey, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        logQueryToHistory(sql);
        loadQueryHistory();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            // Get column names for the table
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(meta.getColumnName(i));
            }

            // Prepare data for the table and chart
            Vector<Vector<Object>> tableData = new Vector<>();
            Map<String, TimeSeries> seriesMap = new LinkedHashMap<>();

            while (rs.next()) {
                // Populate table data
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                tableData.add(row);

                String item = rs.getString("item");
                int year = rs.getInt("year");
                int month = rs.getInt("month");

                double oldVal = rs.getDouble("old_forecast");
                double newVal = rs.getDouble("new_forecast");

                String oldLabel = item + " (Old)";
                String newLabel = item + " (New)";

                seriesMap.putIfAbsent(oldLabel, new TimeSeries(oldLabel));
                seriesMap.putIfAbsent(newLabel, new TimeSeries(newLabel));

                seriesMap.get(oldLabel).addOrUpdate(new Month(month, year), oldVal);
                seriesMap.get(newLabel).addOrUpdate(new Month(month, year), newVal);
            }

            // Create the dataset for the chart
            final Vector<Vector<Object>> originalTableData = new Vector<>(tableData);
            final Vector<String> originalColumnNames = new Vector<>(columnNames);

            // Reset the table model
            displayDataTable.setModel(new DefaultTableModel(tableData, columnNames));
            chartPanel.removeAll();
            checkboxPanel.removeAll();
            categoryCheckboxes.clear();

            // Set up the checkbox panel
            checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
            JLabel titleLabel = new JLabel("Select Categories");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            checkboxPanel.add(titleLabel);

            // Group checkboxes by base item (not old/new)
            Set<String> baseItems = new TreeSet<>();
            for (String label : seriesMap.keySet()) {
                baseItems.add(label.replace(" (Old)", "").replace(" (New)", ""));
            }

            // Create checkboxes for each base item
            // Documentation at https://www.jfree.org/jfreechart/api/javadoc/org/jfree/data/time/TimeSeriesCollection.html
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            for (String baseItem : baseItems) {
                JCheckBox cb = new JCheckBox(baseItem, true);
                cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
                cb.setAlignmentX(Component.LEFT_ALIGNMENT);
                cb.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                categoryCheckboxes.put(baseItem, cb);
                checkboxPanel.add(cb);
                dataset.addSeries(seriesMap.get(baseItem + " (Old)"));
                dataset.addSeries(seriesMap.get(baseItem + " (New)"));
            }

            // Control buttons
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(3, 1, 5, 5));
            controlPanel.setMaximumSize(new Dimension(150, 100));
            controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Filter buttons
            JButton selectAll = new JButton("Select All");
            JButton deselectAll = new JButton("Deselect All");
            JButton applyFilter = new JButton("Apply Filter");

            // Add action listeners to filter buttons
            selectAll.addActionListener(e -> categoryCheckboxes.values().forEach(cb -> cb.setSelected(true)));
            deselectAll.addActionListener(e -> categoryCheckboxes.values().forEach(cb -> cb.setSelected(false)));

            // Apply filter based on selected checkboxes
            applyFilter.addActionListener(e -> {
                TimeSeriesCollection filteredDataset = new TimeSeriesCollection();
                Vector<Vector<Object>> filteredTableContent = new Vector<>();

                // Add selected items to the filtered dataset
                for (String item : categoryCheckboxes.keySet()) {
                    if (categoryCheckboxes.get(item).isSelected()) {
                        filteredDataset.addSeries(seriesMap.get(item + " (Old)"));
                        filteredDataset.addSeries(seriesMap.get(item + " (New)"));
                    }
                }

                // Filter table data based on selected items
                for (Vector<Object> row : originalTableData) {
                    String rowItem = row.get(0).toString();
                    if (categoryCheckboxes.containsKey(rowItem) && categoryCheckboxes.get(rowItem).isSelected()) {
                        filteredTableContent.add(row);
                    }
                }

                // Create a new chart with the filtered dataset
                JFreeChart filteredChart = createTimeSeriesChart(indexType + " Methodology Comparison", filteredDataset);
                chartPanel.removeAll();
                ChartPanel cp = new ChartPanel(filteredChart);
                cp.setPreferredSize(new Dimension(700, 400));
                cp.setMouseWheelEnabled(true);
                cp.setDomainZoomable(true);
                cp.setRangeZoomable(true);
                chartPanel.add(cp, BorderLayout.CENTER);
                chartPanel.validate();
                chartPanel.repaint();

                displayDataTable.setModel(new DefaultTableModel(filteredTableContent, originalColumnNames));
            });

            // Add buttons to the control panel
            controlPanel.add(selectAll);
            controlPanel.add(deselectAll);
            controlPanel.add(applyFilter);
            checkboxPanel.add(Box.createVerticalStrut(10));
            checkboxPanel.add(controlPanel);

            // Create the default chart when query is loaded
            JFreeChart chart = createTimeSeriesChart(indexType + " Methodology Comparison", dataset);
            ChartPanel cp = new ChartPanel(chart);
            cp.setPreferredSize(new Dimension(700, 400));
            cp.setMouseWheelEnabled(true);
            cp.setDomainZoomable(true);
            cp.setRangeZoomable(true);
            chartPanel.setLayout(new BorderLayout());
            chartPanel.add(cp, BorderLayout.CENTER);

            chartPanel.revalidate();
            chartPanel.repaint();
            checkboxPanel.revalidate();
            checkboxPanel.repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Runs the forecast accuracy query and updates the chart and table.
     * @param indexType the type of index (CPI or PPI)
     * @param centerPanel the center panel of the GUI
     */
    private static void runForecastAccuracyQuery(String indexType, JPanel centerPanel) {
        String sqlKey = indexType.equals("Consumer Price Index") ? "forecast_accuracy_cpi" : "forecast_accuracy_ppi";
        String sql = queryMap.get(sqlKey);
        if (sql == null) {
            JOptionPane.showMessageDialog(null, "No query found for " + sqlKey, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Log the query to history
        logQueryToHistory(sql);
        loadQueryHistory();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Get metadata for the result set and add column names to the table
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            Vector<Vector<Object>> tableData = new Vector<>(); // For the table model

            Map<String, Map<Integer, Double>> dataMap = new LinkedHashMap<>(); // For the chart
            Set<Integer> years = new TreeSet<>(); // To store unique years for the chart axis

            while (rs.next()) {
                Vector<Object> row = new Vector<>(); // For table
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                tableData.add(row);

                String item = rs.getString("item"); // For chart
                int year = rs.getInt("year");
                double error = rs.getDouble("mean_absolute_error");

                years.add(year);
                dataMap.computeIfAbsent(item, k -> new HashMap<>()).put(year, error);
            }

            // Determine min and max year for the chart title dynamically
            int minYear = years.isEmpty() ? 0 : Collections.min(years);
            int maxYear = years.isEmpty() ? 0 : Collections.max(years);

            DefaultCategoryDataset dataset = createDataset(dataMap, years);
            JFreeChart chart = createChart(dataset, indexType + " Forecast Accuracy", minYear, maxYear);

            chartPanel.removeAll(); // Clear previous chart
            ChartPanel cp = new ChartPanel(chart);
            cp.setPreferredSize(new Dimension(700, 400));
            cp.setMouseWheelEnabled(true);
            cp.setDomainZoomable(true);
            cp.setRangeZoomable(true);
            chartPanel.setLayout(new BorderLayout()); // Ensure chartPanel has a layout
            chartPanel.add(cp, BorderLayout.CENTER);

            // Set the table model with the data
            displayDataTable.setModel(new DefaultTableModel(tableData, columnNames));

            final Map<String, Map<Integer, Double>> originalDataMap = new LinkedHashMap<>(dataMap);
            final Vector<Vector<Object>> originalTableData = new Vector<>(tableData); // Store original table data

            checkboxPanel.removeAll();
            checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS)); // Ensure layout
            categoryCheckboxes.clear();

            // Set up the checkbox panel
            JLabel titleLabel = new JLabel("Select Categories");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            checkboxPanel.add(titleLabel);

            // Create checkboxes for each item in the data map
            for (String item : dataMap.keySet()) { // Use dataMap keys for checkboxes
                JCheckBox box = new JCheckBox(item, true);
                box.setFont(new Font("SansSerif", Font.PLAIN, 12));
                box.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                categoryCheckboxes.put(item, box);
                checkboxPanel.add(box);
            }

            // Control buttons
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(3, 1, 5, 5));
            controlPanel.setMaximumSize(new Dimension(150, 100));
            controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton selectAll = new JButton("Select All");
            JButton deselectAll = new JButton("Deselect All");
            JButton applyFilter = new JButton("Apply Filter");

            selectAll.addActionListener(e -> categoryCheckboxes.values().forEach(cb -> cb.setSelected(true)));
            deselectAll.addActionListener(e -> categoryCheckboxes.values().forEach(cb -> cb.setSelected(false)));

            // Apply filter based on selected checkboxes
            applyFilter.addActionListener(e -> {
                Map<String, Map<Integer, Double>> filteredChartData = new LinkedHashMap<>();
                Vector<Vector<Object>> filteredTableContent = new Vector<>(); // For the table

                for (Map.Entry<String, JCheckBox> entry : categoryCheckboxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        String key = entry.getKey();
                        if (originalDataMap.containsKey(key)) {
                            filteredChartData.put(key, originalDataMap.get(key));
                        }
                    }
                }

                // Filter table data based on selected items
                for (Vector<Object> row : originalTableData) {
                    String rowItem = row.get(0).toString(); // Assuming item is the first column
                    if (categoryCheckboxes.containsKey(rowItem) && categoryCheckboxes.get(rowItem).isSelected()) {
                        filteredTableContent.add(row);
                    }
                }

                DefaultCategoryDataset filteredDataset = createDataset(filteredChartData, years); // Use original 'years'
                JFreeChart filteredChart = createChart(filteredDataset, indexType + " Forecast Accuracy", minYear, maxYear);

                // Update the chart panel with the filtered chart
                chartPanel.removeAll();
                ChartPanel newChartPanel = new ChartPanel(filteredChart);
                newChartPanel.setPreferredSize(new Dimension(700, 400));
                newChartPanel.setMouseWheelEnabled(true);
                newChartPanel.setDomainZoomable(true);
                newChartPanel.setRangeZoomable(true);
                chartPanel.add(newChartPanel, BorderLayout.CENTER);
                chartPanel.revalidate();
                chartPanel.repaint();

                displayDataTable.setModel(new DefaultTableModel(filteredTableContent, columnNames));
            });

            controlPanel.add(selectAll);
            controlPanel.add(deselectAll);
            controlPanel.add(applyFilter);
            checkboxPanel.add(Box.createVerticalStrut(10));
            checkboxPanel.add(controlPanel);

            centerPanel.revalidate();
            centerPanel.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Runs the volatility query and updates the chart and table.
     * @param indexType the type of index (CPI or PPI)
     * @param yearFrom starting year for the query
     * @param yearTo ending year for the query
     * @param centerPanel the center panel of the GUI
     */
    private static void runVolatilityQuery(String indexType, int yearFrom, int yearTo, JPanel centerPanel) {
        String table = indexType.equals("Consumer Price Index") ? "historicalcpi" : "historicalppi";
        String itemColumn = indexType.equals("Consumer Price Index") ? "consumerPriceIndexItem" : "producerPriceIndexItem";

        // Embedded SQL query to calculate average percent change based on
        // dynamic year range
        String sql = String.format("""
    SELECT %s AS item, year, AVG(percentChange) AS avgChange
    FROM %s
    WHERE year BETWEEN %d AND %d
    GROUP BY %s, year
    ORDER BY %s, year
    """, itemColumn, table, yearFrom, yearTo, itemColumn, itemColumn);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            logQueryToHistory(sql);
            loadQueryHistory();

            // Get metadata for the result set and add column names to the table
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            Vector<Vector<Object>> tableData = new Vector<>(); // For the table model

            Map<String, Map<Integer, Double>> dataMap = new LinkedHashMap<>(); // For the chart
            Set<Integer> years = new TreeSet<>();

            // Populate table data and chart data based on the query result
            while (rs.next()) {
                Vector<Object> row = new Vector<>(); // For table
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                tableData.add(row);

                String item = rs.getString("item"); // For chart
                int year = rs.getInt("year");
                double change = rs.getDouble("avgChange");

                years.add(year);
                dataMap.computeIfAbsent(item, k -> new HashMap<>()).put(year, change);
            }

            DefaultCategoryDataset dataset = createDataset(dataMap, years);
            JFreeChart chart = createChart(dataset, indexType + " Item Volatility", yearFrom, yearTo); // Updated title for clarity

            chartPanel.removeAll(); // Clear previous chart
            ChartPanel cp = new ChartPanel(chart);
            cp.setPreferredSize(new Dimension(700, 400));
            cp.setMouseWheelEnabled(true);
            cp.setDomainZoomable(true);
            cp.setRangeZoomable(true);
            chartPanel.setLayout(new BorderLayout()); // Ensure chartPanel has a layout
            chartPanel.add(cp, BorderLayout.CENTER);

            displayDataTable.setModel(new DefaultTableModel(tableData, columnNames));

            final Map<String, Map<Integer, Double>> originalDataMap = new LinkedHashMap<>(dataMap);
            final Vector<Vector<Object>> originalTableData = new Vector<>(tableData); // Store original table data for filtering

            checkboxPanel.removeAll();
            checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS)); // Ensure layout
            categoryCheckboxes.clear();

            JLabel titleLabel = new JLabel("Select Categories");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            checkboxPanel.add(titleLabel);

            // Create checkboxes for each item in the data map
            for (String item : dataMap.keySet()) { // Use dataMap keys for checkboxes
                JCheckBox box = new JCheckBox(item, true);
                box.setFont(new Font("SansSerif", Font.PLAIN, 12));
                box.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                categoryCheckboxes.put(item, box);
                checkboxPanel.add(box);
            }

            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(3, 1, 5, 5));
            controlPanel.setMaximumSize(new Dimension(150, 100));
            controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton selectAll = new JButton("Select All");
            JButton deselectAll = new JButton("Deselect All");
            JButton applyFilter = new JButton("Apply Filter");

            // Add action listeners to filter buttons
            selectAll.addActionListener(e -> categoryCheckboxes.values().forEach(cb -> cb.setSelected(true)));
            deselectAll.addActionListener(e -> categoryCheckboxes.values().forEach(cb -> cb.setSelected(false)));

            applyFilter.addActionListener(e -> {
                Map<String, Map<Integer, Double>> filteredChartData = new LinkedHashMap<>();
                Vector<Vector<Object>> filteredTableContent = new Vector<>();

                for (Map.Entry<String, JCheckBox> entry : categoryCheckboxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        String key = entry.getKey();
                        if (originalDataMap.containsKey(key)) {
                            filteredChartData.put(key, originalDataMap.get(key));
                        }
                    }
                }

                // Filter table data based on selected items
                for (Vector<Object> row : originalTableData) {
                    String rowItem = row.get(0).toString(); // Assuming item is the first column
                    if (categoryCheckboxes.containsKey(rowItem) && categoryCheckboxes.get(rowItem).isSelected()) {
                        filteredTableContent.add(row);
                    }
                }

                DefaultCategoryDataset filteredDataset = createDataset(filteredChartData, years);
                JFreeChart filteredChart = createChart(filteredDataset, indexType + " Item Volatility", yearFrom, yearTo);

                chartPanel.removeAll();
                ChartPanel newChartPanel = new ChartPanel(filteredChart);
                newChartPanel.setPreferredSize(new Dimension(700, 400));
                newChartPanel.setMouseWheelEnabled(true);
                newChartPanel.setDomainZoomable(true);
                newChartPanel.setRangeZoomable(true);
                chartPanel.add(newChartPanel, BorderLayout.CENTER);
                chartPanel.revalidate();
                chartPanel.repaint();

                displayDataTable.setModel(new DefaultTableModel(filteredTableContent, columnNames));
            });

            controlPanel.add(selectAll);
            controlPanel.add(deselectAll);
            controlPanel.add(applyFilter);

            checkboxPanel.add(Box.createVerticalStrut(10));
            checkboxPanel.add(controlPanel);

            centerPanel.revalidate();
            centerPanel.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a dataset for the chart from the given data map and years.
     * @param dataMap the data map containing item names and their corresponding values
     * @param years the set of years to include in the dataset
     * @return a DefaultCategoryDataset object
     */
    private static DefaultCategoryDataset createDataset(Map<String, Map<Integer, Double>> dataMap, Set<Integer> years) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Iterate through the data map and add values to the dataset
        for (Map.Entry<String, Map<Integer, Double>> entry : dataMap.entrySet()) {
            String item = entry.getKey();
            Map<Integer, Double> yearlyData = entry.getValue();
            for (Integer yr : years) {
                Double val = yearlyData.get(yr);
                if (val != null) dataset.addValue(val, item, yr);
            }
        }
        return dataset;
    }

    /**
     * Creates a chart from the given dataset.
     * @param dataset the dataset to be used for the chart
     * @param indexType the type of index (CPI or PPI)
     * @param yearFrom the starting year for the chart
     * @param yearTo the ending year for the chart
     * @return a JFreeChart object
     */
    private static JFreeChart createChart(DefaultCategoryDataset dataset, String indexType, int yearFrom, int yearTo) {
        // Create a line chart with the dataset
        // The chart title, axis labels, and dataset are specified here
        // Documentation found at https://www.jfree.org/jfreechart/api/javadoc/org/jfree/chart/ChartFactory.html
        JFreeChart chart = ChartFactory.createLineChart(
                indexType + " Item Volatility (" + yearFrom + "-" + yearTo + ")",
                "Year", "Avg % Change", dataset
        );

        // Edits the chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Renderer settings, no shapes and custom line stroke
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(false); // Change to false to hide shapes
        renderer.setDefaultStroke(new BasicStroke(2.0f));

        // Assign distinct colors to each series for better visibility
        int seriesCount = dataset.getRowCount();
        for (int i = 0; i < seriesCount; i++) {
            Color color = getDistinctColor(i, seriesCount);
            renderer.setSeriesPaint(i, color);
        }

        // Format axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(new DecimalFormat("0.0'%'"));

        // Improve the legend
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setPosition(RectangleEdge.RIGHT);

        return chart;
    }

    /**
     * Generates a distinct color based on the index and total number of series.
     * @param index the index of the series
     * @param total the total number of series
     * @return a Color object representing the distinct color
     */
    private static Color getDistinctColor(int index, int total) {
        // Basic color palette
        Color[] baseColors = {
                new Color(31, 119, 180),   // blue
                new Color(255, 127, 14),   // orange
                new Color(44, 160, 44),    // green
                new Color(214, 39, 40),    // red
                new Color(148, 103, 189),  // purple
                new Color(140, 86, 75),    // brown
                new Color(227, 119, 194),  // pink
                new Color(127, 127, 127),  // gray
                new Color(188, 189, 34),   // olive
                new Color(23, 190, 207)    // teal
        };

        // If the index is within the base colors, return the corresponding color
        if (index < baseColors.length) {
            return baseColors[index];
        } else {
            // Generate additional colors if we have more series than base colors
            float hue = (float) index / (float) total;
            return Color.getHSBColor(hue, 0.85f, 0.85f);
        }
    }

    /**
     * Displays the full data for the selected table.
     * @param tableName the name of the table to display
     * @param dataTable the JTable to display the data
     */
    private static void showTableData(String tableName, JTable dataTable) {
        // Get the SQL query for the selected table
        String sqlKey = "full_data_" + tableName;
        String sql = queryMap.get(sqlKey);
        if (sql == null) return;

        logQueryToHistory(sql);
        loadQueryHistory();

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) columnNames.add(meta.getColumnName(i));

            Vector<Vector<Object>> data = new Vector<>();
            Map<String, TimeSeries> seriesMap = new HashMap<>();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) row.add(rs.getObject(i));
                data.add(row);

                int year, month = 1;
                double value;
                String item;

                // Determine the item and value based on the table name
                if (tableName.equalsIgnoreCase("historicalcpi")) {
                    year = rs.getInt("year");
                    value = rs.getDouble("percentChange");
                    item = rs.getString("consumerPriceIndexItem");
                } else if (tableName.equalsIgnoreCase("historicalppi")) {
                    year = rs.getInt("year");
                    value = rs.getDouble("percentChange");
                    item = rs.getString("producerPriceIndexItem");
                } else if (tableName.equalsIgnoreCase("CPIHistoricalForecast")) {
                    String attribute = rs.getString("attribute");
                    if (!attribute.toLowerCase().contains("mid")) continue;
                    year = rs.getInt("yearBeingForecast");
                    month = rs.getInt("monthOfForecast");
                    value = rs.getDouble("forecastPercentChange");
                    item = rs.getString("consumerPriceIndexItem");
                } else if (tableName.equalsIgnoreCase("cpiforecastarchived")) {
                    String attribute = rs.getString("attribute");
                    if (!attribute.toLowerCase().contains("mid")) continue;
                    year = rs.getInt("yearBeingForecast");
                    month = rs.getInt("monthOfForecast");
                    String valStr = rs.getString("forecastPercentChange");
                    try {
                        value = Double.parseDouble(valStr);
                    } catch (NumberFormatException ex) {
                        continue;
                    }
                    item = rs.getString("consumerPriceIndexItem");
                } else if (tableName.equalsIgnoreCase("ppiforecastarchived")) {
                    String attribute = rs.getString("attribute");
                    if (!attribute.toLowerCase().contains("mid")) continue;
                    year = rs.getInt("yearBeingForecast");
                    month = rs.getInt("monthOfForecast");
                    String valStr = rs.getString("forecastPercentChange");
                    try {
                        value = Double.parseDouble(valStr);
                    } catch (NumberFormatException ex) {
                        continue;
                    }
                    item = rs.getString("producerPriceIndexItem");
                } else {
                    String attribute = rs.getString("attribute");
                    if (!attribute.toLowerCase().contains("mid")) continue;
                    year = rs.getInt("yearBeingForecast");
                    month = rs.getInt("monthOfForecast");
                    value = rs.getDouble("forecastPercentChange");

                    if (tableName.toLowerCase().contains("ppi")) {
                        item = rs.getString("producerPriceIndexItem");
                    } else {
                        item = rs.getString("consumerPriceIndexItem");
                    }
                }

                if (item != null) {
                    seriesMap.putIfAbsent(item, new TimeSeries(item));
                    seriesMap.get(item).addOrUpdate(new Month(month, year), value);
                }
            }

            dataTable.setModel(new DefaultTableModel(data, columnNames));
            chartPanel.removeAll();
            checkboxPanel.removeAll();
            categoryCheckboxes.clear();

            if (!seriesMap.isEmpty()) {
                // Set up the checkbox panel with proper layout
                checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));

                // Add a title to the checkbox panel
                JLabel titleLabel = new JLabel("Select Categories");
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
                checkboxPanel.add(titleLabel);

                // Create initial dataset with all series
                TimeSeriesCollection dataset = new TimeSeriesCollection();
                List<String> itemLabels = new ArrayList<>(seriesMap.keySet());
                Collections.sort(itemLabels); // Sort for consistent order

                // Create checkboxes for each series
                for (String label : itemLabels) {
                    JCheckBox box = new JCheckBox(label, true);
                    box.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    box.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    box.setAlignmentX(Component.LEFT_ALIGNMENT);
                    categoryCheckboxes.put(label, box);
                    checkboxPanel.add(box);
                    dataset.addSeries(seriesMap.get(label));
                }

                // Add control buttons in a vertical layout
                JPanel controlPanel = new JPanel();
                controlPanel.setLayout(new GridLayout(3, 1, 5, 5));
                controlPanel.setMaximumSize(new Dimension(150, 100));
                controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JButton toggleAllButton = new JButton("Select All");
                JButton deselectAllButton = new JButton("Deselect All");
                JButton applyButton = new JButton("Apply Filter");

                // Add action listeners to select/deselect buttons
                toggleAllButton.addActionListener(e -> {
                    categoryCheckboxes.values().forEach(cb -> cb.setSelected(true));
                });

                deselectAllButton.addActionListener(e -> {
                    categoryCheckboxes.values().forEach(cb -> cb.setSelected(false));
                });

                // Apply filter based on selected checkboxes
                applyButton.addActionListener(e -> {
                    TimeSeriesCollection filtered = new TimeSeriesCollection();
                    for (String label : categoryCheckboxes.keySet()) {
                        if (categoryCheckboxes.get(label).isSelected()) {
                            filtered.addSeries(seriesMap.get(label));
                        }
                    }

                    // Create chart with filtered dataset
                    JFreeChart filteredChart = createTimeSeriesChart(tableName, filtered);

                    // Refresh and update the chart panel with the new chart
                    chartPanel.removeAll();
                    ChartPanel cp = new ChartPanel(filteredChart);
                    cp.setPreferredSize(new Dimension(700, 400));
                    cp.setMouseWheelEnabled(true);
                    cp.setDomainZoomable(true);
                    cp.setRangeZoomable(true);
                    chartPanel.setLayout(new BorderLayout());
                    chartPanel.add(cp, BorderLayout.CENTER);
                    chartPanel.validate();
                    chartPanel.repaint();
                });

                // Add some spacing before control panel
                checkboxPanel.add(Box.createVerticalStrut(10));
                controlPanel.add(toggleAllButton);
                controlPanel.add(deselectAllButton);
                controlPanel.add(applyButton);
                checkboxPanel.add(controlPanel);

                // Create and show the initial chart
                JFreeChart chart = createTimeSeriesChart(tableName, dataset);

                chartPanel.setLayout(new BorderLayout());
                ChartPanel cp = new ChartPanel(chart);
                cp.setPreferredSize(new Dimension(700, 400));
                cp.setMouseWheelEnabled(true);
                cp.setDomainZoomable(true);
                cp.setRangeZoomable(true);
                chartPanel.add(cp, BorderLayout.CENTER);
            }

            // Refresh the chart and checkbox panels
            chartPanel.revalidate();
            chartPanel.repaint();
            checkboxPanel.revalidate();
            checkboxPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a time series chart with the given title and dataset.
     * @param title the title of the chart
     * @param dataset the dataset to be used for the chart
     * @return a JFreeChart object
     */
    private static JFreeChart createTimeSeriesChart(String title, TimeSeriesCollection dataset) {
        // Create a time series chart with the dataset
        // Documentation found at https://www.jfree.org/jfreechart/api/javadoc/org/jfree/chart/ChartFactory.html
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title + " Midpoint Forecasts", "Time", "% Change", dataset);

        // Customize the chart appearance
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Format y-axis
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setNumberFormatOverride(new DecimalFormat("0.0'%'"));

        // Configure renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false); // Changed to false to hide shapes
        renderer.setDefaultStroke(new BasicStroke(2.0f));

        // Assign distinct colors to each series
        int seriesCount = dataset.getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
            Color color = getDistinctColor(i, seriesCount);
            renderer.setSeriesPaint(i, color);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }

        plot.setRenderer(renderer);

        // Improve the legend
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setPosition(RectangleEdge.RIGHT);

        return chart;
    }

    /**
     * Displays the forecast chart for the selected table.
     * @param tableName the name of the table to display
     * @param dataTable the JTable to display the data
     */
    private static void showForecastChart(String tableName, JTable dataTable) {
        // Get the SQL query for the selected table
        String sqlKey = "full_data_" + tableName + "_for_2025_bounds";
        String sql = queryMap.get(sqlKey);
        if (sql == null) return;

        logQueryToHistory(sql);
        loadQueryHistory();

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) columnNames.add(meta.getColumnName(i));

            Vector<Vector<Object>> data = new Vector<>();
            Map<String, Double> lowerMap = new HashMap<>();
            Map<String, Double> upperMap = new HashMap<>();

            // Iterates through the result set to extract and store data for table display and charting.
            // Adds each row to a table model, then checks for "Percent change" values only.
            // Attempts to extract a display label using 'disaggregate' or fallback hierarchy depending on index type (CPI or PPI).
            // Categorizes and maps forecast values into `lowerMap` and `upperMap` based on whether the attribute is a lower or upper bound.
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) row.add(rs.getObject(i));
                data.add(row);

                String unit = rs.getString("unit");
                if (!"Percent change".equalsIgnoreCase(unit)) continue;

                String attribute = rs.getString("attribute");
                double value = rs.getDouble("value");
                String label;
                try {
                    String disagg = rs.getString("disaggregate");
                    label = (disagg != null && !disagg.isBlank()) ? disagg : rs.getString("midLevel");
                } catch (SQLException ex) {
                    // disaggregate doesn't exist in PPI, so fallback to midLevel or producerPriceIndexItem
                    try {
                        String mid = rs.getString("midLevel");
                        label = (mid != null && !mid.isBlank()) ? mid : rs.getString("producerPriceIndexItem");
                    } catch (SQLException e2) {
                        label = rs.getString("producerPriceIndexItem");
                    }
                }

                if (attribute.toLowerCase().contains("lower")) lowerMap.put(label, value);
                else if (attribute.toLowerCase().contains("upper")) upperMap.put(label, value);
            }

            dataTable.setModel(new DefaultTableModel(data, columnNames));

            chartPanel.removeAll();
            checkboxPanel.removeAll();
            categoryCheckboxes.clear();

            Set<String> labels = new TreeSet<>();
            labels.addAll(lowerMap.keySet());
            labels.addAll(upperMap.keySet());

            // Set up the checkbox panel with proper layout
            checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));

            // Add a title to the checkbox panel
            JLabel titleLabel = new JLabel("Select Categories");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            checkboxPanel.add(titleLabel);

            // Create checkboxes for each item in the data map
            DefaultCategoryDataset initialDataset = new DefaultCategoryDataset();
            for (String item : labels) {
                JCheckBox cb = new JCheckBox(item, true);
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                cb.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                cb.setAlignmentX(Component.LEFT_ALIGNMENT);
                categoryCheckboxes.put(item, cb);
                checkboxPanel.add(cb);

                if (lowerMap.containsKey(item)) initialDataset.addValue(lowerMap.get(item), "Lower Bound", item);
                if (upperMap.containsKey(item)) initialDataset.addValue(upperMap.get(item), "Upper Bound", item);
            }

            // Add control buttons in a vertical layout
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(3, 1, 5, 5));
            controlPanel.setMaximumSize(new Dimension(150, 100));
            controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton toggleAllButton = new JButton("Select All");
            JButton deselectAllButton = new JButton("Deselect All");
            JButton applyButton = new JButton("Apply Filter");

            toggleAllButton.addActionListener(e -> {
                categoryCheckboxes.values().forEach(cb -> cb.setSelected(true));
            });

            deselectAllButton.addActionListener(e -> {
                categoryCheckboxes.values().forEach(cb -> cb.setSelected(false));
            });

            // Apply filter based on selected checkboxes
            applyButton.addActionListener(e -> {
                DefaultCategoryDataset filteredDataset = new DefaultCategoryDataset();
                for (String item : categoryCheckboxes.keySet()) {
                    if (categoryCheckboxes.get(item).isSelected()) {
                        if (lowerMap.containsKey(item)) filteredDataset.addValue(lowerMap.get(item), "Lower Bound", item);
                        if (upperMap.containsKey(item)) filteredDataset.addValue(upperMap.get(item), "Upper Bound", item);
                    }
                }

                JFreeChart filteredChart = createBarChart(tableName, filteredDataset);

                // Refresh and update the chart panel with the new chart
                chartPanel.removeAll();
                ChartPanel cp = new ChartPanel(filteredChart);
                cp.setPreferredSize(new Dimension(700, 400));
                cp.setMouseWheelEnabled(true);
                cp.setDomainZoomable(true);
                cp.setRangeZoomable(true);
                chartPanel.setLayout(new BorderLayout());
                chartPanel.add(cp, BorderLayout.CENTER);
                chartPanel.validate();
                chartPanel.repaint();
            });

            // Add some spacing before control panel
            checkboxPanel.add(Box.createVerticalStrut(10));
            controlPanel.add(toggleAllButton);
            controlPanel.add(deselectAllButton);
            controlPanel.add(applyButton);
            checkboxPanel.add(controlPanel);

            // Create and display the initial chart
            JFreeChart chart = createBarChart(tableName, initialDataset);

            chartPanel.setLayout(new BorderLayout());
            ChartPanel cp = new ChartPanel(chart);
            cp.setPreferredSize(new Dimension(700, 400));
            cp.setMouseWheelEnabled(true);
            cp.setDomainZoomable(true);
            cp.setRangeZoomable(true);
            chartPanel.add(cp, BorderLayout.CENTER);

            chartPanel.validate();
            checkboxPanel.revalidate();
            checkboxPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a bar chart with the given dataset.
     * @param tableName the name of the table to display
     * @param dataset the dataset to be used for the chart
     * @return a JFreeChart object
     */
    private static JFreeChart createBarChart(String tableName, DefaultCategoryDataset dataset) {
        // Create a bar chart with the dataset
        // Documentation found at https://www.jfree.org/jfreechart/api/javadoc/org/jfree/chart/ChartFactory.html
        JFreeChart chart = ChartFactory.createBarChart(
                tableName + " 2025 Prediction Intervals",
                "Item Category", "Percent Change", dataset);

        // Customize the chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Format the domain axis (categories)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        domainAxis.setMaximumCategoryLabelLines(2);

        // Configure the renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(30, 144, 255));    // Blue for lower bound
        renderer.setSeriesPaint(1, new Color(220, 20, 60));     // Red for upper bound
        renderer.setDrawBarOutline(true);
        renderer.setDefaultOutlinePaint(Color.DARK_GRAY);
        renderer.setDefaultOutlineStroke(new BasicStroke(1.0f));
        renderer.setItemMargin(0.1);
        renderer.setShadowVisible(false);

        // Set label generator
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0.0'%'")));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));

        // Format the range axis (values)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(new DecimalFormat("0.0'%'"));

        // Improve the legend
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setPosition(RectangleEdge.RIGHT);

        return chart;
    }

    /**
     * Closes the connection pool to the database.
     */
    private static void closeConnectionPool() {
        if (dataSource != null) dataSource.close();
    }
}