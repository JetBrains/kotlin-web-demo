package web.view.ukhorskaya.css;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.Nullable;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.handlers.BaseHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 9/22/11
 * Time: 5:05 PM
 */

public class GlobalCssMap {

    private static final GlobalCssMap MAP = new GlobalCssMap();

    private final Map<MyTextAttributes, String> mapAttributes = new HashMap<MyTextAttributes, String>();
    private List<Color> foregroundColors = new ArrayList<Color>();
    private List<Color> lineColors = new ArrayList<Color>();
    private List<Color> backgroundColors = new ArrayList<Color>();
    private List<EffectType> effectTypes = new ArrayList<EffectType>();

    private List<Integer> fontTypes = new ArrayList<Integer>();

    long startTime = System.currentTimeMillis();

    public static GlobalCssMap getInstance() {
        return MAP;
    }

    private GlobalCssMap() {
        fillCssMap();
    }

    public String getClassFromTextAttribute(TextAttributes attr) {
        return mapAttributes.get(new MyTextAttributes(attr));
    }

    private void fillCssMap() {

        putColors();
        putEffectTypes();
        putFontTypes();
        System.out.println("put to map " + (System.currentTimeMillis() - startTime));
        int i = 0;
        for (Color c : foregroundColors) {
            for (Color b : backgroundColors) {
                for (EffectType t : effectTypes) {
                    for (Integer f : fontTypes) {

                        if (t.equals(EffectType.WAVE_UNDERSCORE)) {
                            for (Color lc : lineColors) {
                                addToMap(i, c, b, t, f, lc);
                                i++;
                            }
                        } else {
                            addToMap(i, c, b, t, f, null);
                            i++;
                        }

                    }
                }
            }
        }
        System.out.println("generate map size=" + i + " - " + (System.currentTimeMillis() - startTime));
        //generateCssStyles();
    }

    private void addToMap(int i, Color c, Color b, EffectType t, int f, @Nullable Color lc) {
        TextAttributes attr = new TextAttributes();
        attr.setForegroundColor(c);
        attr.setBackgroundColor(b);
        attr.setEffectType(t);
        attr.setFontType(f);
        attr.setEffectColor(lc);
        mapAttributes.put(new MyTextAttributes(attr), "class" + i);
    }


    private void putFontTypes() {
        fontTypes.add(0);
        fontTypes.add(1);  //bold
        fontTypes.add(2);  //italic
        fontTypes.add(3); //bold + italic
    }

    private void putEffectTypes() {
        effectTypes.add(EffectType.BOXED);
        effectTypes.add(EffectType.WAVE_UNDERSCORE);
        effectTypes.add(EffectType.LINE_UNDERSCORE);
    }

    private void putColors() {
        //backgroundColors.add(new Color(255, 255, 215));
        backgroundColors.add(new Color(255, 255, 255));
        //backgroundColors.add(new Color(255, 255, 204));
        //backgroundColors.add(new Color(255, 204, 204));
        //backgroundColors.add(new Color(226, 255, 226));
        backgroundColors.add(new Color(246, 235, 188)); //light yellow (unused variable)
        //backgroundColors.add(new Color(239, 239, 239));
        //backgroundColors.add(new Color(237, 252, 237));
        //backgroundColors.add(new Color(247, 233, 233));

        foregroundColors.add(new Color(0, 0, 0));
        //foregroundColors.add(new Color(0, 0, 255)); //blue
        //foregroundColors.add(new Color(102, 14, 122)); //violet (System.out)
        foregroundColors.add(new Color(255, 0, 0)); //red
        //foregroundColors.add(new Color(0, 0, 128)); //navy
        foregroundColors.add(new Color(128, 128, 128));
        //foregroundColors.add(new Color(0, 255, 0));
        //foregroundColors.add(new Color(128, 128, 0)); //olive
        foregroundColors.add(new Color(0, 128, 0)); //green ("aaa")
        //foregroundColors.add(new Color(69, 131, 131));
        //foregroundColors.add(new Color(122, 122, 43));

        lineColors.add(new Color(255, 0, 0)); //red
        lineColors.add(new Color(0, 128, 0));

    }


