package io.breen.socrates.view.icon;

import javax.swing.*;
import java.awt.*;

public class PassedTestIcon implements Icon {

    private final int width = 16;
    private final int height = 16;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        BasicStroke stroke = new BasicStroke(3);

        g2d.setColor(new Color(49, 141, 34));
        g2d.setStroke(stroke);

        g2d.drawLine(x + 3, y + 9, x + width - 8, y + height - 4);
        g2d.drawLine(x + width - 8, y + height - 4, x + width - 2, y + 2);

        g2d.dispose();
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }
}
