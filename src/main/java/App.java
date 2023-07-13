import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class App extends JFrame {

    private JComboBox<String> fromComboBox;
    private JComboBox<String> toComboBox;
    private JComboBox<Integer> changesComboBox;
    private JButton goButton;
    private JPanel mapPanel;
    private JTable resultTable;

    public App() {
        setTitle("DakarMapper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mettre en plein écran

        // Création des composants
        fromComboBox = new JComboBox<>();
        toComboBox = new JComboBox<>();
        changesComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        goButton = new JButton("GO");
        mapPanel = new JPanel();
        resultTable = new JTable();

        // Ajout d'un gestionnaire de disposition
        setLayout(new BorderLayout());

        // Création du panneau supérieur avec les espaces de saisie et le bouton
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("From: "));
        topPanel.add(fromComboBox);
        topPanel.add(new JLabel("To: "));
        topPanel.add(toComboBox);
        topPanel.add(new JLabel("Changes: "));
        topPanel.add(changesComboBox);
        topPanel.add(goButton);

        // Ajout d'un écouteur de bouton pour le bouton "GO"
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String from = (String) fromComboBox.getSelectedItem();
                String to = (String) toComboBox.getSelectedItem();
                int changes = (int) changesComboBox.getSelectedItem();
                // Effectuer des actions lorsque le bouton est cliqué
                // Utiliser les valeurs "from", "to" et "changes" pour les traitements appropriés
                System.out.println("From: " + from);
                System.out.println("To: " + to);
                System.out.println("Changes: " + changes);
                String query = "SELECT DISTINCT A.nom_long,A.ligne,B.nom_long " +
                        "FROM bus as A, bus as B " +
                        "WHERE B.nom_long = '" + to + "' AND A.nom_long = '" + from + "' AND A.ligne = B.ligne ;";

                if (changes >= 1) {
                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");

                        Statement statement = connection.createStatement();
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

                        resultSet.close();
                        statement.close();
                        connection.close();

                        // Mise à jour de la JTable avec le modèle de table
                        resultTable.setModel(tableModel);

                        // Modification de la taille de la police dans la JTable
                        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                        Font font = renderer.getFont();
                        Font newFont = font.deriveFont(font.getSize() + 2f); // Augmenter la taille de la police
                        resultTable.setFont(newFont);


                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Ajout d'un écouteur pour la liste déroulante "From"
        fromComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateToComboBox();
            }
        });

        // Ajout d'un écouteur pour la liste déroulante "To"
        toComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFromComboBox();
            }
        });

        // Ajout du panneau supérieur et du panneau de la carte à la fenêtre
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);
        add(mapPanel, BorderLayout.SOUTH);

        // Exécution de la requête SQL et peuplement des JComboBox
        populateComboBoxes();

        setVisible(true);
    }

    private void populateComboBoxes() {
        String query = "SELECT DISTINCT nom_long FROM bus ORDER BY nom_long ASC";
        List<String> stations = new ArrayList<>();

        try {
            // Remplacez "yourConnection" par votre propre objet Connection pour établir une connexion à votre base de données
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dakar_mapper", "root", "12345678");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String station = resultSet.getString("nom_long");
                stations.add(station);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DefaultComboBoxModel<String> fromComboBoxModel = new DefaultComboBoxModel<>(stations.toArray(new String[0]));
        fromComboBox.setModel(fromComboBoxModel);

        DefaultComboBoxModel<String> toComboBoxModel = new DefaultComboBoxModel<>(stations.toArray(new String[0]));
        toComboBox.setModel(toComboBoxModel);
    }

    private void updateToComboBox() {
        String selectedFrom = (String) fromComboBox.getSelectedItem();
        String selectedTo = (String) toComboBox.getSelectedItem();
        List<String> stations = getStations();

        DefaultComboBoxModel<String> toComboBoxModel = new DefaultComboBoxModel<>(stations.toArray(new String[0]));
        toComboBox.setModel(toComboBoxModel);

        if (selectedTo != null && stations.contains(selectedTo)) {
            toComboBox.setSelectedItem(selectedTo);
        } else {
            toComboBox.setSelectedIndex(-1);
        }

        if (selectedFrom != null && stations.contains(selectedFrom)) {
            fromComboBox.setSelectedItem(selectedFrom);
        }
    }

    private void updateFromComboBox() {
        String selectedFrom = (String) fromComboBox.getSelectedItem();
        String selectedTo = (String) toComboBox.getSelectedItem();
        List<String> stations = getStations();

        DefaultComboBoxModel<String> fromComboBoxModel = new DefaultComboBoxModel<>(stations.toArray(new String[0]));
        fromComboBox.setModel(fromComboBoxModel);

        if (selectedFrom != null && stations.contains(selectedFrom)) {
            fromComboBox.setSelectedItem(selectedFrom);
        } else {
            fromComboBox.setSelectedIndex(-1);
        }

        if (selectedTo != null && stations.contains(selectedTo)) {
            toComboBox.setSelectedItem(selectedTo);
        }
    }

    private List<String> getStations() {
        List<String> stations = new ArrayList<>();
        DefaultComboBoxModel<String> comboBoxModel = (DefaultComboBoxModel<String>) fromComboBox.getModel();

        for (int i = 0; i < comboBoxModel.getSize(); i++) {
            stations.add(comboBoxModel.getElementAt(i));
        }

        return stations;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
            }
        });
    }
}
