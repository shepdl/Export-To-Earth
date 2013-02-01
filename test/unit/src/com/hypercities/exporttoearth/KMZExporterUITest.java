package com.hypercities.exporttoearth;

import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.exporter.spi.Exporter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dave Shepard
 */
public class KMZExporterUITest {
    
    public KMZExporterUITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of findGeoFields method, of class KMZExporterUI.
     */
    @Test
    public void testFindGeoFieldsWithLatitudeAndLongitude() {
        AttributeColumn[] columns = {
            new MockAttributeColumn("id", AttributeType.STRING),
            new MockAttributeColumn("stringLatitude", AttributeType.STRING),
            new MockAttributeColumn("latitude", AttributeType.DOUBLE),
            new MockAttributeColumn("longitude", AttributeType.DOUBLE)
        };
        KMZExporterUI instance = new KMZExporterUI();
        AttributeColumn[] resultColumns = instance.findGeoFields(columns);
        // TODO review the generated test code and remove the default call to fail.
        assertNotNull("Longitude not found.", resultColumns[0]);
        assertNotNull("Latitude not found.", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatitudeAndLongitudePartialNames() {
        AttributeColumn[] columns = {
            new MockAttributeColumn("id", AttributeType.STRING),
            new MockAttributeColumn("PlaceLatitudeInDegrees", AttributeType.DOUBLE),
            new MockAttributeColumn("PlaceLongitudeInDegrees", AttributeType.DOUBLE)
        };
        KMZExporterUI instance = new KMZExporterUI();
        AttributeColumn[] resultColumns = instance.findGeoFields(columns);
        // TODO review the generated test code and remove the default call to fail.
        assertNotNull("PlaceLongitudeInDegrees not found.", resultColumns[0]);
        assertNotNull("PlaceLatitudeInDegrees not found.", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatitudeAndLongitudeButWrongTypes() {
        AttributeColumn[] columns = {
            new MockAttributeColumn("id", AttributeType.STRING),
            new MockAttributeColumn("latitude", AttributeType.STRING),
            new MockAttributeColumn("longitude", AttributeType.STRING)
        };
        KMZExporterUI instance = new KMZExporterUI();
        AttributeColumn[] resultColumns = instance.findGeoFields(columns);
        assertNull("Longitude found even though it's a string.", resultColumns[0]);
        assertNull("Latitude found even though it's a string.", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatAndLon() {
        AttributeColumn[] columns = {
            new MockAttributeColumn("id", AttributeType.STRING),
            new MockAttributeColumn("lat", AttributeType.DOUBLE),
            new MockAttributeColumn("lon", AttributeType.DOUBLE)
        };
        KMZExporterUI instance = new KMZExporterUI();
        AttributeColumn[] resultColumns = instance.findGeoFields(columns);
        assertNotNull("Lon not found", resultColumns[0]);
        assertNotNull("Lat not found", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatAndLng() {
        AttributeColumn[] columns = {
            new MockAttributeColumn("id", AttributeType.STRING),
            new MockAttributeColumn("lat", AttributeType.DOUBLE),
            new MockAttributeColumn("lng", AttributeType.DOUBLE)
        };
        KMZExporterUI instance = new KMZExporterUI();
        AttributeColumn[] resultColumns = instance.findGeoFields(columns);
        assertNotNull("lng not found", resultColumns[0]);
        assertNotNull("lat not found", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithXAndY() {
        AttributeColumn[] columns = {
            new MockAttributeColumn("id", AttributeType.STRING),
            new MockAttributeColumn("x", AttributeType.DOUBLE),
            new MockAttributeColumn("y", AttributeType.DOUBLE)
        };
        KMZExporterUI instance = new KMZExporterUI();
        AttributeColumn[] resultColumns = instance.findGeoFields(columns);
        assertNotNull("Y not found", resultColumns[0]);
        assertNotNull("X not found", resultColumns[1]);
    }
}
