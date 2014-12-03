/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meteoinfo.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author yaqiang
 */
public class EncodingUtil {

    private static final Pattern pep263EncodingPattern = Pattern.compile("#.*coding[:=]\\s*([-\\w.]+)");

    public static String matchEncoding(String inputStr) {
        Matcher matcher = pep263EncodingPattern.matcher(inputStr);
        boolean matchFound = matcher.find();

        if ((matchFound) && (matcher.groupCount() == 1)) {
            String groupStr = matcher.group(1);
            return groupStr;
        }
        return null;
    }
}
