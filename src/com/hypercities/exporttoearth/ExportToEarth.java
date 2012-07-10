package com.hypercities.exporttoearth;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gephi.graph.api.GraphController;
import org.gephi.project.api.Workspace;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;


@ActionID(category = "Plugins",
id = "com.hypercities.exporttoearth.ExportToEarth")
@ActionRegistration(displayName = "#CTL_ExportToEarth")
@ActionReferences({
    @ActionReference(path = "Menu/Plugins", position = 0)
})
@Messages("CTL_ExportToEarth=Export to Earth")
public final class ExportToEarth implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Workspace workspace = Lookup.getDefault().lookup(GraphController.class)
                .getModel().getWorkspace();

        KMZExporter exporter = new KMZExporter();
        exporter.setWorkspace(workspace);
        exporter.execute();
    }

}
