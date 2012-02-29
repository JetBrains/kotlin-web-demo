package org.jetbrains.demo.ukhorskaya;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 2/29/12
 * Time: 2:44 PM
 */

public class TextUtils {

    public static String decodeUrl(String str) throws UnsupportedEncodingException {
        str = URLDecoder.decode(str, "UTF-8");
        str = str.replaceAll("@percent@", "%");
        return str;
    }


}
