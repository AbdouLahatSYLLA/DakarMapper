import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Paints a route on the map
 */
public class RoutePainter implements Painter<JXMapViewer> {
    private Color color = Color.BLUE;
    private boolean antiAlias = true;
    private int markerSize = 10; // Taille du marqueur personnalisé (cercle)
    private Color markerColor = Color.RED; // Couleur du marqueur personnalisé (cercle)

    private List<GeoPosition> track;

    public RoutePainter(List<GeoPosition> track) {
        this.track = new ArrayList<>(track);
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (antiAlias)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // do the drawing
        g.setColor(color);
        g.setStroke(new BasicStroke(2));

        drawRoute(g, map);

        // Dessiner le marqueur personnalisé
        g.setColor(markerColor);
        drawMarkers(g, map);

        g.dispose();
    }

    private void drawRoute(Graphics2D g, JXMapViewer map) {
        int lastX = 0;
        int lastY = 0;

        boolean first = true;

        for (GeoPosition gp : track) {
            // convert geo-coordinate to world bitmap pixel
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

            if (first) {
                first = false;
            } else {
                g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
            }

            lastX = (int) pt.getX();
            lastY = (int) pt.getY();
        }
    }

    private void drawMarkers(Graphics2D g, JXMapViewer map) {
        for (GeoPosition gp : track) {
            // convert geo-coordinate to world bitmap pixel
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

            int x = (int) pt.getX() - markerSize / 2;
            int y = (int) pt.getY() - markerSize / 2;

            // Dessiner le marqueur personnalisé (cercle)
            g.fillOval(x, y, markerSize, markerSize);
        }
    }

    public void setRoute(List<GeoPosition> routePoints) {
        this.track.addAll(routePoints);
    }

    public void clear() {
        track = new ArrayList<GeoPosition>();
    }

    public void setMarkerSize(int markerSize) {
        this.markerSize = markerSize;
    }

    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }
}
