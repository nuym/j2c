package cc.nuym.jnic;

import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * 此类用于处理Java方法名或类名，生成JNI兼容的名称。
 */
public class NativeSignature {

    /**
     * 将给定的方法名转换为JNI定义格式。保留有效的JNI标识符字符（小写字母、大写字母、下划线和数字），
     * 其他字符替换为下划线。
     *
     * @param name   原始Java方法名
     * @param sb     StringBuilder对象，用于构建JNI定义格式的名称
     */
    public static void getJNIDefineName(String name, StringBuilder sb) {
        for (char c : name.toCharArray()) {
            if ((c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') ||
                    c == '_' ||
                    (c >= '0' && c <= '9')) {
                sb.append(c);
            }
        }
    }

    /**
     * 将给定的Java名称转换为JNI兼容格式。将特殊字符转换为下划线及十六进制编码。
     *
     * @param name   原始Java方法名或类名
     * @param sb     StringBuilder对象，用于构建JNI兼容格式的名称
     */
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
                // 对于非ASCII字符，使用下划线和其Unicode编码的十六进制表示
                sb.append("_0");
                sb.append(String.format("%04x", (int) c));
            }
        }
    }

    /**
     * 获取一个给定Java名称的JNI兼容名称。
     *
     * @param name 原始Java方法名或类名
     * @return 返回JNI兼容格式的名称
     */
    public static String getJNICompatibleName(String name) {
        StringBuilder sb = new StringBuilder();
        NativeSignature.getJNICompatibleName(name, sb);
        return sb.toString();
    }
}