    //Generate css-file
    private void generateCssStyles() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("body { font-family: monospace; font-size: 12px; color: #000000; background-color: #FFFFFF;} ");
        buffer.append(" a {text-decoration: none; color: #000000;} span.highlighting { background-color: yellow !important;}");
        buffer.append(" a span {text-decoration: none; color: #000000;} a:hover span {color: blue; text-decoration: underline;}");
        for (MyTextAttributes attr : mapAttributes.keySet()) {
            buffer.append("\nspan.");
            buffer.append(mapAttributes.get(attr)).append("{");
            if (!attr.getTextAttributes().getBackgroundColor().equals(new Color(0, 0, 0))) {
                buffer.append("color: ").append(ResponseUtils.getColor(attr.getTextAttributes().getForegroundColor())).append(" !important; ");
            }

            buffer.append(ResponseUtils.getFontType(attr.getTextAttributes().getFontType())).append(" ");
            if (attr.getTextAttributes().getEffectType() == EffectType.LINE_UNDERSCORE) {
                buffer.append("text-decoration: underline; ").append(";");
            }
            if (attr.getTextAttributes().getEffectType() == EffectType.WAVE_UNDERSCORE) {
                if (attr.getTextAttributes().getEffectColor().equals(new Color(255, 0, 0))) {
                    buffer.append("background: url(/wavyline-red.gif) repeat-x 100% 100% !important; padding-bottom: 2px; ").append("; ");
                } else if (attr.getTextAttributes().getEffectColor().equals(new Color(0, 128, 0))) {
                    buffer.append("background: url(/wavyline-green.gif) repeat-x 100% 100% !important; padding-bottom: 2px; ").append("; ");
                }
            }
            if (!attr.getTextAttributes().getBackgroundColor().equals(new Color(255, 255, 255))) {
                buffer.append("background-color: ").append(ResponseUtils.getColor(attr.getTextAttributes().getBackgroundColor())).append(" !important; ");
            }
            buffer.append("}");

            //Cut empty styles
            String tmp = "\nspan." + mapAttributes.get(attr) + "{ }";
            int position = buffer.toString().indexOf(tmp);
            if (position != -1) {
                buffer = buffer.delete(position, buffer.length());
            }
        }
        System.out.println("genearte string with css " + (System.currentTimeMillis() - startTime));
        System.out.println(buffer);
        System.out.println("print css " + (System.currentTimeMillis() - startTime));
    }

    private static class MyTextAttributes {
        private TextAttributes myAttributes;

        public MyTextAttributes(TextAttributes attributes) {
            myAttributes = attributes;
            if (myAttributes.getBackgroundColor() == null) {
                myAttributes.setBackgroundColor(new Color(255, 255, 255));
            }
            if (myAttributes.getForegroundColor() == null) {
                myAttributes.setForegroundColor(new Color(0, 0, 0));
            }
        }

        public TextAttributes getTextAttributes() {
            return myAttributes;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof MyTextAttributes)) {
                return false;
            }
            MyTextAttributes newTextAttributes = (MyTextAttributes) object;
            if (myAttributes.equals(newTextAttributes.getTextAttributes())) {
                return true;
            }
            boolean result = (myAttributes.getFontType() == newTextAttributes.getTextAttributes().getFontType()) &&
                    (myAttributes.getForegroundColor().equals(newTextAttributes.getTextAttributes().getForegroundColor())) &&
                    (myAttributes.getBackgroundColor().equals(newTextAttributes.getTextAttributes().getBackgroundColor())) &&
                    (myAttributes.getEffectType() == newTextAttributes.getTextAttributes().getEffectType());

            if (((myAttributes.getEffectColor() != null) && (newTextAttributes.getTextAttributes().getEffectColor() != null) && (myAttributes.getEffectColor().equals(newTextAttributes.getTextAttributes().getEffectColor())) && result) ||
                ((myAttributes.getEffectColor() == null) && (newTextAttributes.getTextAttributes().getEffectColor() == null) && result)) {
                return true;
            }
            /*if (!((myAttributes.getEffectColor() != null)
                    && (newTextAttributes.getTextAttributes().getEffectColor() != null)
                    && (myAttributes.getEffectColor().equals(newTextAttributes.getTextAttributes().getEffectColor()))
            ) || !((myAttributes.getEffectColor() == null) && (newTextAttributes.getTextAttributes().getEffectColor() == null))) {
                result = false;
            }*/
            //return result;
            return false;
        }

        @Override
        public int hashCode() {
            int hashCode = 0;
            if (myAttributes.getBackgroundColor() != null) {
                hashCode += myAttributes.getBackgroundColor().hashCode();
            }
            if (myAttributes.getForegroundColor() != null) {
                hashCode += myAttributes.getForegroundColor().hashCode();
            }
            hashCode += myAttributes.getEffectType().hashCode() + myAttributes.getFontType();
            if (myAttributes.getEffectColor() != null) {
                hashCode += myAttributes.getEffectColor().hashCode();
            }
            return hashCode;
        }


    }
}
