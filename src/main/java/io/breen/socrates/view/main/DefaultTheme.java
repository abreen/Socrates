package io.breen.socrates.view.main;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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

    public static final Map<String, String> map;
    public static final Color backgroundColor;
    public static final Color foregroundColor;

    static {
        backgroundColor = Color.white;
        foregroundColor = Color.black;

        map = new HashMap<>();

        map.put("SelectionColor", toHex(UIManager.getColor("textHighlight")));

        map.put("DefaultFont", "Monospaced-PLAIN-12");

        map.put("LineNumbers.RightMargin", "10");
        map.put("LineNumbers.Foreground", toHex(new Color(100, 100, 100)));
        map.put("LineNumbers.Background", toHex(backgroundColor));
        map.put("LineNumbers.CurrentBack", toHex(backgroundColor));

        map.put("Style.KEYWORD", colorAndStyle("0x5555cc", Style.PLAIN));
        map.put("Style.KEYWORD2", colorAndStyle("0x5555cc", Style.BOLD_ITALIC));
        map.put("Style.TYPE", colorAndStyle("0x000000", Style.ITALIC));
        map.put("Style.TYPE2", colorAndStyle("0x000000", Style.BOLD));
        map.put("Style.TYPE3", colorAndStyle("0x000000", Style.BOLD_ITALIC));
        map.put("Style.STRING", colorAndStyle("0xcc4400", Style.PLAIN));
        map.put("Style.STRING2", colorAndStyle("0xcc4400", Style.BOLD));
        map.put("Style.NUMBER", colorAndStyle("0x0099cc", Style.PLAIN));
        map.put("Style.REGEX", colorAndStyle("0xcc6600", Style.PLAIN));
        map.put("Style.IDENTIFIER", colorAndStyle("0x000000", Style.PLAIN));
        map.put("Style.DEFAULT", colorAndStyle("0x000000", Style.PLAIN));
        map.put("Style.WARNING", colorAndStyle("0xcc0000", Style.PLAIN));
        map.put("Style.ERROR", colorAndStyle("0xcc0000", Style.BOLD_ITALIC));
        map.put("Style.COMMENT", colorAndStyle("0x339933", Style.PLAIN));
        map.put("Style.COMMENT2", colorAndStyle("0x339933", Style.PLAIN));
        map.put("Style.OPERATOR", colorAndStyle("0x000000", Style.PLAIN));
        map.put("Style.DELIMITER", colorAndStyle("0x000000", Style.BOLD));
    }

    protected static String colorAndStyle(String color, Style style) {
        return color + ", " + style.s;
    }

    protected static String toHex(Color c) {
        String hex = Integer.toHexString(c.getRGB());
        return "#" + hex.substring(2);
    }
}
