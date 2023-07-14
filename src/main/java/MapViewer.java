import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import java.awt.*;

public class MapViewer extends JFrame {

    private JXMapViewer mapViewer;

    public MapViewer() {
        setTitle("OpenStreetMap Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Création du JXMapViewer
        mapViewer = new JXMapViewer();
        mapViewer.setZoom(2);

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
        TileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Ajout du JXMapViewer à la fenêtre
        getContentPane().add(mapViewer, BorderLayout.CENTER);
    }

    public void setMarker(double latitude, double longitude) {
        // Ajout d'un marqueur à une position spécifiée
        GeoPosition position = new GeoPosition(latitude, longitude);
        mapViewer.setAddressLocation(position);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MapViewer mapViewer = new MapViewer();
                mapViewer.setVisible(true);

                // Exemple d'utilisation : Définition d'un marqueur à la position spécifiée
                mapViewer.setMarker(48.858093, 2.294694); // Exemple : Tour Eiffel
            }
        });
    }
}
