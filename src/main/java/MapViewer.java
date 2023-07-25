import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.jxmapviewer.painter.CompoundPainter;


public class MapViewer extends JPanel {

    private final JXMapViewer mapViewer;
    private Point startPoint;
    private Set<Waypoint> waypoints;

    private RoutePainter routePainter;


    private final double moveScale = 0.0001; // Facteur d'échelle pour le déplacement plus lent
    private WaypointPainter<Waypoint> waypointPainter;
    private List<GeoPosition> linePoints = new ArrayList<>();


    public MapViewer() {
        setLayout(new BorderLayout());
        routePainter = new RoutePainter(new ArrayList<>());

        // Création du JXMapViewer
        mapViewer = new JXMapViewer();
        mapViewer.setZoom(25); // Zoom initial pour Paris

        waypoints = new HashSet<>();
         waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(waypointPainter);
        // Configuration de la tuile d'usine pour OpenStreetMap
        TileFactoryInfo info = new TileFactoryInfo(1, 17, 17, 256, true, true,
                "https://a.tile.openstreetmap.org/",
                "x", "y", "z") {
            public String getTileUrl(int x, int y, int zoom) {
                zoom = 17 - zoom;
                String url = this.baseURL + zoom + "/" + x + "/" + y + ".png";
                return url;
            }
        };
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Ajout du JXMapViewer au panneau
        add(mapViewer, BorderLayout.CENTER);

        // Ajout d'un écouteur de souris pour détecter les clics sur la carte
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    startPoint = e.getPoint();
                }
            }


            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    startPoint = null;
                }
            }
        });

        // Ajout d'un écouteur de la souris pour déplacer la carte
        mapViewer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startPoint != null) {
                    Point endPoint = e.getPoint();
                    int dx = startPoint.x - endPoint.x; // Inversion du sens du déplacement
                    int dy = startPoint.y - endPoint.y; // Inversion du sens du déplacement
                    startPoint = endPoint;

                    GeoPosition currentCenter = mapViewer.getCenterPosition();
                    double latitude = currentCenter.getLatitude() + dy * moveScale; // Inversion du sens du déplacement
                    double longitude = currentCenter.getLongitude() + dx * moveScale; // Inversion du sens du déplacement
                    mapViewer.setCenterPosition(new GeoPosition(latitude, longitude));
                }
            }
        });

        // Ajout d'un écouteur de la molette de la souris pour gérer le zoom
        mapViewer.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                int currentZoom = mapViewer.getZoom();
                if (rotation < 0) {
                    mapViewer.setZoom(currentZoom - 1);
                } else {
                    mapViewer.setZoom(currentZoom + 1);
                }
            }
        });

        // Ajout des boutons "+" et "-"
        JButton zoomInButton = new JButton("+");
        zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentZoom = mapViewer.getZoom();
                mapViewer.setZoom(currentZoom - 1);
            }
        });

        JButton zoomOutButton = new JButton("-");
        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentZoom = mapViewer.getZoom();
                mapViewer.setZoom(currentZoom + 1);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(zoomInButton);
        buttonPanel.add(zoomOutButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Centre la carte sur Dakar
        double dakarLatitude = 14.6927;
        double dakarLongitude = -17.4467;
        mapViewer.setCenterPosition(new GeoPosition(dakarLatitude, dakarLongitude));
    }


    public void addRoute(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        // Met à jour les points de départ et d'arrivée
        GeoPosition startPoint = new GeoPosition(startLatitude, startLongitude);
        GeoPosition endPoint = new GeoPosition(endLatitude, endLongitude);

        // Ajoute les marqueurs de départ et d'arrivée au WaypointPainter
        Set<Waypoint> waypoints = new HashSet<>();
        waypoints.add(new DefaultWaypoint(startPoint));
        waypoints.add(new DefaultWaypoint(endPoint));
        waypointPainter.setWaypoints(waypoints);

        // Créez une liste pour stocker les points pour la ligne
        List<GeoPosition> linePoints = new ArrayList<>();
        linePoints.add(startPoint);
        linePoints.add(endPoint);

        // Créez une instance de RoutePainter en passant la liste de points pour la ligne
        routePainter.setRoute(linePoints);

        // Ajoutez le RoutePainter au JXMapViewer
        mapViewer.setOverlayPainter(routePainter);

        // Rafraîchit le JXMapViewer pour afficher les marqueurs et la ligne
        mapViewer.repaint();
    }


    // La method setMarker reste inchangée, sauf la déclaration de la variable waypoints
    public void setMarker(double latitude, double longitude) {
        // Crée un nouveau marqueur pour la position spécifiée
        Waypoint newWaypoint = new DefaultWaypoint(new GeoPosition(latitude, longitude));

        // Ajoute le nouveau marqueur à la liste des marqueurs existants
        waypoints.add(newWaypoint);

        // Met à jour les marqueurs dans le waypointPainter existant
        waypointPainter.setWaypoints(waypoints);

        // Rafraîchit la carte pour afficher le nouveau marqueur
        mapViewer.repaint();
    }



    public void clear() {
        waypoints.clear(); // Efface tous les marqueurs en vidant la liste des marqueurs
        routePainter.clear();
        // Crée un nouveau WaypointPainter sans marqueurs
        WaypointPainter<Waypoint> newWaypointPainter = new WaypointPainter<>();
        newWaypointPainter.setWaypoints(waypoints);

        // Affecte le nouveau WaypointPainter au mapViewer
        mapViewer.setOverlayPainter(newWaypointPainter);

        // Rafraîchit la carte pour appliquer les changements
        mapViewer.repaint();
    }

    public JXMapViewer getMapViewer(){
        return  this.mapViewer;
    }


    public GeoPosition convertPointToGeoPosition(Point point) {
        int zoom = mapViewer.getZoom();
        int tileSize = mapViewer.getTileFactory().getTileSize(zoom);

        // Get the current center GeoPosition and the top-left pixel position of the map
        GeoPosition centerGeoPos = mapViewer.getCenterPosition();
        Point2D centerPoint = mapViewer.getTileFactory().geoToPixel(centerGeoPos, zoom);

        // Get the clicked pixel position relative to the center of the map
        double x = point.getX() - ((double) getWidth() / 2);
        double y = point.getY() - ((double) getHeight() / 2);

        // Calculate the new pixel position by adding the clicked position to the center pixel position
        double newCenterX = ((Point2D) centerPoint).getX() + x;
        double newCenterY = centerPoint.getY() + y;

        // Convert the new pixel position to a GeoPosition
        GeoPosition clickedGeoPos = mapViewer.getTileFactory().pixelToGeo(new Point2D.Double(newCenterX, newCenterY), zoom);

        return clickedGeoPos;
    }
}
