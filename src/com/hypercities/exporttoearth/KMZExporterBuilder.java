package com.hypercities.exporttoearth;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Builder for Export to Earth plugin.
 * 
 * @author Dave Shepard
 */
@ServiceProvider(service=GraphFileExporterBuilder.class)
public class KMZExporterBuilder implements GraphFileExporterBuilder {

	@Override
	public GraphExporter buildExporter() {
		Logger.getAnonymousLogger().log(Level.INFO, "Building exporter");
		return new KMZExporter();
	}

	@Override
	public FileType[] getFileTypes() {
		return new FileType[]{new FileType(".kmz", "KMZ File")};
	}

	@Override
	public String getName() {
		return "Export to Earth";
	}
	
}
