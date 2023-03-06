// rebuild
package cc.nuym.jnic;

import java.util.Iterator;

public class NativeSignature {
    public static void getJNIDefineName(String name, StringBuilder sb) {
        Iterator iterator = ((Iterable)name.chars()::iterator).iterator();
        while (iterator.hasNext()) {
            int cp = (Integer)iterator.next();
            if (cp >= 97 && cp <= 122 || cp >= 65 && cp <= 90 || cp == 95 || cp >= 48 && cp <= 57) {
                sb.append((char)cp);
                continue;
            }
            sb.append('_');
        }
    }

    public static void getJNICompatibleName(String name, StringBuilder sb) {
        for (char c : name.toCharArray()) {
            if (c < 127) {
                switch (c) {
                    case '.':
                    case '/':
                        sb.append('_');
                        break;
                    case '$':
                        sb.append("_00024");
                        break;
                    case '_':
                        sb.append("_1");
                        break;
                    case ';':
                        sb.append("_2");
                        break;
                    case '[':
                        sb.append("_3");
                        break;
                    default:
                        sb.append(c);
                }
            } else {
                sb.append("_0");
                sb.append(String.format("%04x", (int) c));
            }
        }
    }


    public static String getJNICompatibleName(String name) {
        StringBuilder sb = new StringBuilder();
        NativeSignature.getJNICompatibleName(name, sb);
        return sb.toString();
    }
}
