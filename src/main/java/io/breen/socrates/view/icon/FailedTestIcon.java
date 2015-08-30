package io.breen.socrates.view.icon;

import io.breen.socrates.Globals;

import java.awt.*;

public class FailedTestIcon extends TestIcon {

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        int strokeWidth = width / 4 - 1;
        BasicStroke stroke = new BasicStroke(strokeWidth);

        g2d.setColor(Globals.RED);
        g2d.setStroke(stroke);

        g2d.drawLine(x + 4, y + 4, x + width - 4, y + height - 4);
        g2d.drawLine(x + 4, y + height - 4, x + width - 4, y + 4);

        g2d.dispose();
    }
}
