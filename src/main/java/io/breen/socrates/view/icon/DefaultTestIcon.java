package io.breen.socrates.view.icon;

import javax.swing.*;
import java.awt.*;

public class DefaultTestIcon implements Icon {

    private final int width = 16;
    private final int height = 16;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        BasicStroke stroke = new BasicStroke(4);

        g2d.setColor(new Color(140, 140, 140));
        g2d.setStroke(stroke);
        g2d.fillOval(x + 4, y + 4, width - 6, height - 6);

        g2d.dispose();
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }
}
