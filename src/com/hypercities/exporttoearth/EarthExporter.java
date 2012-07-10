/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hypercities.exporttoearth;

import org.gephi.io.exporter.spi.Exporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author daveshepard
 */
public class EarthExporter implements Exporter, LongTask {

    @Override
    public boolean execute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setWorkspace(Workspace wrkspc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Workspace getWorkspace() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean cancel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
