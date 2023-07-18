import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class App extends JFrame implements ActionListener {

    private JComboBox<String> fromComboBox;
    private JComboBox<String> toComboBox;

    private JComboBox<String> modeComboBox;
    private JComboBox<Integer> changesComboBox;
    private JButton goButton;
    private JPanel mapPanel;
    private JTable resultTable;
    private MapViewer mapViewer;

    public App() {
        setTitle("DakarMapper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mettre en plein écran

        // Création des composants
        fromComboBox = new JComboBox<>();
        toComboBox = new JComboBox<>();
        changesComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        modeComboBox = new JComboBox<>(new String[]{"DDD","AFTU","YEUP"});
        goButton = new JButton("GO");
        mapPanel = new JPanel(new BorderLayout());
        resultTable = new JTable();
        mapViewer = new MapViewer();

        // Création du panneau supérieur avec les espaces de saisie et le bouton
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("From: "));
        topPanel.add(fromComboBox);
        topPanel.add(new JLabel("To: "));
        topPanel.add(toComboBox);
        topPanel.add(new JLabel("Changes: "));
        topPanel.add(changesComboBox);
        topPanel.add(new JLabel("Mode: "));
        topPanel.add(modeComboBox);
        topPanel.add(goButton);

        // Ajout d'un gestionnaire de disposition
        setLayout(new BorderLayout());

        // Création du JSplitPane pour diviser la fenêtre
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapPanel, new JScrollPane(resultTable));
        splitPane.setResizeWeight(0.88); // Définir la proportion de redimensionnement (90% pour la carte)

        // Ajout des composants à la fenêtre
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Ajout du MapViewer au panneau de la carte
        mapPanel.add(mapViewer, BorderLayout.CENTER);

        // Exécution de la requête SQL et peuplement des JComboBox
        populateComboBoxes();

        // Ajout d'un écouteur de bouton pour le bouton "GO"
        goButton.addActionListener(this);
    }

    private void populateComboBoxes() {
        String query = "SELECT DISTINCT nom_long FROM bus ORDER BY nom_long ASC";
        List<String> stations = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String station = resultSet.getString("nom_long");
                stations.add(station);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        DefaultComboBoxModel<String> fromComboBoxModel = new DefaultComboBoxModel<>(stations.toArray(new String[0]));
        fromComboBox.setModel(fromComboBoxModel);

        DefaultComboBoxModel<String> toComboBoxModel = new DefaultComboBoxModel<>(stations.toArray(new String[0]));
        toComboBox.setModel(toComboBoxModel);
    }

    public void actionPerformed(ActionEvent e) {
        String from = (String) fromComboBox.getSelectedItem();
        String to = (String) toComboBox.getSelectedItem();
        int changes = (int) changesComboBox.getSelectedItem();

        String mode = (String) modeComboBox.getSelectedItem();
        System.out.println("Mode: " + mode);
        // Effectuer des actions lorsque le bouton est cliqué
        // Utiliser les valeurs "from", "to" et "changes" pour les traitements appropriés
        System.out.println("From: " + from);
        System.out.println("To: " + to);
        System.out.println("Changes: " + changes);

        if (mode.equals( "DDD") || mode.equals( "AFTU")) {
            if (changes >= 1) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                     Statement statement = connection.createStatement()) {
                    String query = "SELECT DISTINCT A.nom_long, A.ligne, B.nom_long " +
                            "FROM bus as A, bus as B " +
                            "WHERE B.nom_long = '" + to + "' AND A.nom_long = '" + from + "' AND A.ligne = B.ligne AND A.type = '" + mode + "' AND B.type = '" + mode + "'  ;";
                    ResultSet resultSet = statement.executeQuery(query);

                    // Création du modèle de table pour les résultats
                    DefaultTableModel tableModel = new DefaultTableModel();
                    tableModel.addColumn("From");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("To");

                    // Ajout des résultats au modèle de table
                    while (resultSet.next()) {
                        String fromStation = resultSet.getString("A.nom_long");
                        String line = resultSet.getString("A.ligne");
                        String toStation = resultSet.getString("B.nom_long");
                        tableModel.addRow(new Object[]{fromStation, line, toStation});
                    }

                    // Mise à jour de la JTable avec le modèle de table
                    resultTable.setModel(tableModel);

                    // Modification de la taille de la police dans la JTable
                    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                    Font font = renderer.getFont();
                    Font newFont = font.deriveFont(font.getSize() + 1f); // Augmenter la taille de la police
                    resultTable.setFont(newFont);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (changes >= 2) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                     Statement statement = connection.createStatement()) {
                    String query2 = "SELECT DISTINCT A.nom_long, A.ligne, B1.nom_long AS change_station, B2.ligne, B2.nom_long AS to_station\n" +
                            "FROM bus AS A, bus AS B1, bus AS B2\n" +
                            "WHERE A.nom_long = '" + from + "' AND B2.nom_long = '" + to + "' AND A.ligne = B1.ligne AND B1.nom_long = B2.nom_long AND A.type = '" + mode + "' AND B1.type = '" + mode + "' AND B2.type = '" + mode + "'  ;";
                    ResultSet resultSet = statement.executeQuery(query2);

                    // Création du modèle de table pour les résultats
                    DefaultTableModel tableModel = new DefaultTableModel();
                    tableModel.addColumn("From");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("Change");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("To");

                    // Ajout des résultats au modèle de table
                    while (resultSet.next()) {
                        String fromStation = resultSet.getString("A.nom_long");
                        String line1 = resultSet.getString("A.ligne");
                        String changeStation = resultSet.getString("change_station");
                        String line2 = resultSet.getString("B2.ligne");
                        String toStation = resultSet.getString("to_station");
                        tableModel.addRow(new Object[]{fromStation, line1, changeStation, line2, toStation});
                    }

                    // Mise à jour de la JTable avec le modèle de table
                    resultTable.setModel(tableModel);

                    // Modification de la taille de la police dans la JTable
                    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                    Font font = renderer.getFont();
                    Font newFont = font.deriveFont(font.getSize() + 1f); // Augmenter la taille de la police
                    resultTable.setFont(newFont);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (changes >= 3) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                     Statement statement = connection.createStatement()) {
                    String query3 = "SELECT DISTINCT A.nom_long, A.ligne, B1.nom_long AS change1_station, B2.ligne, B2.nom_long AS change2_station, B3.ligne, B3.nom_long AS to_station\n" +
                            "FROM bus AS A, bus AS B1, bus AS B2, bus AS B3\n" +
                            "WHERE A.nom_long = '" + from + "' AND B3.nom_long = '" + to + "' AND A.ligne = B1.ligne AND B1.nom_long = B2.nom_long AND B2.ligne = B3.ligne AND A.type = '" + mode + "' AND B1.type = '" + mode + "' AND B2.type = '" + mode + "' AND B3.type = '" + mode + "'  ;";
                    ResultSet resultSet = statement.executeQuery(query3);

                    // Création du modèle de table pour les résultats
                    DefaultTableModel tableModel = new DefaultTableModel();
                    tableModel.addColumn("From");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("Change 1");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("Change 2");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("To");

                    // Ajout des résultats au modèle de table
                    while (resultSet.next()) {
                        String fromStation = resultSet.getString("A.nom_long");
                        String line1 = resultSet.getString("A.ligne");
                        String change1Station = resultSet.getString("change1_station");
                        String line2 = resultSet.getString("B2.ligne");
                        String change2Station = resultSet.getString("change2_station");
                        String line3 = resultSet.getString("B3.ligne");
                        String toStation = resultSet.getString("to_station");
                        tableModel.addRow(new Object[]{fromStation, line1, change1Station, line2, change2Station, line3, toStation});
                    }

                    // Mise à jour de la JTable avec le modèle de table
                    resultTable.setModel(tableModel);

                    // Modification de la taille de la police dans la JTable
                    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                    Font font = renderer.getFont();
                    Font newFont = font.deriveFont(font.getSize() + 1f); // Augmenter la taille de la police
                    resultTable.setFont(newFont);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            if (changes >= 1) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                     Statement statement = connection.createStatement()) {
                    String query = "SELECT DISTINCT A.nom_long, A.ligne, B.nom_long " +
                            "FROM bus as A, bus as B " +
                            "WHERE B.nom_long = '" + to + "' AND A.nom_long = '" + from + "' AND A.ligne = B.ligne AND (A.type = 'AFTU' OR A.type = 'DDD') AND (B.type = 'AFTU' OR B.type = 'DDD')  ;";
                    ResultSet resultSet = statement.executeQuery(query);

                    // Création du modèle de table pour les résultats
                    DefaultTableModel tableModel = new DefaultTableModel();
                    tableModel.addColumn("From");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("To");

                    // Ajout des résultats au modèle de table
                    while (resultSet.next()) {
                        String fromStation = resultSet.getString("A.nom_long");
                        String line = resultSet.getString("A.ligne");
                        String toStation = resultSet.getString("B.nom_long");
                        tableModel.addRow(new Object[]{fromStation, line, toStation});
                    }

                    // Mise à jour de la JTable avec le modèle de table
                    resultTable.setModel(tableModel);

                    // Modification de la taille de la police dans la JTable
                    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                    Font font = renderer.getFont();
                    Font newFont = font.deriveFont(font.getSize() + 1f); // Augmenter la taille de la police
                    resultTable.setFont(newFont);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (changes >= 2) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                     Statement statement = connection.createStatement()) {
                    String query2 = "SELECT DISTINCT A.nom_long, A.ligne, B1.nom_long AS change_station, B2.ligne, B2.nom_long AS to_station\n" +
                            "FROM bus AS A, bus AS B1, bus AS B2\n" +
                            "WHERE A.nom_long = '" + from + "' AND B2.nom_long = '" + to + "' AND A.ligne = B1.ligne AND B1.nom_long = B2.nom_long AND (A.type = 'AFTU' OR A.type = 'DDD') AND (B1.type = 'AFTU' OR B1.type = 'DDD') AND (B2.type = 'AFTU' OR B2.type = 'DDD') ;";
                    ResultSet resultSet = statement.executeQuery(query2);

                    // Création du modèle de table pour les résultats
                    DefaultTableModel tableModel = new DefaultTableModel();
                    tableModel.addColumn("From");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("Change");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("To");

                    // Ajout des résultats au modèle de table
                    while (resultSet.next()) {
                        String fromStation = resultSet.getString("A.nom_long");
                        String line1 = resultSet.getString("A.ligne");
                        String changeStation = resultSet.getString("change_station");
                        String line2 = resultSet.getString("B2.ligne");
                        String toStation = resultSet.getString("to_station");
                        tableModel.addRow(new Object[]{fromStation, line1, changeStation, line2, toStation});
                    }

                    // Mise à jour de la JTable avec le modèle de table
                    resultTable.setModel(tableModel);

                    // Modification de la taille de la police dans la JTable
                    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                    Font font = renderer.getFont();
                    Font newFont = font.deriveFont(font.getSize() + 1f); // Augmenter la taille de la police
                    resultTable.setFont(newFont);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (changes >= 3) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                     Statement statement = connection.createStatement()) {
                    String query3 = "SELECT DISTINCT A.nom_long, A.ligne, B1.nom_long AS change1_station, B2.ligne, B2.nom_long AS change2_station, B3.ligne, B3.nom_long AS to_station\n" +
                            "FROM bus AS A, bus AS B1, bus AS B2, bus AS B3\n" +
                            "WHERE A.nom_long = '" + from + "' AND B3.nom_long = '" + to + "' AND A.ligne = B1.ligne AND B1.nom_long = B2.nom_long AND B2.ligne = B3.ligne AND (A.type = 'AFTU' OR A.type = 'DDD') AND (B1.type = 'AFTU' OR B1.type = 'DDD') AND (B2.type = 'AFTU' OR B2.type = 'DDD') AND (B3.type = 'AFTU' OR B3.type = 'DDD') ;";
                    ResultSet resultSet = statement.executeQuery(query3);

                    // Création du modèle de table pour les résultats
                    DefaultTableModel tableModel = new DefaultTableModel();
                    tableModel.addColumn("From");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("Change 1");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("Change 2");
                    tableModel.addColumn("Line");
                    tableModel.addColumn("To");

                    // Ajout des résultats au modèle de table
                    while (resultSet.next()) {
                        String fromStation = resultSet.getString("A.nom_long");
                        String line1 = resultSet.getString("A.ligne");
                        String change1Station = resultSet.getString("change1_station");
                        String line2 = resultSet.getString("B2.ligne");
                        String change2Station = resultSet.getString("change2_station");
                        String line3 = resultSet.getString("B3.ligne");
                        String toStation = resultSet.getString("to_station");
                        tableModel.addRow(new Object[]{fromStation, line1, change1Station, line2, change2Station, line3, toStation});
                    }

                    // Mise à jour de la JTable avec le modèle de table
                    resultTable.setModel(tableModel);

                    // Modification de la taille de la police dans la JTable
                    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                    Font font = renderer.getFont();
                    Font newFont = font.deriveFont(font.getSize() + 1f); // Augmenter la taille de la police
                    resultTable.setFont(newFont);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App().setVisible(true);
            }
        });
    }
}
