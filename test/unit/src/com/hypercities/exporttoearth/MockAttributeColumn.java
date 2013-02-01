package com.hypercities.exporttoearth;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;

/**
 *
 * @author Dave Shepard
 */
public class MockAttributeColumn implements AttributeColumn {
    private String title;
    private AttributeType type;

    public MockAttributeColumn(String title, AttributeType type) {
        this.title = title;
        this.type = type;
    }

    @Override
    public AttributeType getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getIndex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AttributeOrigin getOrigin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getDefaultValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AttributeValueDelegateProvider getProvider() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AttributeTable getTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
