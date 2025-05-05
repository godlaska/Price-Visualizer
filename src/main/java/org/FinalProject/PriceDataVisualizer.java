package org.FinalProject;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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

public class PriceDataVisualizer {
    private static HikariDataSource dataSource;
    private static Map<String, String> queryMap = new HashMap<>();
    private static JPanel chartPanel;
    private static JPanel checkboxPanel;
    private static final Map<String, JCheckBox> categoryCheckboxes =
            new LinkedHashMap<>();

    public static void main(String[] args) {
        setupConnectionPool();
        try {
            queryMap = loadQueriesFromFile("tableQueries.sql");
        } catch (IOException e) {
            System.err.println("Failed to load queries from file: tableQueries.sql");
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(PriceDataVisualizer::closeConnectionPool));
        SwingUtilities.invokeLater(PriceDataVisualizer::createAndShowGUI);
    }

    private static void setupConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/foodprices");
        config.setUsername("root");
        config.setPassword("pass");
        config.setMaximumPoolSize(5);
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static Map<String, String> loadQueriesFromFile(String filePath) throws IOException {
        Map<String, String> queries = new HashMap<>();
        StringBuilder currentQuery = new StringBuilder();
        String currentKey = null;

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
                } else if (!line.isEmpty() && !line.startsWith("#")) {
                    currentQuery.append(line).append(" ");
                }
            }
            if (currentKey != null && !currentQuery.isEmpty()) {
                queries.put(currentKey, currentQuery.toString().trim());
            }
        }
        return queries;
    }

    private static DefaultListModel<String> historyModel = new DefaultListModel<>();
    private static JList<String> historyList = new JList<>(historyModel);

    public static void logQueryToHistory(String sql) {
        String insert = "INSERT INTO query_history (query_text) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadQueryHistory() {
        historyModel.clear();
        String select = "SELECT query_text FROM query_history ORDER BY run_timestamp DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {
            while (rs.next()) {
                String query = rs.getString("query_text").trim();
                if (!query.isEmpty()) historyModel.addElement(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Price Data Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> typeSelector = new JComboBox<>(new String[]{"Consumer Price Index", "Producer Price Index"});
        JComboBox<String> querySelector = new JComboBox<>(new String[]{"", "Show Full Data"});
        JComboBox<String> tableSelector = new JComboBox<>();
        tableSelector.setVisible(false);
        JTable dataTable = new JTable();

        String[] cpiTables = {"CPIForecast", "CPIHistoricalForecast", "historicalcpi", "cpiforecastarchived"};
        String[] ppiTables = {"PPIForecast", "PPIHistoricalForecast", "historicalppi", "ppiforecastarchived"};

        ActionListener refreshTableSelector = e -> {
            String selectedQuery = (String) querySelector.getSelectedItem();
            String selectedType = (String) typeSelector.getSelectedItem();
            if ("Show Full Data".equals(selectedQuery)) {
                tableSelector.removeAllItems();
                String[] tables = selectedType.equals("Consumer Price Index") ? cpiTables : ppiTables;
                for (String t : tables) tableSelector.addItem(t);
                tableSelector.setVisible(true);
                tableSelector.setSelectedIndex(0);
            } else {
                tableSelector.setVisible(false);
            }
        };

        querySelector.addActionListener(refreshTableSelector);
        typeSelector.addActionListener(refreshTableSelector);

        tableSelector.addActionListener(e -> {
            if (!tableSelector.isVisible()) return;
            String tableName = (String) tableSelector.getSelectedItem();
            if (tableName == null || tableName.isEmpty()) return;

            if (tableName.equals("CPIForecast") || tableName.equals("PPIForecast")) {
                showForecastChart(tableName, dataTable);
            } else {
                showTableData(tableName, dataTable);
            }
        });

        topPanel.add(new JLabel("Select Data Type: "));
        topPanel.add(typeSelector);
        topPanel.add(new JLabel("Select Query: "));
        topPanel.add(querySelector);
        topPanel.add(new JLabel("Select Table: "));
        topPanel.add(tableSelector);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        chartPanel = new JPanel();
        chartPanel.setPreferredSize(new Dimension(700, 400));
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        JScrollPane dataScrollPane = new JScrollPane(dataTable);
        dataScrollPane.setPreferredSize(new Dimension(700, 200));
        centerPanel.add(dataScrollPane, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBorder(BorderFactory.createTitledBorder("Select Categories"));
        JScrollPane checkboxScrollPane = new JScrollPane(checkboxPanel);
        checkboxScrollPane.setPreferredSize(new Dimension(200, 400));
        centerPanel.add(checkboxScrollPane, BorderLayout.WEST);

        // Right side history panel setup
        loadQueryHistory();
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

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setPreferredSize(new Dimension(300, 0));
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        JButton clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearHistoryButton.setBackground(Color.PINK);
        clearHistoryButton.addActionListener(e -> {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM query_history");
                historyModel.clear();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        JPanel bottomHistoryPanel = new JPanel(new BorderLayout());
        bottomHistoryPanel.setPreferredSize(new Dimension(300, 200));
        bottomHistoryPanel.add(clearHistoryButton, BorderLayout.NORTH);

        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Description"));
        descriptionArea.setPreferredSize(new Dimension(300, 150));
        bottomHistoryPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        historyPanel.add(bottomHistoryPanel, BorderLayout.SOUTH);
        mainPanel.add(historyPanel, BorderLayout.EAST);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private static void refreshTables(JComboBox<String> typeSelector, JComboBox<String> querySelector, JComboBox<String> tableSelector, String[] cpiTables, String[] ppiTables) {
        String selectedQuery = (String) querySelector.getSelectedItem();
        String selectedType = (String) typeSelector.getSelectedItem();
        if ("Show Full Data".equals(selectedQuery)) {
            tableSelector.removeAllItems();
            String[] tables = selectedType.equals("Consumer Price Index") ? cpiTables : ppiTables;
            for (String t : tables) tableSelector.addItem(t);
            tableSelector.setVisible(true);
            tableSelector.setSelectedIndex(0);
        } else {
            tableSelector.setVisible(false);
        }
    }

    private static void showTableData(String tableName, JTable dataTable) {
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
                TimeSeriesCollection dataset = new TimeSeriesCollection();
                for (String label : seriesMap.keySet()) {
                    JCheckBox box = new JCheckBox(label, true);
                    box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    box.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                    categoryCheckboxes.put(label, box);
                    checkboxPanel.add(box);
                    dataset.addSeries(seriesMap.get(label));
                }

                JButton toggleAllButton = new JButton("Select/Deselect All");
                toggleAllButton.addActionListener(e -> {
                    boolean allSelected = categoryCheckboxes.values().stream().allMatch(AbstractButton::isSelected);
                    for (JCheckBox cb : categoryCheckboxes.values()) {
                        cb.setSelected(!allSelected);
                    }
                });
                checkboxPanel.add(toggleAllButton, 0);

                JButton applyButton = new JButton("Apply Filter");
                applyButton.addActionListener(e -> {
                    TimeSeriesCollection filtered = new TimeSeriesCollection();
                    for (String label : categoryCheckboxes.keySet()) {
                        if (categoryCheckboxes.get(label).isSelected()) {
                            filtered.addSeries(seriesMap.get(label));
                        }
                    }
                    JFreeChart filteredChart = ChartFactory.createTimeSeriesChart(
                            tableName + " Midpoint Forecasts", "Time", "% Change", filtered);
                    XYPlot plot = filteredChart.getXYPlot();
                    plot.setBackgroundPaint(Color.WHITE);
                    plot.setDomainGridlinePaint(Color.GRAY);
                    plot.setRangeGridlinePaint(Color.GRAY);
                    NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
                    yAxis.setNumberFormatOverride(new DecimalFormat("0.0'%'"));
                    plot.setRenderer(new XYLineAndShapeRenderer(true, false));

                    chartPanel.removeAll();
                    chartPanel.setLayout(new BorderLayout());
                    chartPanel.add(new ChartPanel(filteredChart), BorderLayout.CENTER);
                    chartPanel.validate();
                });
                checkboxPanel.add(applyButton);

                JFreeChart chart = ChartFactory.createTimeSeriesChart(
                        tableName + " Midpoint Forecasts", "Time", "% Change", dataset);
                XYPlot plot = chart.getXYPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setDomainGridlinePaint(Color.GRAY);
                plot.setRangeGridlinePaint(Color.GRAY);
                NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
                yAxis.setNumberFormatOverride(new DecimalFormat("0.0'%'"));
                plot.setRenderer(new XYLineAndShapeRenderer(true, false));

                chartPanel.setLayout(new BorderLayout());
                chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
            }

            chartPanel.revalidate();
            chartPanel.repaint();
            checkboxPanel.revalidate();
            checkboxPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showForecastChart(String tableName, JTable dataTable) {
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

            DefaultCategoryDataset initialDataset = new DefaultCategoryDataset();
            for (String item : labels) {
                JCheckBox cb = new JCheckBox(item, true);
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cb.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                categoryCheckboxes.put(item, cb);
                checkboxPanel.add(cb);

                if (lowerMap.containsKey(item)) initialDataset.addValue(lowerMap.get(item), "Lower Bound", item);
                if (upperMap.containsKey(item)) initialDataset.addValue(upperMap.get(item), "Upper Bound", item);
            }

            JButton toggleAllButton = new JButton("Select/Deselect All");
            toggleAllButton.addActionListener(e -> {
                boolean allSelected = categoryCheckboxes.values().stream().allMatch(AbstractButton::isSelected);
                for (JCheckBox cb : categoryCheckboxes.values()) cb.setSelected(!allSelected);
            });
            checkboxPanel.add(toggleAllButton, 0);

            JButton applyButton = new JButton("Apply Filter");
            applyButton.addActionListener(e -> {
                DefaultCategoryDataset filteredDataset = new DefaultCategoryDataset();
                for (String item : categoryCheckboxes.keySet()) {
                    if (categoryCheckboxes.get(item).isSelected()) {
                        if (lowerMap.containsKey(item)) filteredDataset.addValue(lowerMap.get(item), "Lower Bound", item);
                        if (upperMap.containsKey(item)) filteredDataset.addValue(upperMap.get(item), "Upper Bound", item);
                    }
                }
                JFreeChart filteredChart = ChartFactory.createBarChart(
                        tableName + " 2025 Prediction Intervals",
                        "Item Category", "Percent Change", filteredDataset);
                CategoryPlot plot = filteredChart.getCategoryPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setDomainGridlinePaint(Color.GRAY);
                plot.setRangeGridlinePaint(Color.GRAY);
                CategoryAxis domainAxis = plot.getDomainAxis();
                domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
                domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
                domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
                BarRenderer renderer = (BarRenderer) plot.getRenderer();
                renderer.setSeriesPaint(0, new Color(30, 144, 255));
                renderer.setSeriesPaint(1, new Color(220, 20, 60));
                renderer.setDrawBarOutline(false);
                renderer.setItemMargin(0.1);
                renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0.0'%'") ));
                renderer.setDefaultItemLabelsVisible(true);
                renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 10));
                chartPanel.removeAll();
                chartPanel.setLayout(new BorderLayout());
                chartPanel.add(new ChartPanel(filteredChart), BorderLayout.CENTER);
                chartPanel.validate();
            });
            checkboxPanel.add(applyButton);

            JFreeChart chart = ChartFactory.createBarChart(
                    tableName + " 2025 Prediction Intervals",
                    "Item Category", "Percent Change", initialDataset);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.GRAY);
            plot.setRangeGridlinePaint(Color.GRAY);
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
            domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(30, 144, 255));
            renderer.setSeriesPaint(1, new Color(220, 20, 60));
            renderer.setDrawBarOutline(false);
            renderer.setItemMargin(0.1);
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0.0'%'") ));
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 10));

            chartPanel.setLayout(new BorderLayout());
            chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
            chartPanel.validate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void closeConnectionPool() {
        if (dataSource != null) dataSource.close();
    }
}
