package com.hypercities.exporttoearth;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Dave Shepard
 */
public class GeoAttributeFinderTest {
    
    public GeoAttributeFinderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testFindGeoFieldsWithLatitudeAndLongitude() {
        AttributeColumn[] columns = {
            new MockAttributeColumn("id", AttributeType.STRING),
            new MockAttributeColumn("stringLatitude", AttributeType.STRING),
            new MockAttributeColumn("latitude", AttributeType.DOUBLE),
            new MockAttributeColumn("longitude", AttributeType.DOUBLE)
        };
        GeoAttributeFinder instance = new GeoAttributeFinder();
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
        GeoAttributeFinder instance = new GeoAttributeFinder();
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
        GeoAttributeFinder instance = new GeoAttributeFinder();
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
        GeoAttributeFinder instance = new GeoAttributeFinder();
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
        GeoAttributeFinder instance = new GeoAttributeFinder();
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
        GeoAttributeFinder instance = new GeoAttributeFinder();
        AttributeColumn[] resultColumns = instance.findGeoFields(columns);
        assertNotNull("Y not found", resultColumns[0]);
        assertNotNull("X not found", resultColumns[1]);
    }
}
