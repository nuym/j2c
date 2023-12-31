package cc.nuym.jnic;

import cc.nuym.jnic.env.SetupManager;
import cc.nuym.jnic.utils.DecryptorClass;
import cc.nuym.jnic.utils.TamperUtils;
import cc.nuym.jnic.xml.Config;
import org.apache.commons.compress.utils.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import picocli.CommandLine;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class Main
{
    private static final char[] DIGITS;
    private File inputFile;
    static File output;



    public static void main(final String[] args) {
        System.out.println("\n");
        System.out.println("JNIC Java to C translator 3.6.1");
        System.out.println(" ~ (c) +Vincent Tang 2020-2023");
        System.out.println("\n");
        System.out.println("License: nuym (Enterprise)");
        SetupManager.init();
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
        @CommandLine.Parameters(index = "0", description = "Jar file to transpile")
        private File jarFile;

        @CommandLine.Parameters(index = "1", description = "Output directory")
        private String outputDirectory;

        @CommandLine.Option(names = {"-c", "--config"}, defaultValue = "config.xml",
                description = "Config file")
        private File config;

        @CommandLine.Option(names = {"-l", "--libraries"}, description = "Directory for dependent libraries")
        private File librariesDirectory;

        @CommandLine.Option(names = {"-a", "--annotations"}, description = "Use annotations to ignore/include native obfuscation")
        private boolean useAnnotations;
        @CommandLine.Option(names = { "--plain-lib-name" }, description = { "Common library name to be used for the loader" })
        private String libraryName;
        
        @Override
        public Integer call() throws Exception {
            System.out.println("Reading input jar " + this.jarFile);
            System.out.println("Reading configuration file " + this.config.toPath());
            final StringBuilder stringBuilder = new StringBuilder();
            if (Files.exists(this.config.toPath())) {
                try (final BufferedReader br = Files.newBufferedReader(this.config.toPath())) {
                    String str;
                    while ((str = br.readLine()) != null) {
                        stringBuilder.append(str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Serializer serializer = new Persister();
                Config configInfo = serializer.read(Config.class, stringBuilder.toString());
                final List<Path> libs = new ArrayList<Path>();
                if (this.librariesDirectory != null) {
                    Files.walk(this.librariesDirectory.toPath(), FileVisitOption.FOLLOW_LINKS).filter(f -> f.toString().endsWith(".jar") || f.toString().endsWith(".zip")).forEach(libs::add);
                }
                if (new File(this.outputDirectory).isDirectory()) {
                    final File outFile = new File(this.outputDirectory, this.jarFile.getName());
                    if (outFile.exists()) {
                        outFile.renameTo(new File(this.outputDirectory, this.jarFile.getName() + ".BACKUP"));
                    }
                } else {
                    final File outFile = new File(this.outputDirectory);
                    if (outFile.exists()) {
                        outFile.renameTo(new File(this.outputDirectory + ".BACKUP"));
                    }
                }
                //开始处理
                new NativeObfuscator().process(this.jarFile.toPath(), Paths.get(this.outputDirectory), configInfo, libs, this.libraryName, this.useAnnotations);
                /*
                try {
                    String jarName = this.jarFile.getName().substring(0, this.jarFile.getName().length() - 4);
                    obf(new File(outputDirectory + "\\" + this.jarFile.getName()), new File(outputDirectory + "\\" + jarName + "-enc.jar"));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }

                 */
                return 0;
            }

            final Path path = Files.createFile(this.config.toPath(), (FileAttribute<?>[])new FileAttribute[0]);
            stringBuilder.append("<jnic>\n" +
                    "\t<targets>\n" +
                    "\t\t<target>WINDOWS_X86_64</target>\n" +
                    "\t\t<!--<target>WINDOWS_AARCH64</target>-->\n" +
                    "\t\t<target>MACOS_X86_64</target>\n" +
                    "\t\t<!--<target>MACOS_AARCH64</target>-->\n" +
                    "\t\t<target>LINUX_X86_64</target>\n" +
                    "\t\t<!--<target>LINUX_AARCH64</target>-->\n" +
                    "\t</targets>\n" +
                    "\t<options>\n" +
                    "\t\t<!--String obfuscation-->\n" +
                    "\t\t<stringObf>false</stringObf>\n" +
                    "\t\t<!--Control flow obfuscation-->\n" +
                    "\t\t<flowObf>false</flowObf>\n" +
                    "\t</options>" +
                    "\t<include>\n" +
                    "\t\t<!-- Match supports Ant style path matching? Match one character, * match multiple characters, * * match multiple paths -->\n" +
                    "\t\t<match className=\"**\" />\n" +
                    "\t\t<!--<match className=\"dev/jnic/web/**\" />-->\n" +
                    "\t\t<!--<match className=\"dev.jnic.service.**\" />-->\n" +
                    "\t</include>\n" +
                    "\t<exclude>\n" +
                    "\t\t<!--<match className=\"dev/jnic/Main\" methodName=\"main\" methodDesc=\"(\\[Ljava/lang/String;)V\"/>-->\n" +
                    "\t\t<!--<match className=\"dev.jnic.test.**\" />-->\n" +
                    "\t</exclude>\n" +
                    "</jnic>\n");
            Files.write(path, stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Unable to read configuration file. Default config has been generated for you");
            System.out.println("The default configuration compiles all classes and methods, which will seriously affect the running performance of the program. Please use this function with caution");
            System.out.println("Please open the configuration file, configure the compiled classes and methods, and then continue to run the command");
            return 0;
        }
    }

    // DO NOT SUPPORT JDK 1.9+!!!! BE-CAREFUL

    private static void FolderObf(File input, File output) throws Throwable {
        try (ZipInputStream zipInput = new ZipInputStream(Files.newInputStream(input.toPath()));
             ZipOutputStream zipOutput = new ZipOutputStream(Files.newOutputStream(output.toPath()))) {

            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // 获取类名
                    String name = entry.getName();
                    String className = name.replaceAll("/", ".").replaceAll("\\.class", "");

                    // 对类名进行处理
                    className += "/";
                    name = name.substring(0, name.lastIndexOf("/") + 1) + className + "class";

                    // 写入ZipEntry
                    zipOutput.putNextEntry(new ZipEntry(name));
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInput.read(buffer)) > 0) {
                        zipOutput.write(buffer, 0, len);
                    }
                    zipOutput.closeEntry();
                } else {
                    // 写入目录
                    zipOutput.putNextEntry(entry);
                    zipOutput.closeEntry();
                }
            }
        }
    }
    private static void obf(File input, File output) throws Throwable {
        ZipFile zipFile = new ZipFile(input);
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(output.toPath()), StandardCharsets.UTF_8);
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

            toProcess.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray()).filter(TamperUtils::isString).forEach(insn -> {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                ldc.cst = TamperUtils.encrypt((String) ldc.cst, classNode.name.replace("/", "."), methodNode.name, cr.getItemCount() + 20);
            }));
        });

        classes.values().forEach(classNode -> {
            try {
                ClassWriter cw = new ClassWriter(0);
                classNode.accept(cw);
                for (int i = 0; i < 20; i++)
                    cw.newConst(TamperUtils.randomString());

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

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date dat = new Date(System.currentTimeMillis());
        String date = formatter.format(dat);

        zos.putNextEntry(newEntry);
        zos.write(decryptionBytes);
        zos.setComment("Jnic is a powerful native Java bytecode obfuscator made by nuym.\nObfuscation time: "+date+"\nContact:1006800345@qq.com\nCatch me if you can!");
        zos.close();
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
        {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
    /*
    private static void FakeDirectory(File inputFile)
    {
        nputFile = inputFile;

        if(!inputFile.exists() || inputFile.isDirectory())
        {
            System.err.println("File '" + inputFile.getAbsolutePath() + "' couldn't be found!");
        }

        Instant start = Instant.now();

        JarFile inputJar;
        JarOutputStream jarOutputStream;
        try
        {
            inputJar = new JarFile(inputFile);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error while reading JarFile", e);
        }

        try
        {
            jarOutputStream = new JarOutputStream(
                    new FileOutputStream(
                            new File(
                                    inputFile.getAbsolutePath().replace(".jar", "-obf.jar").replace(".zip", "-obf.zip")
                            )
                    )
            );
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error while opening outputs stream", e);
        }

        Enumeration<JarEntry> entries = inputJar.entries();

        while(entries.hasMoreElements())
        {
            JarEntry inEntry = entries.nextElement();
            String name = inEntry.getName();

            try
            {
                if(name.endsWith(".class"))
                    name += "/";

                JarEntry outEntry = new JarEntry(name);
                jarOutputStream.putNextEntry(outEntry);
                jarOutputStream.write(getBytesFromInputStream(inputJar.getInputStream(inEntry)));
                jarOutputStream.closeEntry();
            }
            catch(Exception e)
            {
                new RuntimeException("Error while writing entry '" + name + "'", e).printStackTrace();
            }
        }

        try
        {
            inputJar.close();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error while closing input", e);
        }

        try
        {
            jarOutputStream.close();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error while saving output", e);
        }

        System.out.println("Finished in " + Duration.between(start, Instant.now()).toMillis() + "ms");
    }

     */
}
