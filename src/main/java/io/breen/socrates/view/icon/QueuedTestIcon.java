package io.breen.socrates.view.icon;

import io.breen.socrates.Globals;

import java.awt.*;

public class QueuedTestIcon extends TestIcon {

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        BasicStroke stroke = new BasicStroke(2);

        g2d.setColor(Globals.GRAY);
        g2d.setStroke(stroke);
        g2d.drawOval(x + 4, y + 4, width - 6, height - 6);

        g2d.dispose();
    }
}
