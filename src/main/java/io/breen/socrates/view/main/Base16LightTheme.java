package io.breen.socrates.view.main;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing default style values for the base16 light theme by
 * Chris Kempson (http://chriskempson.github.io/base16/#default).
 */
public class Base16LightTheme extends DefaultTheme {

    public static final Map<String, String> map;
    public static final Color backgroundColor;
    public static final Color foregroundColor;

    static {
        backgroundColor = Color.decode("#f8f8f8");
        foregroundColor = Color.decode("#383838");

        map = new HashMap<>();

        map.put("SelectionColor", toHex(Color.decode("#f8f8f8")));

        map.put("DefaultFont", "Monospaced-PLAIN-12");

        map.put("LineNumbers.RightMargin", "10");
        map.put("LineNumbers.Foreground", "#b8b8b8");
        map.put("LineNumbers.Background", toHex(backgroundColor));
        map.put("LineNumbers.CurrentBack", toHex(backgroundColor));

        map.put("Style.KEYWORD", colorAndStyle("0xba8baf", Style.PLAIN));
        map.put("Style.KEYWORD2", colorAndStyle("0xba8baf", Style.BOLD));
        map.put("Style.TYPE", colorAndStyle("0xdc9656", Style.PLAIN));
        map.put("Style.TYPE2", colorAndStyle("0xdc9656", Style.ITALIC));
        map.put("Style.TYPE3", colorAndStyle("0xdc9656", Style.BOLD_ITALIC));
        map.put("Style.STRING", colorAndStyle("0xa1b56c", Style.PLAIN));
        map.put("Style.STRING2", colorAndStyle("0xa1b56c", Style.BOLD));
        map.put("Style.NUMBER", colorAndStyle("0xdc9656", Style.PLAIN));
        map.put("Style.REGEX", colorAndStyle("0x86c1b9", Style.PLAIN));
        map.put("Style.IDENTIFIER", colorAndStyle("0xab4642", Style.PLAIN));
        map.put("Style.DEFAULT", colorAndStyle("0x383838", Style.PLAIN));
        map.put("Style.WARNING", colorAndStyle("0xab4642", Style.PLAIN));
        map.put("Style.ERROR", colorAndStyle("0xab4642", Style.BOLD));
        map.put("Style.COMMENT", colorAndStyle("0xb8b8b8", Style.PLAIN));
        map.put("Style.COMMENT2", colorAndStyle("0xb8b8b8", Style.ITALIC));
        map.put("Style.OPERATOR", colorAndStyle("0x383838", Style.PLAIN));
        map.put("Style.DELIMITER", colorAndStyle("0x383838", Style.PLAIN));
    }
}
