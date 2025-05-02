package org.FinalProject;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.*;

public class PriceDataVisualizer {
    private static HikariDataSource dataSource;
    private static Map<String, String> queryMap = new HashMap<>();
    private static JPanel chartPanel;

    public static void main(String[] args) {
        setupConnectionPool();
        try {
            queryMap = loadQueriesFromFile("tableQueries.sql");
        } catch (IOException e) {
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
                    if (currentKey != null && currentQuery.length() > 0) {
                        queries.put(currentKey, currentQuery.toString().trim());
                    }
                    currentKey = line.substring(2).trim();
                    currentQuery = new StringBuilder();
                } else if (!line.isEmpty() && !line.startsWith("#")) {
                    currentQuery.append(line).append(" ");
                }
            }
            if (currentKey != null && currentQuery.length() > 0) {
                queries.put(currentKey, currentQuery.toString().trim());
            }
        }
        return queries;
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

            String sql = switch (tableName) {
                case "CPIForecast" -> queryMap.get("full_data_CPIForecast_for_2025_bounds");
                case "PPIForecast" -> queryMap.get("full_data_PPIForecast_for_2025_bounds");
                default -> "SELECT * FROM " + tableName;
            };

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                Vector<String> columnNames = new Vector<>();
                for (int i = 1; i <= columnCount; i++) columnNames.add(meta.getColumnName(i));

                Vector<Vector<Object>> data = new Vector<>();
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                Map<String, Double> lowerMap = new HashMap<>();
                Map<String, Double> upperMap = new HashMap<>();

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= columnCount; i++) row.add(rs.getObject(i));
                    data.add(row);

                    String unit = rs.getString("unit");
                    if ("Percent change".equalsIgnoreCase(unit)) {
                        String attribute = rs.getString("attribute");
                        double value = rs.getDouble("value");
                        String label;

                        if ("CPIForecast".equalsIgnoreCase(tableName)) {
                            String disagg = rs.getString("disaggregate");
                            String mid = rs.getString("midLevel");
                            label = (disagg != null && !disagg.isBlank()) ? disagg : mid;
                        } else if ("PPIForecast".equalsIgnoreCase(tableName)) {
                            label = rs.getString("producerPriceIndexItem");
                        } else continue;

                        if (attribute.toLowerCase().contains("lower")) lowerMap.put(label, value);
                        else if (attribute.toLowerCase().contains("upper")) upperMap.put(label, value);
                    }
                }

                dataTable.setModel(new DefaultTableModel(data, columnNames));

                if (tableName.equalsIgnoreCase("CPIForecast") || tableName.equalsIgnoreCase("PPIForecast")) {
                    Set<String> labels = new TreeSet<>();
                    labels.addAll(lowerMap.keySet());
                    labels.addAll(upperMap.keySet());

                    for (String item : labels) {
                        if (lowerMap.containsKey(item)) dataset.addValue(lowerMap.get(item), "Lower Bound", item);
                        if (upperMap.containsKey(item)) dataset.addValue(upperMap.get(item), "Upper Bound", item);
                    }

                    JFreeChart chart = ChartFactory.createBarChart(
                            tableName + " 2025 Prediction Intervals",
                            "Item Category", "Percent Change", dataset
                    );

                    chart.setBackgroundPaint(Color.WHITE);
                    chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));

                    CategoryPlot plot = chart.getCategoryPlot();
                    plot.setBackgroundPaint(Color.WHITE);
                    plot.setDomainGridlinePaint(Color.GRAY);
                    plot.setRangeGridlinePaint(Color.GRAY);

                    CategoryAxis domainAxis = plot.getDomainAxis();
                    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
                    domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
                    domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

                    BarRenderer renderer = (BarRenderer) plot.getRenderer();
                    renderer.setSeriesPaint(0, new Color(30, 144, 255));  // Dodger Blue
                    renderer.setSeriesPaint(1, new Color(220, 20, 60));   // Crimson Red
                    renderer.setBarPainter(new BarRenderer().getBarPainter());
                    renderer.setDrawBarOutline(false);
                    renderer.setItemMargin(0.1);
                    renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new java.text.DecimalFormat("0.0'%'") ));
                    renderer.setDefaultItemLabelsVisible(true);
                    renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 10));

                    chartPanel.removeAll();
                    chartPanel.setLayout(new BorderLayout());
                    chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
                    chartPanel.validate();
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
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
        chartPanel.setBackground(Color.LIGHT_GRAY);
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        JScrollPane dataScrollPane = new JScrollPane(dataTable);
        dataScrollPane.setPreferredSize(new Dimension(700, 200));
        centerPanel.add(dataScrollPane, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setPreferredSize(new Dimension(250, 0));
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));
        historyPanel.add(new JScrollPane(new JList<>(new DefaultListModel<>())), BorderLayout.CENTER);

        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Description"));
        historyPanel.add(descriptionArea, BorderLayout.SOUTH);
        mainPanel.add(historyPanel, BorderLayout.EAST);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private static void closeConnectionPool() {
        if (dataSource != null) dataSource.close();
    }
}
