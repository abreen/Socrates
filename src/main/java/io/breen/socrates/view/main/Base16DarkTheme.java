package io.breen.socrates.view.main;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing default style values for the base16 dark theme by Chris Kempson
 * (http://chriskempson.github.io/base16/#default).
 */
public class Base16DarkTheme extends Base16LightTheme {

    public static final Map<String, String> map;
    public static final Color backgroundColor;
    public static final Color foregroundColor;

    static {
        backgroundColor = Color.decode("#282828");
        foregroundColor = Color.decode("#585858");

        map = new HashMap<>(Base16LightTheme.map);

        map.put("SelectionColor", toHex(Color.decode("#383838")));

        map.put("LineNumbers.Foreground", "#585858");
        map.put("LineNumbers.Background", toHex(backgroundColor));
        map.put("LineNumbers.CurrentBack", toHex(backgroundColor));
        map.put("Style.TYPE", colorAndStyle("0xf7ca88", Style.PLAIN));
        map.put("Style.TYPE2", colorAndStyle("0xf7ca88", Style.ITALIC));
        map.put("Style.TYPE3", colorAndStyle("0xf7ca88", Style.BOLD_ITALIC));
        map.put("Style.DEFAULT", colorAndStyle("0x585858", Style.PLAIN));
        map.put("Style.COMMENT", colorAndStyle("0x585858", Style.PLAIN));
        map.put("Style.COMMENT2", colorAndStyle("0x585858", Style.ITALIC));
        map.put("Style.OPERATOR", colorAndStyle("0xd8d8d8", Style.PLAIN));
        map.put("Style.DELIMITER", colorAndStyle("0xd8d8d8", Style.PLAIN));
    }
}
