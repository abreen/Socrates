package io.breen.socrates.view.icon;

import java.awt.*;

public class RunningTestIcon extends TestIcon {

    public RunningTestIcon() {}

    public RunningTestIcon(int width, int height) {
        super(width, height);
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        int padding = width / 6;
        int dotWidth = width / 3 - (padding / 2);

        BasicStroke stroke = new BasicStroke();

        g2d.setColor(new Color(37, 123, 210));
        g2d.setStroke(stroke);

        g2d.fillOval(x + padding, y + padding, dotWidth, dotWidth);
        g2d.fillOval(x + padding + dotWidth, y + padding + dotWidth, dotWidth, dotWidth);
        g2d.fillOval(x + padding + 2 * dotWidth, y + padding + 2 * dotWidth, dotWidth, dotWidth);

        g2d.dispose();
    }
}
