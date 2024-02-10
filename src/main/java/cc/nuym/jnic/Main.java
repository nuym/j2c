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
}
