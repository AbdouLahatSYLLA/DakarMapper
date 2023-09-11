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

/*
        * @author     Abdou Lahat SYLLA
        * @version    1.0
        * @since      August 2023
*/

public class App extends JFrame implements ActionListener {

    private final JComboBox<String> fromComboBox;
    private final JComboBox<String> toComboBox;

    private final JComboBox<String> modeComboBox;
    private final JComboBox<Integer> changesComboBox;
    private final JTable resultTable;
    private final MapViewer mapViewer;

    private int counter = 0;

    private  float myLat = 0.0F;
    private  float myLng = 0.0F;





    public App() {
        setTitle("DakarMapper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mettre en plein écran

        // Création des composants
        fromComboBox = new JComboBox<>();
        toComboBox = new JComboBox<>();
        changesComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        modeComboBox = new JComboBox<>(new String[]{"DDD","AFTU","YEUP"});
        JButton goButton = new JButton("GO");
        JButton clearButton = new JButton("Clear");
        JPanel mapPanel = new JPanel(new BorderLayout());
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
        topPanel.add(clearButton);

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
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Efface les marqueurs en vidant la liste des marqueurs
               mapViewer.clear();

            }
        });

        JXMapViewer mymapviewer = mapViewer.getMapViewer();

        resultTable.addMouseListener(new MouseAdapter() {
            String fromStop ;
            String toStop ;

            String changeStop ;

            String changeStop1 ;

            String changeStop2 ;
            String line ;
            String line1 ;

            String line2 ;




            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("click");
                int row = resultTable.getSelectedRow();


                int nb = resultTable.getColumnCount();
                System.out.println("nb : " + nb);

                if(nb == 3) {
                    System.out.println("click in the 3 rows");
                    for (int i = 0; i < nb; i++) {
                        Object val = resultTable.getValueAt(row, i);
                        System.out.println("TableClicked value: " + val);
                        if (i == 0)
                            fromStop = (String) val;
                        if (i == 1)
                            line = (String) val;
                        if (i == 2)
                            toStop = (String) val;

                    }
                    mapViewer.clear();

                    String mode = (String) modeComboBox.getSelectedItem();
                    System.out.println("mode : " + mode);
                    assert mode != null;
                    if (mode.equals("DDD") || mode.equals("AFTU")) {
                        String query = "SELECT route from itineraire where type = '" + mode + "' AND ligne='" + line + "'";


                        String[] itiLigne1;
                        List<String> stationsEntreFromTo = new ArrayList<>();

                        //recuperation des itineraire et traitement pour prendre que les stations qui m'interesse

                        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
                             Statement statement = connection.createStatement();
                             ResultSet resultSet = statement.executeQuery(query)) {

                            while (resultSet.next()) {
                                String route = resultSet.getString("route");
                                System.out.println(route);
                                itiLigne1 = splitted(route, " - ");
                                stationsEntreFromTo = getStationsEntre(fromStop, toStop, itiLigne1);
                            }

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        // recuperer les coord des stations qui m'interessent
                        List<GeoPosition> myTrack = new ArrayList<>();
                        String query2 = "SELECT DISTINCT latitude, longitude FROM stop_loc WHERE name IN (";
                        for (int i = 0; i < stationsEntreFromTo.size(); i++) {
                            query2 += "'" + stationsEntreFromTo.get(i) + "'";
                            if (i < stationsEntreFromTo.size() - 1) {
                                query2 += ",";
                            }
                        }
                        query2 += ");";


                        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
                             Statement statement = connection.createStatement();
                             ResultSet resultSet = statement.executeQuery(query2)) {

                            while (resultSet.next()) {
                                Float lat = resultSet.getFloat("latitude");
                                Float lng = resultSet.getFloat("longitude");

                                myTrack.add(new GeoPosition(lat, lng));
                            }

                            int n = myTrack.size();
                            for (int i = 0; i < n - 1; i++) {
                                mapViewer.addRoute(myTrack.get(i).getLatitude(), myTrack.get(i).getLongitude(),
                                        myTrack.get(i + 1).getLatitude(), myTrack.get(i + 1).getLongitude());
                            }

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }

                }else if (nb == 5){
                    System.out.println("click in the 5 rows");
                    for(int i = 0; i< nb;i++){
                        Object val = resultTable.getValueAt(row, i);
                        System.out.println("TableClicked value: " + val);
                        if(i==0)
                            fromStop = (String) val;

                        if(i==1)
                            line1 = (String) val;
                        if(i==2)
                            changeStop = (String) val;
                        if(i==3)
                            line2 = (String) val;
                        if(i==4)
                            toStop = (String) val;

                    }
                    mapViewer.clear();
                    List<String>  itiLigne1  = new ArrayList<>();



                    //recuperation des itineraire et traitement pour prendre que les stations qui m'interesse

                    itiLigne1.add(fromStop);
                    itiLigne1.add(changeStop);
                    itiLigne1.add(toStop);

                    // recuperer les coord des stations qui m'interessent
                    List<GeoPosition> myTrack = new ArrayList<>();
                    String query2 = "SELECT DISTINCT latitude, longitude FROM stop_loc WHERE name IN (";
                    for (int i = 0; i < itiLigne1.size(); i++) {
                        query2 += "'" + itiLigne1.get(i) + "'";
                        if (i < itiLigne1.size() - 1) {
                            query2 += ",";
                        }
                    }
                    query2 += ");";
                    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
                         Statement statement = connection.createStatement();
                         ResultSet resultSet = statement.executeQuery(query2)) {

                        while (resultSet.next()) {
                            Float lat = resultSet.getFloat("latitude");
                            Float lng = resultSet.getFloat("longitude");

                            myTrack.add(new GeoPosition(lat,lng));
                        }

                        int n = myTrack.size();
                        for (int i = 0; i < n-1 ; i++) {
                            mapViewer.addRoute(myTrack.get(i).getLatitude(),myTrack.get(i).getLongitude(),
                                    myTrack.get(i+1).getLatitude(),myTrack.get(i+1).getLongitude());
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    //nb == 7
                    System.out.println("click in the 7 rows");
                    for(int i = 0; i< nb;i++){
                        Object val = resultTable.getValueAt(row, i);
                        System.out.println("TableClicked value: " + val);
                        if(i==0)
                            fromStop = (String) val;

                        if(i==1)
                            line1 = (String) val;
                        if(i==2)
                            changeStop1 = (String) val;
                        if(i==3)
                            line = (String) val;
                        if(i==4)
                            changeStop2 = (String) val;
                        if(i==5)
                            line2 = (String) val;
                        if(i==6)
                            toStop = (String) val;

                    }
                    mapViewer.clear();
                    List<String>  itiLigne1  = new ArrayList<>();



                    //recuperation des itineraire et traitement pour prendre que les stations qui m'interesse

                    itiLigne1.add(fromStop);
                    itiLigne1.add(changeStop1);
                    itiLigne1.add(changeStop2);
                    itiLigne1.add(toStop);

                    // recuperer les coord des stations qui m'interessent
                    List<GeoPosition> myTrack = new ArrayList<>();
                    String query2 = "SELECT DISTINCT latitude, longitude FROM stop_loc WHERE name IN (";
                    for (int i = 0; i < itiLigne1.size(); i++) {
                        query2 += "'" + itiLigne1.get(i) + "'";
                        if (i < itiLigne1.size() - 1) {
                            query2 += ",";
                        }
                    }
                    query2 += ");";
                    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
                         Statement statement = connection.createStatement();
                         ResultSet resultSet = statement.executeQuery(query2)) {

                        while (resultSet.next()) {
                            Float lat = resultSet.getFloat("latitude");
                            Float lng = resultSet.getFloat("longitude");

                            myTrack.add(new GeoPosition(lat,lng));
                        }

                        int n = myTrack.size();
                        for (int i = 0; i < n-1 ; i++) {
                            mapViewer.addRoute(myTrack.get(i).getLatitude(),myTrack.get(i).getLongitude(),
                                    myTrack.get(i+1).getLatitude(),myTrack.get(i+1).getLongitude());
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

        });

        mymapviewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point startPoint;
                if (e.getButton() == MouseEvent.BUTTON1) {
                    startPoint = e.getPoint();
                    GeoPosition position = mapViewer.convertPointToGeoPosition(startPoint);
                    System.out.println("Coordonnées : Latitude = " + position.getLatitude() + ", Longitude = " + position.getLongitude());
                    String selectedFromStation ;
                    String selectedToStation ;

// Votre code existant pour récupérer les nouvelles stations...
                    myLat = (float) position.getLatitude();
                    myLng = (float) position.getLongitude();

                    mapViewer.setMarker(myLat,myLng);

                    String mode = (String) modeComboBox.getSelectedItem();
                    System.out.println("mode : " + mode);

                    assert mode != null;
                    if(mode.equals("DDD") || mode.equals("AFTU") ){
                        String query = "WITH tables_bus AS\n" +
                                "(SELECT DISTINCT bus.nom_long AS nom, (ST_Distance_Sphere(point(" + myLng + "," + myLat + "), point(stop_loc.longitude, stop_loc.latitude)    ) *.000621371192 ) AS distance\n" +
                                "FROM stop_loc, bus\n" +
                                "WHERE bus.type = '" + mode + "' AND bus.nom_long = stop_loc.name)\n" +
                                "SELECT nom FROM tables_bus WHERE distance = (SELECT MIN(distance) FROM tables_bus);";
                        List<String> stations = new ArrayList<>();

                        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
                             Statement statement = connection.createStatement();
                             ResultSet resultSet = statement.executeQuery(query)) {

                            while (resultSet.next()) {
                                String station = resultSet.getString("nom");
                                stations.add(station);
                                System.out.println(station);

                            }

                            String station  = stations.get(0);


                            if (counter % 2 == 0 ){
                                selectedFromStation = station;
                                fromComboBox.setSelectedItem(selectedFromStation);
                            }else{
                                selectedToStation  = station;
                                toComboBox.setSelectedItem(selectedToStation);
                            }
                            counter ++;

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }else{
                        // AFTU ou DDD
                        String query = "WITH tables_bus AS\n" +
                                "(SELECT DISTINCT bus.nom_long AS nom, (ST_Distance_Sphere(point(" + myLng + "," + myLat + "), point(stop_loc.longitude, stop_loc.latitude)    ) *.000621371192 ) AS distance\n" +
                                "FROM stop_loc, bus\n" +
                                "WHERE  bus.nom_long = stop_loc.name)\n" +
                                "SELECT nom FROM tables_bus WHERE distance = (SELECT MIN(distance) FROM tables_bus);";
                        List<String> stations = new ArrayList<>();

                        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
                             Statement statement = connection.createStatement();
                             ResultSet resultSet = statement.executeQuery(query)) {

                            while (resultSet.next()) {
                                String station = resultSet.getString("nom");
                                stations.add(station);
                                System.out.println(station);

                            }

                            String station  = stations.get(0);



                            if (counter % 2 == 0 ){
                                selectedFromStation = station;
                                fromComboBox.setSelectedItem(selectedFromStation);
                            }else{
                                selectedToStation  = station;
                                toComboBox.setSelectedItem(selectedToStation);
                            }
                            counter ++;

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            }
        });



    }



    private void populateComboBoxes() {
        String query = "SELECT DISTINCT nom_long FROM bus  ORDER BY nom_long ASC";
        List<String> stations = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
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
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
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
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
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
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
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
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
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
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
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
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dakar_mapper", "root", "12345678");
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

    public static List<String> getStationsEntre(String stationA, String stationB, String[] itineraire) {
        List<String> stations = new ArrayList<>();

        // Vérifier si stationA vient avant stationB dans l'itinéraire
        boolean isBefore = false;
        for (String station : itineraire) {
            if (station.equals(stationA)) {
                isBefore = true;
            } else if (station.equals(stationB)) {
                isBefore = false;
                break;
            }
        }

        if (!isBefore) {
            // Inverser l'ordre de stationA et stationB pour l'itinéraire retour
            String temp = stationA;
            stationA = stationB;
            stationB = temp;
        }

        // Ajouter les stations entre stationA et stationB à la liste
        boolean isBetween = false;
        stations.add(stationA);
        for (String station : itineraire) {
            if (station.equals(stationA)) {
                isBetween = true;
            } else if (station.equals(stationB)) {
                isBetween = false;
                break;
            }

            if (isBetween) {
                stations.add(station);
            }
        }
        // Ajouter les stations d'arrivée
        stations.add(stationB);

        return stations;
    }



    public static String[] splitted(String word, String regex){
        return  word.split(regex);
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App().setVisible(true);
            }
        });
    }
}
