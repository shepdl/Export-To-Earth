/**
 * Copyright (c) 2012, David Shepard All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.hypercities.exporttoearth;

import de.micromata.opengis.kml.v_2_2_0.*;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.*;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 * Exports Gephi graphs to KMZ files.
 *
 * @author Dave Shepard
 */
public class KMZExporter implements GraphExporter, ByteExporter, LongTask {

    private boolean exportVisible;
    private Workspace workspace;
    private boolean cancelled;
    private ProgressTicket ticket;
    private File rootDir;
    private OutputStream outputStream;

    @Override
    public void setExportVisible(boolean bln) {
        exportVisible = bln;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public boolean execute() {

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewProperties props = controller.getModel(workspace).getProperties();
        PreviewModel previewModel = workspace.getLookup().lookup(PreviewModel.class);
        int width = 50;
        int height = 50;
        props.putValue("width", width);
        props.putValue("height", height);

        // 1. Validate -- do we have lat/lon columns?
        ticket.start();
        Progress.start(ticket);

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
                    + " Georeferencing will be impossible.\n"
                    + "If you think you have geocoordinates, try refreshing "
                    + "the graph in Preview mode.";
            JOptionPane.showMessageDialog(null, message, "Geocoordinates Not Found", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (invalidNodeCount > validNodes.size()) {
            double nodesWithoutCoordinates = invalidNodeCount / totalNodes;
            String message = "Warning: " + (int) (nodesWithoutCoordinates * 100)
                    + "% of the nodes in this graph have no geocoordinates.\n"
                    + " A KMZ will still be produced, but it may not have very many"
                    + " nodes or edges.";
            JOptionPane.showMessageDialog(null, message, "Few geocoordinates found.", JOptionPane.ERROR_MESSAGE);
        }

        // 2. Produce export
        final Kml kml = new Kml();
        final Folder folder = kml.createAndSetFolder();
        // 2a. produce nodes
        int styleCounter = 0;

        ticket.setDisplayName("Finding nodes");
        ticket.start(validNodes.size());

        HashMap<Integer, Color> modularityClassColors = new HashMap<Integer, Color>();
        IconRenderer renderer = new IconRenderer();

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
            // It's possible for an edge to have a source or target that doesn't
            // have geocoordinates, so we skip those.
            if (source == null || targe == null) {
                continue;
            }
            if (source.getValue(latitudeName) == null 
                    || source.getValue(longitudeName) == null
                    || targe.getValue(latitudeName) == null 
                    || targe.getValue(longitudeName) == null) {
                continue;
            }
            Color color = (Color) i.getData(EdgeItem.COLOR);
            float weight = (Float) i.getData(EdgeItem.WEIGHT);

            String title = i.getData(EdgeItem.EDGE_LABEL);
            if (title == null) {
                title = source.getValue("Label") + " and " + targe.getValue("Label");
            }
            String description = "";
            for (AttributeColumn ac : model.getEdgeTable().getColumns()) {
                if ((ac.getTitle() == null ? latitudeName != null
                        : !ac.getTitle().equals(latitudeName))
                        && (ac.getTitle() == null ? longitudeName != null
                        : !ac.getTitle().equals(longitudeName))) {

                    // Filter labels with null attributes
                    if (row.getValue(ac) != null) {
                        description += ac.getTitle() + ": " + row.getValue(ac) + "\n";
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
                return false;
            }
        }
        try {
            writeKMZ(kml, renderer);
            JOptionPane.showMessageDialog(null, "Export complete",
                    "KML Export complete.", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(KMZExporter.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error saving.",
                    "Sorry, could not write the file.", JOptionPane.ERROR_MESSAGE);
        } finally {
            ticket.finish();
            return true;
        }
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

    private synchronized void writeKMZ(Kml kml, IconRenderer icons) throws IOException {
        ZipOutputStream out = new ZipOutputStream(outputStream);
        ZipEntry entry = new ZipEntry("doc.kml");
        out.putNextEntry(entry);
        kml.marshal(out);

        icons.renderToKMZ(out);
        out.close();
    }

    @Override
    public void setWorkspace(Workspace wrkspc) {
        workspace = wrkspc;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean cancel() {
        return cancelled = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        ticket = pt;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }
}
