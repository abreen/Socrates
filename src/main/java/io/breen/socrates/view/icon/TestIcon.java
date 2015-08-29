package io.breen.socrates.view.icon;

import javax.swing.*;
import java.awt.*;

public abstract class TestIcon implements Icon {
    protected final int width;
    protected final int height;

    public TestIcon(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public TestIcon() {
        this(16, 16);
    }

    public abstract void paintIcon(Component c, Graphics g, int x, int y);

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }
}
