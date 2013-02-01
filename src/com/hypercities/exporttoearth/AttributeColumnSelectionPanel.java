/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hypercities.exporttoearth;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.gephi.data.attributes.api.AttributeColumn;

/**
 *
 * @author Dave Shepard
 */
public class AttributeColumnSelectionPanel extends JPanel {
    
    // Fields that will be exposed.
    private AttributeColumn longitudeColumn;
    private AttributeColumn latitudeColumn;
    private ArrayList<AttributeColumn> columnsToExport = new ArrayList<AttributeColumn>();

    private Map<String, AttributeColumn> columnNames = new HashMap<String, AttributeColumn>();
    private Map<JCheckBox, AttributeColumn> checkBoxesToColumns = new HashMap<JCheckBox, AttributeColumn>();


    private ActionListener longitudeColumnSelector = new ActionListener() {

        @Override
        /**
         * Expects ActionEvent.ActionCommand will be column name
         */
        public void actionPerformed(ActionEvent ae) {
            longitudeColumn = columnNames.get(ae.getActionCommand());
        }
    };

    private ActionListener latitudeColumnSelector = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            latitudeColumn = columnNames.get(ae.getActionCommand());
        }
    };

    private ItemListener columnSelectorResponder = new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent ie) {
            Object source = ie.getItemSelectable();
            for (Map.Entry<JCheckBox, AttributeColumn> entry : checkBoxesToColumns.entrySet()) {
                AttributeColumn column = entry.getValue();
                if (source == entry.getKey()) {
                    if (ie.getStateChange() == ItemEvent.DESELECTED) {
                        columnsToExport.remove(column);
                    } else {
                        if (!columnsToExport.contains(column)) {
                            columnsToExport.add(column);
                        }
                    } // end else
                    // break out of loop early
                    break;
                } // end if
            } // end for
        } // end itemStateChanged
    };

    /**
     * Create panel with specified columns.
     * 
     * @param allColumns
     * @param longitudeColumn Column to pre-select as the longitude column. May be null.
     * @param latitudeColumn Column to pre-select as the latitude column. May be null.
     */
    public AttributeColumnSelectionPanel(AttributeColumn[] allColumns, 
            AttributeColumn longitudeColumn, AttributeColumn latitudeColumn) {
        this.longitudeColumn = longitudeColumn;
        this.latitudeColumn = latitudeColumn;
        columnsToExport = new ArrayList<AttributeColumn>();

        columnNames = new HashMap<String, AttributeColumn>();
        checkBoxesToColumns = new HashMap<JCheckBox, AttributeColumn>();

        // for each column, create a new label, checkbox, lat radio button and lon radio button
        GridLayout layout = new GridLayout(0, 4);
        setLayout(layout);

        for (AttributeColumn column : allColumns) {
            String title = column.getTitle();
            columnNames.put(title, column);
            JLabel label = new JLabel(title);
            add(label);
            
            JCheckBox checkbox = new JCheckBox();
            // TODO: set item responder
            checkBoxesToColumns.put(checkbox, column);
            checkbox.addItemListener(columnSelectorResponder);
            add(checkbox);

            JRadioButton latButton = new JRadioButton();
            latButton.addActionListener(latitudeColumnSelector);
            latButton.setActionCommand(title);
            if (column == latitudeColumn) {
                latButton.setSelected(true);
            }
            add(latButton);

            JRadioButton lonButton = new JRadioButton(title);
            lonButton.addActionListener(longitudeColumnSelector);
            if (column == longitudeColumn) {
                lonButton.setSelected(true);
            }
            add(lonButton);
            
        }
        // checkboxes are stored in a hash of objects, item to column name
        // TODO: save geo fields and required fields
    }

    public AttributeColumn getLongitudeColumn() {
        return longitudeColumn;
    }

    public AttributeColumn getLatitudeColumn() {
        return latitudeColumn;
    }

    public AttributeColumn[] getColumnsToExport() {
        return columnsToExport.toArray(new AttributeColumn[columnsToExport.size()]);
    }
}
