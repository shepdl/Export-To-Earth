package com.hypercities.exporttoearth;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.types.DependantColor;

/**
 * Creates icons for Google Earth.
 * 
 * Caches icons to avoid having to regenerate them.
 * 
 * @author Dave Shepard
 */
public class IconRenderer {

    private ArrayList<String> filenames = new ArrayList<String>();
    private File rootDir;

    private String lastFilename;

    public IconRenderer(File rootDir) {
        this.rootDir = rootDir;
    }

    public void render (Item item, PreviewProperties pp) {
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) pp.getValue(
                PreviewProperty.NODE_BORDER_COLOR)
            ).getColor(color);
        int borderSize = (int)pp.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH) * 4;
        int size = 80;

        lastFilename = "tiles-" + size + "-" + color.getRed() + "-" +
                color.getGreen() + "-" + color.getBlue() + ".png";
        // If the circle has already been generated, don't regenerate it.
        if (filenames.contains(lastFilename)) {
            return;
        }
        filenames.add(lastFilename);
        
        int alpha = (int) ((pp.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f) * 255f);
        if (alpha > 255) {
            alpha = 255;
        }

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        graphics.setColor(color);
        graphics.fillOval(borderSize, borderSize, size - borderSize, size - borderSize);

        if (borderSize > 0) {
            graphics.setColor(borderColor);
            graphics.drawOval(borderSize, borderSize, size - borderSize, size - borderSize);
        } 

        File file = new File(rootDir.getPath() + "/" + lastFilename);
        try {
            ImageIO.write(img, "png", file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getLastFilename() {
        return lastFilename;
    }
}
