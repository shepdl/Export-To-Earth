package com.hypercities.exporttoearth;

import de.micromata.opengis.kml.v_2_2_0.*;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.*;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * Exports Gephi graphs with geocoordinates to Google Earth KMZs.
 *
 * @author Dave Shepard
 */
public class KMZExporter implements GraphExporter, Cancellable {

    private Workspace workspace;
    private boolean exportVisible;
    private boolean cancelled = false;
    private File rootDir;

    @Override
    public boolean execute() {

        ProgressTicketProvider ptp = Lookup.getDefault().lookup(ProgressTicketProvider.class);
        if (ptp == null) {
            JOptionPane.showMessageDialog(null, "Could not acquire Progress Ticket.");
            return false;
        }

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewProperties props = controller.getModel(workspace).getProperties();
        PreviewModel previewModel = workspace.getLookup().lookup(PreviewModel.class);
        int width = 50;
        int height = 50;
        props.putValue("width", width);
        props.putValue("height", height);

        // 1. Validate -- do we have lat/lon columns?
        ProgressTicket ticket = ptp.createTicket("Locating coordinate columns", this);
        Progress.start(ticket);
        ticket.switchToIndeterminate();

        String[] latAttributes = {"latitude", "^lat$", "^y$", "(.*)lat(.*)"};
        String[] lonAttributes = {"longitude", "lon", "lng", "^x$", "(.*)lon(.*)", "(.*)lng(.*)"};

        // find attributes by iterating over property names
        AttributeModel model = Lookup.getDefault().lookup(AttributeController.class).getModel();
        String latitudeName = getAttributeField(latAttributes, model);
        String longitudeName = getAttributeField(lonAttributes, model);

        if (latitudeName.isEmpty() || longitudeName.isEmpty()) {
            String missingMessage = "Sorry, but we could not identify ";

            if (latitudeName.isEmpty() && longitudeName.isEmpty()) {
                missingMessage += "a latitude field or a longitude field.";
            } else if (latitudeName.isEmpty()) {
                missingMessage += "a latitude field.";
            } else if (longitudeName.isEmpty()) {
                missingMessage += "a longitude field.";
            }

            missingMessage += "This plugin cannot run. Please identify lat and lon"
                    + " columns by adding 'lat' and 'lon' to the appropriate columns,"
                    + " and converting them to floats, doubles, or decimals.";
            JOptionPane.showMessageDialog(null, missingMessage,
                    "Geocoordinates Not Found", JOptionPane.ERROR_MESSAGE);
        }

        Progress.finish(ticket);

        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(null);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        try {

            rootDir = fc.getSelectedFile();

            rootDir.mkdir();

            int renderablesCount = 0;
            ArrayList<NodeItem> validNodes = new ArrayList<NodeItem>();
            double invalidNodeCount = 0,
                    totalNodes = 0;

            Float maxSize = new Float(0.0);
            for (Item ni : previewModel.getItems(Item.NODE)) {
                Node n = (Node) ni.getSource();
                AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                Float size = (Float) ni.getData(NodeItem.SIZE);

                if (size > maxSize) {
                    maxSize = size;
                }

                boolean hasLat = false,
                        hasLon = false;

                if (row.getValue(latitudeName) != null) {
                    hasLat = true;
                }
                if (row.getValue(longitudeName) != null) {
                    hasLon = true;
                }

                if (hasLat && hasLon) {
                    validNodes.add((NodeItem) ni);
                    renderablesCount++;
                } else {
                    invalidNodeCount++;
                }
                totalNodes++;
            }

            HashMap<String, Item> edgeList = new HashMap<String, Item>();
            float maxWeight = 0;
            for (Item i : previewModel.getItems(Item.EDGE)) {
                Float weight = (Float) i.getData(EdgeItem.WEIGHT);
                if (weight > maxWeight) {
                    maxWeight = weight;
                    renderablesCount++;
                }
            }

            if (invalidNodeCount == totalNodes) {
                // no valid nodes: exit
                String message = "Sorry, no nodes in this graph have geocoordinates."
                        + " Georeferencing will be impossible.";
                JOptionPane.showMessageDialog(null, message, "Geocoordinates Not Found", JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (invalidNodeCount > validNodes.size()) {
                double nodesWithoutCoordinates = invalidNodeCount / totalNodes;
                String message = (int) (nodesWithoutCoordinates * 100) + "% of the nodes"
                        + " in this graph have no"
                        + " geocoordinates. Are you sure you want to continue?";
                return false;
            }

            // 2. Produce export
            final Kml kml = new Kml();
            final Folder folder = kml.createAndSetFolder();
            // 2a. produce nodes
            int styleCounter = 0;

            ticket = ptp.createTicket("Creating icons", this);
            Progress.start(ticket, validNodes.size());

            ArrayList<String> iconFiles = new ArrayList<String>();
            HashMap<Integer, Color> modularityClassColors = new HashMap<Integer, Color>();
            IconRenderer renderer = new IconRenderer(rootDir);
            int progressCounter = 0;
            Progress.finish(ticket);


            double maxScale = 2.0;
            for (NodeItem ni : validNodes) {
                Node n = (Node) ni.getSource();
                renderer.render(ni, props);
                String iconFilename = renderer.getLastFilename();
                AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                Float weight = (Float) ni.getData(NodeItem.SIZE);

                String description = "";
                for (AttributeColumn ac : model.getNodeTable().getColumns()) {
                    if ((ac.getTitle() == null ? latitudeName != null
                            : !ac.getTitle().equals(latitudeName))
                            && (ac.getTitle() == null ? longitudeName != null
                            : !ac.getTitle().equals(longitudeName))) {

                        description += ac.getTitle() + ": " + row.getValue(ac) + "\n";

                        if (ac.getTitle().equals("Modularity Class")) {
                            modularityClassColors.put((Integer) row.getValue(ac), (Color) ni.getData(NodeItem.COLOR));
                        }
                    }
                }
                Placemark placemark = folder.createAndAddPlacemark().withName((String) row.getValue("Label")).withDescription(description);

                Style style = folder.createAndAddStyle().withId("style_" + styleCounter);
                style.createAndSetIconStyle().withScale((weight / maxSize) * maxScale).withIcon(new Icon().withHref(iconFilename));

                placemark.setStyleUrl("#style_" + styleCounter);
                placemark.createAndSetPoint().addToCoordinates((Double) row.getValue(longitudeName),
                        (Double) row.getValue(latitudeName));
                styleCounter++;

                if (cancelled) {
                    cleanup();
                    return false;
                }
            }

            if (styleCounter == 0) {
                JOptionPane.showMessageDialog(null, "Sorry, we could not locate the preivew.\n"
                        + " Please try switching to Preview mode and running the plugin again.");
                return false;
            }

            // 2b. produce edges
            for (Item i : previewModel.getItems(Item.EDGE)) {
                Edge e = (Edge) i.getSource();
                AttributeRow row = (AttributeRow) e.getAttributes();
                AttributeRow source = (AttributeRow) e.getSource().getAttributes();
                AttributeRow targe = (AttributeRow) e.getTarget().getAttributes();
                Color color = (Color) i.getData(EdgeItem.COLOR);
                float weight = (Float) i.getData(EdgeItem.WEIGHT);

                String title = i.getData(EdgeItem.EDGE_LABEL);
                if (title == null) {
                    title = source.getValue("Label") + " and " + targe.getValue("Label");
                }
                String description = "";
                String modularityClass = "";
                for (AttributeColumn ac : model.getEdgeTable().getColumns()) {
                    if ((ac.getTitle() == null ? latitudeName != null
                            : !ac.getTitle().equals(latitudeName))
                            && (ac.getTitle() == null ? longitudeName != null
                            : !ac.getTitle().equals(longitudeName))) {

                        description += ac.getTitle() + ": " + row.getValue(ac) + "\n";
                        if (ac.getTitle().equals("Modularity Class")) {
                            color = modularityClassColors.get((Integer) row.getValue(ac));
                        }
                    }
                }

                // Default is whitish
                String colorCode = "#33ffffff";
                if (color != null) {
                    colorCode = "#" + Integer.toHexString(color.getAlpha()) + ""
                            + Integer.toHexString(color.getRed())
                            + Integer.toHexString(color.getGreen())
                            + Integer.toHexString(color.getBlue());
                }

                Placemark placemark = folder.createAndAddPlacemark().withDescription(description).withName(title);

                Style style = folder.createAndAddStyle().withId("style_" + styleCounter);
                style.createAndSetLineStyle().withWidth((weight / maxWeight) * 20.0).withColorMode(ColorMode.NORMAL).withColor(colorCode);
                placemark.setStyleUrl("#style_" + styleCounter);

                placemark.createAndSetLineString().addToCoordinates((Double) source.getValue(longitudeName),
                        (Double) source.getValue(latitudeName), 0).addToCoordinates((Double) targe.getValue(longitudeName),
                        (Double) targe.getValue(latitudeName), 0).withTessellate(Boolean.TRUE).withExtrude(Boolean.TRUE);

                styleCounter++;


                if (cancelled) {
                    cleanup();
                    return false;
                }
            }
            try {
                kml.marshal(new File(rootDir.getPath() + "/doc.kml"));
                writeKMZ();
            } catch (IOException ex) {
                Logger.getLogger(ExportToEarth.class.getName()).log(Level.SEVERE, null, ex);
            }


            JOptionPane.showMessageDialog(null, "Export complete",
                    "KML Export complete.", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } finally {
            // Delete files created in case of abnormal termination.
            cleanup();
        }
    }

    private synchronized void writeKMZ() throws IOException {
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(rootDir.getPath() + ".kmz");
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        final int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        String files[] = rootDir.list();

        for (int i = 0; i < files.length; i++) {
            if (".DS_Store".equals(files[i])) {
                continue;
            }

            FileInputStream fi = new FileInputStream(rootDir.getPath() + "/" + files[i]);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(files[i]);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        out.close();
        cleanup();
    }

    private synchronized void cleanup() {
        for (String filename : rootDir.list()) {
            File file = new File(rootDir.getPath() + "/" + filename);
            file.delete();
        }
        rootDir.delete();
    }

    private String getAttributeField(String[] patterns, AttributeModel model) {
        for (AttributeColumn col : model.getNodeTable().getColumns()) {
            for (String str : patterns) {
                Pattern pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(col.getTitle());
                if (matcher.find() && (col.getType() == AttributeType.FLOAT
                        // Make sure data is formatted correctly
                        || col.getType() == AttributeType.DOUBLE
                        || col.getType() == AttributeType.BIGDECIMAL
                        || col.getType() == AttributeType.DYNAMIC_BIGDECIMAL
                        || col.getType() == AttributeType.DYNAMIC_DOUBLE
                        || col.getType() == AttributeType.DYNAMIC_FLOAT)) {

                    return col.getTitle();
                }
            }
        }
        return "";
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        return cancelled;
    }

    @Override
    public void setExportVisible(boolean bln) {
        exportVisible = bln;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public void setWorkspace(Workspace wrkspc) {
        workspace = wrkspc;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
