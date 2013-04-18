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
import javax.swing.border.EmptyBorder;
import org.gephi.data.attributes.api.AttributeColumn;

/**
 * UI for attribute selection.
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

        setBorder(new EmptyBorder(10, 10, 10, 10));
        // for each column, create a new label, checkbox, lat radio button and lon radio button
        GridLayout layout = new GridLayout(0, 3);
        setLayout(layout);
        //add(new JLabel("Column name"));
        add(new JLabel("Include column?"));
        add(new JLabel("Longitude"));
        add(new JLabel("Latitude"));

        for (AttributeColumn column : allColumns) {
            String title = column.getTitle();
            columnNames.put(title, column);
            JLabel label = new JLabel(title);
            //add(label);
            
            JCheckBox checkbox = new JCheckBox(title);
            // TODO: set item responder
            checkBoxesToColumns.put(checkbox, column);
            checkbox.addItemListener(columnSelectorResponder);
            if (column != longitudeColumn && column != latitudeColumn) {
                checkbox.setSelected(true);
            }
            add(checkbox);

            JRadioButton lonButton = new JRadioButton();
            lonButton.addActionListener(longitudeColumnSelector);
            if (column == longitudeColumn) {
                lonButton.setSelected(true);
            }
            add(lonButton);

            JRadioButton latButton = new JRadioButton();
            latButton.addActionListener(latitudeColumnSelector);
            latButton.setActionCommand(title);
            if (column == latitudeColumn) {
                latButton.setSelected(true);
            }
            add(latButton);

            
        }
        // checkboxes are stored in a hash of objects, item to column name
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
