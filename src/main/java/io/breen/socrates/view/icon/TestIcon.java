package io.breen.socrates.view.icon;

import javax.swing.*;
import java.awt.*;

public abstract class TestIcon implements Icon {

    protected int width;
    protected int height;
    protected int opacity;

    public TestIcon(int width, int height, int opacity) {
        this.width = width;
        this.height = height;
        this.opacity = opacity;
    }

    public TestIcon(int width, int height) {
        this(width, height, 255);
    }

    public TestIcon() {
        this(16, 16);
    }

    public abstract void paintIcon(Component c, Graphics g, int x, int y);

    public int getIconWidth() {
        return width;
    }

    public void setIconWidth(int width) {
        this.width = width;
    }

    public int getIconHeight() {
        return height;
    }

    public void setIconHeight(int height) {
        this.height = height;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        if (opacity < 0 || opacity > 255)
            throw new IllegalArgumentException();
        this.opacity = opacity;
    }
}
