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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;

/**
 * Find columns with geographic attributes in Data Laboratory.
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
