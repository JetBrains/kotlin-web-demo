package web.view.ukhorskaya;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/27/11
 * Time: 5:50 PM
 */

public class ResponseUtils {
    public static String escapeString(String string) {
        if (string.contains("<")) {
            string = string.replaceAll("<", "&lt;");
        }
        if (string.contains(">")) {
            string = string.replaceAll(">", "&gt;");
        }
        return string;
    }

    public static Project getProjectByName(String projectName) {
        for (Project p : ProjectManager.getInstance().getOpenProjects()) {
            if (p.getName().equals(projectName)) {
                return p;
            }
        }
        return null;
    }

    //Get Color as String
    public static String getColor(Color color) {
        java.util.List<String> colors = new ArrayList<String>();
        colors.add(Long.toHexString(color.getRed()));
        colors.add(Long.toHexString(color.getGreen()));
        colors.add(Long.toHexString(color.getBlue()));

        StringBuilder buffer = new StringBuilder("#");
        for (String c : colors) {
            if (c.length() == 1) {
                buffer.append("0");
            }
            buffer.append(c);
        }
        return (buffer.toString());
    }

    //Get fontType as String
    public static String getFontType(int fontType) {
        switch (fontType) {
            case 1:
                return "font-weight: bold;";
            case 2:
                return "font-style: italic;";
            case 3:
                return "font-style: italic; font-weight: bold;";
            default:
                return "";

        }
    }
}
