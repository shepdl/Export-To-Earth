package com.hypercities.exporttoearth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;

/**
 *
 * @author Dave Shepard
 */
public class GeoAttributeFinder {

    private AttributeColumn longitudeColumn;
    private AttributeColumn latitudeColumn;

    AttributeColumn[] findGeoFields(AttributeColumn[] columns) {
        String[] latAttributes = {"latitude", "^lat$", "^y$", "(.*)lat(.*)"};
        String[] lonAttributes = {"longitude", "lon", "lng", "^x$", "(.*)lon(.*)", "(.*)lng(.*)"};

        // find attributes by iterating over property names
        longitudeColumn = getAttributeField(lonAttributes, columns);
        latitudeColumn = getAttributeField(latAttributes, columns);
        AttributeColumn[] result = {getLongitudeColumn(), getLatitudeColumn()};
        return result;
    }

    AttributeColumn getAttributeField(String[] patterns, AttributeColumn[] columns) {
        for (AttributeColumn col : columns) {
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

                    return col;
                }
            }
        }
        return null;
    }

    /**
     * @return the longitudeColumn
     */
    public AttributeColumn getLongitudeColumn() {
        return longitudeColumn;
    }

    /**
     * @return the latitudeColumn
     */
    public AttributeColumn getLatitudeColumn() {
        return latitudeColumn;
    }
}
