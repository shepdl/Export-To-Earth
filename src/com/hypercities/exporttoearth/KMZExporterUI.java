package com.hypercities.exporttoearth;

import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * UI for panel
 * @author Dave Shepard
 */
@ServiceProvider(service = ExporterUI.class)
public class KMZExporterUI implements ExporterUI {

    private AttributeColumnSelectionPanel panel;
    private KMZExporter exporter;

    private AttributeModel model = Lookup.getDefault().lookup(AttributeController.class).getModel();
    private AttributeColumn longitudeColumn;
    private AttributeColumn latitudeColumn;

    @Override
    public JPanel getPanel() {
        // get all fields
        AttributeColumn[] columns = model.getNodeTable().getColumns();

        // get geocoordinate fields
        GeoAttributeFinder gaf = new GeoAttributeFinder();
        gaf.findGeoFields(columns);
        longitudeColumn = gaf.getLongitudeColumn();
        latitudeColumn = gaf.getLatitudeColumn();
        // for each column, create a new label, checkbox, lat radio button and lon radio button
        // checkboxes are stored in a hash of objects, item to column name
        panel = new AttributeColumnSelectionPanel(columns, longitudeColumn, latitudeColumn);
        return panel;
    }

    @Override
    public void setup(Exporter exprtr) {
        model = Lookup.getDefault().lookup(AttributeController.class).getModel();
        exporter = (KMZExporter)exprtr;
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            // the user hit OK; save everything
            exporter.setColumnsToUse(panel.getLongitudeColumn(), 
                    panel.getLatitudeColumn(), 
                    panel.getColumnsToExport()
                );
        } else {
            // cancel was hit
        }
        panel = null;
        exporter = null;
    }

    @Override
    public boolean isUIForExporter(Exporter exprtr) {
        return exprtr instanceof KMZExporter;
    }

    @Override
    public String getDisplayName() {
        return "Select latitude and longitude fields";
    }
    
}
