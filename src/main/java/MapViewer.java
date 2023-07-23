import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;


public class MapViewer extends JPanel {

    private JXMapViewer mapViewer;
    private Point startPoint;
    private double moveScale = 0.0001; // Facteur d'échelle pour le déplacement plus lent

    private  float myLat = 0.0F;
    private  float myLng = 0.0F;


    public MapViewer() {
        setLayout(new BorderLayout());

        // Création du JXMapViewer
        mapViewer = new JXMapViewer();
        mapViewer.setZoom(34); // Zoom initial pour Paris


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

        // Centrer la carte sur Dakar
        double dakarLatitude = 14.6927;
        double dakarLongitude = -17.4467;
        mapViewer.setCenterPosition(new GeoPosition(dakarLatitude, dakarLongitude));
    }

    public void setMarker(double latitude, double longitude) {
        // Ajout d'un marqueur à une position spécifiée
        GeoPosition position = new GeoPosition(latitude, longitude);
        mapViewer.setCenterPosition(position);
        mapViewer.setZoom(10);
    }

    public JXMapViewer getMapViewer(){
        return  this.mapViewer;
    }

    public Point getStartPoint(){
        return this.startPoint;
    }

    public float getMyLat(){
        return this.myLat;
    }

    public float getMyLng(){
        return this.myLng;
    }

    public GeoPosition convertPointToGeoPosition(Point point) {
        int zoom = mapViewer.getZoom();
        int tileSize = mapViewer.getTileFactory().getTileSize(zoom);

        // Get the current center GeoPosition and the top-left pixel position of the map
        GeoPosition centerGeoPos = mapViewer.getCenterPosition();
        Point2D centerPoint = mapViewer.getTileFactory().geoToPixel(centerGeoPos, zoom);

        // Get the clicked pixel position relative to the center of the map
        double x = point.getX() - (getWidth() / 2);
        double y = point.getY() - (getHeight() / 2);

        // Calculate the new pixel position by adding the clicked position to the center pixel position
        double newCenterX = ((Point2D) centerPoint).getX() + x;
        double newCenterY = centerPoint.getY() + y;

        // Convert the new pixel position to a GeoPosition
        GeoPosition clickedGeoPos = mapViewer.getTileFactory().pixelToGeo(new Point2D.Double(newCenterX, newCenterY), zoom);

        return clickedGeoPos;
    }
}
