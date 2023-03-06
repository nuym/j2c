package cc.nuym.jnic;

import cc.nuym.jnic.env.SetupManager;
import cc.nuym.jnic.utils.ConsoleColors;
import cc.nuym.jnic.utils.DecryptorClass;
import cc.nuym.jnic.utils.StringUtils;
import cc.nuym.jnic.utils.TamperUtils;
import cc.nuym.jnic.xml.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;
import org.apache.commons.compress.utils.IOUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class Main
{
    public static final String VERSION = "2022.1009.05";
    private static final char[] DIGITS;
    
    public static void main(final String[] args) {
        final String processors =
                String.format("%19.19s", "Processors:")
                        + "   "
                        + String.format(
                        "%-19.19s",
                        Runtime.getRuntime().availableProcessors() + " cores"
                );

        final long freeMemory = Math.round(Runtime.getRuntime().freeMemory() / 1E6);
        final String memory =
                String.format("%19.19s", "Current Memory:")
                        + "   "
                        + String.format("%-19.19s", freeMemory + "mb");

        final long maxMemory = Math.round(Runtime.getRuntime().maxMemory() / 1E6);
        final String memoryString = (maxMemory == Long.MAX_VALUE
                ? ConsoleColors.GREEN + "no limit"
                : maxMemory + "mb"
        );
        String topMemory =
                String.format("%19.19s", "Max Memory:")
                        + "   "
                        + String.format("%-19.19s",
                        memoryString + (maxMemory > 1500 ? "" : " ⚠️")
                );

        topMemory = StringUtils.replaceColor(
                topMemory,
                memoryString,
                maxMemory > 1500 ? ConsoleColors.GREEN_BRIGHT : ConsoleColors.RED_BRIGHT
        );
        // slight fix for thing
        topMemory = topMemory.replace("⚠️", "⚠️ ");
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date dat = new Date(System.currentTimeMillis());
        String date = formatter.format(dat);
        System.out.println("                                       _____            __           ");
        System.out.println("                                      |     \\          |  \\          ");
        System.out.println("                                       \\$$$$$ _______   \\$$  _______");
        System.out.println("                                         | $$|       \\ |  \\ /       \\");
        System.out.println("                                    __   | $$| $$$$$$$\\| $$|  $$$$$$$");
        System.out.println("                                   |  \\  | $$| $$  | $$| $$| $$      ");
        System.out.println("                                   | $$__| $$| $$  | $$| $$| $$_____ ");
        System.out.println("                                    \\$$    $$| $$  | $$| $$ \\$$     \\");
        System.out.println("                                     \\$$$$$$  \\$$   \\$$ \\$$  \\$$$$$$$");
        System.out.println("");
        System.out.println("                               ┌───────────────────────────────────────────┐");
        System.out.println("                               │"               + processors             +"  │");
        System.out.println("                               │"               + memory                 +"  │");
        System.out.println("                               └───────────────────────────────────────────┘");
        System.out.println("");
        System.out.println("                          作者: nuym     版本: 2.0.6     现在时间: "+ DateFormat.getDateTimeInstance().format(new Date(Instant.now().toEpochMilli())));
        System.out.println("\n初始化中...");
        SetupManager.init();
        System.out.println("初始化完成!\n");
        System.exit(new CommandLine(new NativeObfuscatorRunner()).setCaseInsensitiveEnumValuesAllowed(true).execute(args));
    }
    
    private static int getLength(final Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }
    
    private static byte[] digest(final byte[] input, final String algorithm, final byte[] salt, final int iterations) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(algorithm);
            if (salt != null) {
                digest.update(salt);
            }
            byte[] result = digest.digest(input);
            for (int i = 1; i < iterations; ++i) {
                digest.reset();
                result = digest.digest(result);
            }
            return result;
        }
        catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String encodeHex(final byte[] input) {
        final int l = input.length;
        final char[] out = new char[l << 1];
        int i = 0;
        int j = 0;
        while (i < l) {
            out[j++] = Main.DIGITS[(0xF0 & input[i]) >>> 4];
            out[j++] = Main.DIGITS[0xF & input[i]];
            ++i;
        }
        return new String(out);
    }
    
    static {
        DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    }
    
    @CommandLine.Command(name = "Jnic", mixinStandardHelpOptions = true, version = { "Jnic Bytecode Translator" }, description = { "将.jar文件翻译成.c文件并生成输出.jar文件" })
    private static class NativeObfuscatorRunner implements Callable<Integer>
    {
        @CommandLine.Parameters(index = "0", description = { "要转译的Jar文件" })
        private File jarFile;
        @CommandLine.Parameters(index = "1", description = { "输出目录" })
        private String outputDirectory;
        @CommandLine.Option(names = { "-c", "--config" }, defaultValue = "config.xml", description = { "Config file" })
        private File config;
        @CommandLine.Option(names = { "-l", "--libraries" }, description = { "依赖库的目录" })
        private File librariesDirectory;
        @CommandLine.Option(names = { "--plain-lib-name" }, description = { "用于加载器的普通库名称" })
        private String libraryName;
        @CommandLine.Option(names = { "-a", "--annotations" }, description = { "使用注解来忽略/包含本地混淆的内容" })
        private boolean useAnnotations;
        
        @Override
        public Integer call() throws Exception {
            System.out.println("读取配置文件:" + this.config.toPath());
            final StringBuilder stringBuilder = new StringBuilder();
            if (Files.exists(this.config.toPath(), new LinkOption[0])) {
                try (final BufferedReader br = Files.newBufferedReader(this.config.toPath())) {
                    String str;
                    while ((str = br.readLine()) != null) {
                        stringBuilder.append(str);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                final ObjectMapper objectMapper = new XmlMapper();
                final Config configInfo;
                configInfo = objectMapper.readValue(stringBuilder.toString(), Config.class);
                final List<Path> libs = new ArrayList<Path>();
                if (this.librariesDirectory != null) {
                    Files.walk(this.librariesDirectory.toPath(), FileVisitOption.FOLLOW_LINKS).filter(f -> f.toString().endsWith(".jar") || f.toString().endsWith(".zip")).forEach(libs::add);
                }
                if (new File(this.outputDirectory).isDirectory()) {
                    final File outFile = new File(this.outputDirectory, this.jarFile.getName());
                    if (outFile.exists()) {
                        outFile.renameTo(new File(this.outputDirectory, this.jarFile.getName() + ".BACKUP"));
                    }
                }
                else {
                    final File outFile = new File(this.outputDirectory);
                    if (outFile.exists()) {
                        outFile.renameTo(new File(this.outputDirectory + ".BACKUP"));
                    }
                }
                new NativeObfuscator().process(this.jarFile.toPath(), Paths.get(this.outputDirectory, new String[0]), configInfo, libs, this.libraryName, this.useAnnotations);
                return 0;
            }
            final Path path = Files.createFile(this.config.toPath(), (FileAttribute<?>[])new FileAttribute[0]);
            stringBuilder.append("<jnic>\n\t<targets>\n\t\t<target>WINDOWS_X86_64</target>\n\t\t<!--<target>WINDOWS_AARCH64</target>\n\t\t<target>MACOS_X86_64</target>\n\t\t<target>MACOS_AARCH64</target>-->\n\t\t<target>LINUX_X86_64</target>\n\t\t<!--<target>LINUX_AARCH64</target>-->\n\t</targets>\n\t<include>\n\t\t<!-- match支持 Ant 风格的路径匹配 ? 匹配一个字符, * 匹配多个字符, ** 匹配多层路径 -->\n\t\t<match className=\"**\" />\n\t\t<!--<match className=\"cn/jnic/web/**\" />-->\n\t\t<!--<match className=\"cn.jnic.service.**\" />-->\n\t</include>\n\t<exclude>\n\t\t<!--<match className=\"cn/jnic/Main\" methodName=\"main\" methodDesc=\"(\\[Ljava/lang/String;)V\"/>-->\n\t\t<!--<match className=\"cn.jnic.test.**\" />-->\n\t</exclude>\n</jnic>\n");
            Files.write(path, stringBuilder.toString().getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            System.out.println("无法读取配置文件，自动生成的默认配置文件。");
            System.out.println("Jnic现在将退出，请在编辑配置文件后再次运行。");
            return 0;
        }
    }

    // stringenc-antitamper https://github.com/ItzSomebody/stringenc-antitamper
    private static void obf(File input, File output) throws Throwable {
        ZipFile zipFile = new ZipFile(input);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
        Map<String, ClassNode> classes = new HashMap<>();
        long current = System.currentTimeMillis();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if (entry.getName().endsWith(".class")) {
                ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
                ClassNode classNode = new ClassNode();

                cr.accept(classNode, 0);
                classes.put(classNode.name, classNode);
            } else {
                ZipEntry newEntry = new ZipEntry(entry);
                newEntry.setTime(current);
                zos.putNextEntry(newEntry);
                zos.write(IOUtils.toByteArray(zipFile.getInputStream(entry)));
            }
        }
        List<String> classNames = new ArrayList<>(classes.keySet());

        String decryptClassName = TamperUtils.randomClassName(classNames);
        String decryptMethodName = TamperUtils.randomString();

        classes.values().forEach(classNode -> {
            Set<MethodNode> toProcess = new HashSet<>();

            classNode.methods.stream().filter(TamperUtils::hasInstructions).forEach(methodNode -> {
                for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
                    if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof String) {
                        methodNode.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                decryptClassName, decryptMethodName, "(Ljava/lang/Object;)Ljava/lang/String;", false));
                        toProcess.add(methodNode);
                    }
                }
            });

            ClassWriter cw = new ClassWriter(0);
            classNode.accept(cw);
            ClassReader cr = new ClassReader(cw.toByteArray());

            toProcess.forEach(methodNode -> Stream.of(methodNode.instructions.toArray()).filter(TamperUtils::isString).forEach(insn -> {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                ldc.cst = TamperUtils.encrypt((String) ldc.cst, classNode.name.replace("/", "."), methodNode.name, cr.getItemCount() + 20);
            }));
        });

        classes.values().forEach(classNode -> {
            try {
                ClassWriter cw = new ClassWriter(0);
                classNode.accept(cw);
                for (int i = 0; i < 20; i++)
                    cw.newUTF8(TamperUtils.randomString());

                ZipEntry newEntry = new ZipEntry(classNode.name + ".class");
                newEntry.setTime(current);

                zos.putNextEntry(newEntry);
                zos.write(cw.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        byte[] decryptionBytes = new DecryptorClass(decryptClassName, TamperUtils.randomString(), TamperUtils.randomString(),
                decryptMethodName).getBytes();

        ZipEntry newEntry = new ZipEntry(decryptClassName + ".class");
        newEntry.setTime(current);

        zos.putNextEntry(newEntry);
        zos.write(decryptionBytes);
        zos.close();
    }
}
