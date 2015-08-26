package io.breen.socrates.view.main;

import javax.swing.*;

/**
 * Class containing default style values for the JSyntaxPane.
 */
public class DefaultTheme {

    protected enum Style {
        PLAIN("0"),
        BOLD("1"),
        ITALIC("2"),
        BOLD_ITALIC("3");

        public final String s;

        Style(String s) {
            this.s = s;
        }
    }

    public final String background = "0xffffff";
    public final String foreground = "0x000000";
    public final String selectionColor = "#" + Integer.toHexString(
            UIManager.getColor("textHighlight").getRGB()
    ).substring(2);

    public final String font = "Monospaced-PLAIN-12";

    public final String lineNumberMargin = "10";
    public final String lineNumberForeground = "0x888888";
    public final String lineNumberBackground = background;
    public final String currentLineBackground = background;

    public final String keyword = colorAndStyle("0x5555cc", Style.PLAIN);
    public final String keyword2 = colorAndStyle("0x5555cc", Style.BOLD_ITALIC);

    public final String type = colorAndStyle("0x000000", Style.ITALIC);
    public final String type2 = colorAndStyle("0x000000", Style.BOLD);
    public final String type3 = colorAndStyle("0x000000", Style.BOLD_ITALIC);

    public final String string = colorAndStyle("0xcc4400", Style.PLAIN);
    public final String string2 = colorAndStyle("0xcc4400", Style.BOLD);

    public final String number = colorAndStyle("0x0099cc", Style.PLAIN);

    public final String regex = colorAndStyle("0xcc6600", Style.PLAIN);

    public final String identifier = colorAndStyle("0x000000", Style.PLAIN);

    public final String dephault = colorAndStyle("0x000000", Style.PLAIN);
    public final String warning = colorAndStyle("0xCC0000", Style.PLAIN);
    public final String error = colorAndStyle("0xCC0000", Style.BOLD_ITALIC);

    public final String comment = colorAndStyle("0x339933", Style.PLAIN);
    public final String comment2 = colorAndStyle("0x339933", Style.PLAIN);

    public final String operator = colorAndStyle("0x000000", Style.PLAIN);
    public final String delimiter = colorAndStyle("0x000000", Style.BOLD);

    protected static String colorAndStyle(String color, Style style) {
        return color + ", " + style.s;
    }

    protected DefaultTheme() {}
}
