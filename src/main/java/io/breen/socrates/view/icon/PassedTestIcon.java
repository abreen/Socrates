package io.breen.socrates.view.icon;

import io.breen.socrates.Globals;

import java.awt.*;

public class PassedTestIcon extends TestIcon {

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        int strokeWidth = width / 4 - 1;
        int padding = width / 5;

        int stroke1start = width / 2;
        int stroke1end = width / 4;

        BasicStroke stroke = new BasicStroke(strokeWidth);

        g2d.setColor(Globals.GREEN);
        g2d.setStroke(stroke);

        g2d.drawLine(
                x + padding,
                y + stroke1start + padding,
                x + width - stroke1end - padding,
                y + height - padding
        );

        g2d.drawLine(
                x + width - stroke1end - padding,
                y + height - padding,
                x + width - padding,
                y + padding
        );

        g2d.dispose();
    }
}
