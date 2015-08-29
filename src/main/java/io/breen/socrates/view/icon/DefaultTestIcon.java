package io.breen.socrates.view.icon;

import java.awt.*;

public class DefaultTestIcon extends TestIcon {

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        BasicStroke stroke = new BasicStroke();

        g2d.setColor(new Color(186, 186, 186, opacity));
        g2d.setStroke(stroke);
        g2d.fillOval(x + 4, y + 4, width - 6, height - 6);

        g2d.dispose();
    }
}
