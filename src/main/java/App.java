import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.jxmapviewer.JXMapViewer;
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

    private int counter = 0;

    public  float myLat = 0.0F;
    public  float myLng = 0.0F;

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

        JXMapViewer mymapviewer = mapViewer.getMapViewer();
        mymapviewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point startPoint;
                if (e.getButton() == MouseEvent.BUTTON1) {
                    startPoint = e.getPoint();
                    GeoPosition position = mapViewer.convertPointToGeoPosition(startPoint);
                    System.out.println("Coordonnées : Latitude = " + position.getLatitude() + ", Longitude = " + position.getLongitude());
                    String selectedFromStation = (String) fromComboBox.getSelectedItem();
                    String selectedToStation = (String) toComboBox.getSelectedItem();

// Votre code existant pour récupérer les nouvelles stations...
                    myLat = (float) position.getLatitude();
                    myLng = (float) position.getLongitude();

                    String query = "WITH tables_bus AS\n" +
                            "(SELECT DISTINCT bus.nom_long AS nom, latitude AS lat, longitude AS lng, (ABS(latitude - " + myLat + ") + ABS(longitude - " + myLng + ")) AS distance\n" +
                            "FROM stop_loc, bus\n" +
                            "WHERE bus.type = 'DDD' AND bus.nom_long = stop_loc.name)\n" +
                            "SELECT nom FROM tables_bus WHERE distance = (SELECT MIN(distance) FROM tables_bus);";
                    List<String> stations = new ArrayList<>();

                    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                         Statement statement = connection.createStatement();
                         ResultSet resultSet = statement.executeQuery(query)) {

                        while (resultSet.next()) {
                            String station = resultSet.getString("nom");
                            stations.add(station);
                            System.out.println(station);
                            selectedFromStation = station;
                        }

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

// Ajouter les nouvelles stations à la liste existante des JComboBox
                    for (String station : stations) {
                        fromComboBox.addItem(station);
                        toComboBox.addItem(station);
                        if (counter % 2 == 0 ){
                            selectedFromStation = station;
                            fromComboBox.setSelectedItem(selectedFromStation);
                            counter++;
                        }else{
                            selectedToStation  = station;
                            toComboBox.setSelectedItem(selectedToStation);
                            counter++;
                        }


                    }



// Rétablir l'élément sélectionné par l'utilisateur (s'il existe toujours dans la nouvelle liste)
                    if (selectedFromStation != null && stations.contains(selectedFromStation)) {

                    }

                    if (selectedToStation != null && stations.contains(selectedToStation)) {

                    }

                }
            }
        });
    }

    private void populateComboBoxes() {
        String query = "SELECT DISTINCT nom_long FROM bus WHERE type = 'DDD' ORDER BY nom_long ASC";
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

        assert mode != null;
        if (mode.equals( "DDD") || mode.equals( "AFTU")) {
            if (changes >= 1) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");
                     Statement statement = connection.createStatement()) {
                    String query = "SELECT DISTINCT A.nom_long, A.ligne, B.nom_long " +
                            "FROM bus as A, bus as B " +
                            "WHERE B.nom_long = '" + to + "' AND A.nom_long = '" + from + "' AND A.ligne = B.ligne AND A.type = B.type  AND B.type = '" + mode + "'  ;";
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
                    String query2 = "SELECT distinct A.nom_long, A.ligne, B.nom_long, C.ligne,  D.nom_long \n" +
                            "FROM bus as A, bus as B, bus as C, bus as D\n" +
                            "WHERE A.nom_long = '" + from + "' AND D.nom_long = '" + to + "' AND A.ligne = B.ligne AND B.nom_long = C.nom_long AND C.ligne = D.ligne AND A.ligne <> C.ligne AND A.nom_long <> B.nom_long AND B.nom_long <> D.nom_long AND A.type = B.type AND  C.type = B.type AND B.type = '" + mode + "' ;";

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
                        String changeStation = resultSet.getString("B.nom_long");
                        String line2 = resultSet.getString("C.ligne");
                        String toStation = resultSet.getString("D.nom_long");
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
                    String query3 = "SELECT distinct A.nom_long, A.ligne, B2.nom_long, B2.ligne, C2.nom_long, C2.ligne, D.nom_long \n" +
                            "FROM bus as A, bus as B1, bus as B2, bus as C1, bus as C2, bus as D \n" +
                            "WHERE A.nom_long = '" + from + "'" + " AND A.ligne = B1.ligne AND B1.nom_long = B2.nom_long AND B2.ligne = C1.ligne AND C1.nom_long = C2.nom_long AND C2.ligne = D.ligne AND D.nom_long = '" + to +  "'" +  "AND A.ligne <> B2.ligne AND B2.ligne <> C2.ligne AND A.ligne <> C2.ligne AND A.nom_long <> B1.nom_long AND B2.nom_long <> C1.nom_long AND C2.nom_long <> D.nom_long AND \n" +
                            "A.type = B1.type AND  B2.type = C1.type AND  C2.type = D.type AND D.type = '" + mode + "';";
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
                        String change1Station = resultSet.getString("B2.nom_long");
                        String line2 = resultSet.getString("B2.ligne");
                        String change2Station = resultSet.getString("C2.nom_long");
                        String line3 = resultSet.getString("C2.ligne");
                        String toStation = resultSet.getString("D.nom_long");
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
                    String query2 = "SELECT distinct A.nom_long, A.ligne, B.nom_long, C.ligne,  D.nom_long \n" +
                            "FROM bus as A, bus as B, bus as C, bus as D\n" +
                            "WHERE A.nom_long = '" + from + "' AND D.nom_long = '" + to + "' AND A.ligne = B.ligne AND B.nom_long = C.nom_long AND C.ligne = D.ligne AND A.ligne <> C.ligne AND A.nom_long <> B.nom_long AND B.nom_long <> D.nom_long AND (A.type IN ('AFTU','DDD'))  AND  (B.type IN ('AFTU','DDD'))  AND (C.type IN ('AFTU','DDD'))  AND (D.type IN ('AFTU','DDD')) ;";

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
                        String changeStation = resultSet.getString("B.nom_long");
                        String line2 = resultSet.getString("C.ligne");
                        String toStation = resultSet.getString("D.nom_long");
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
                    String query3 = "SELECT distinct A.nom_long, A.ligne, B2.nom_long, B2.ligne, C2.nom_long, C2.ligne, D.nom_long \n" +
                            "FROM bus as A, bus as B1, bus as B2, bus as C1, bus as C2, bus as D \n" +
                            "WHERE A.nom_long = '" + from + "'" + " AND A.ligne = B1.ligne AND B1.nom_long = B2.nom_long AND B2.ligne = C1.ligne AND C1.nom_long = C2.nom_long AND C2.ligne = D.ligne AND D.nom_long = '" + to +  "'" +  "AND A.ligne <> B2.ligne AND B2.ligne <> C2.ligne AND A.ligne <> C2.ligne AND A.nom_long <> B1.nom_long AND B2.nom_long <> C1.nom_long AND C2.nom_long <> D.nom_long AND \n" +
                            "(A.type IN ('AFTU','DDD')) AND (B1.type IN ('AFTU','DDD')) AND (B2.type IN ('AFTU','DDD')) AND  (C1.type IN ('AFTU','DDD')) AND (C2.type IN ('AFTU','DDD')) AND (D.type IN ('AFTU','DDD')) ;";
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
                        String change1Station = resultSet.getString("B2.nom_long");
                        String line2 = resultSet.getString("B2.ligne");
                        String change2Station = resultSet.getString("C2.nom_long");
                        String line3 = resultSet.getString("C2.ligne");
                        String toStation = resultSet.getString("D.nom_long");
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
