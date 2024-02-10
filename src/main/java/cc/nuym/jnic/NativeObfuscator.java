// rebuild
package cc.nuym.jnic;

import cc.nuym.jnic.asm.ClassMetadataReader;
import cc.nuym.jnic.asm.SafeClassWriter;
import cc.nuym.jnic.cache.*;
import cc.nuym.jnic.env.SetupManager;
import cc.nuym.jnic.helpers.ProcessHelper;
import cc.nuym.jnic.utils.*;
import cc.nuym.jnic.xml.Config;
import cc.nuym.jnic.xml.Match;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NativeObfuscator {
    private final Snippets snippets;
    private InterfaceStaticClassProvider staticClassProvider;
    private final MethodProcessor methodProcessor;
    private final NodeCache<String> cachedStrings;
    private final ClassNodeCache cachedClasses;
    private final MethodNodeCache cachedMethods;
    private final FieldNodeCache cachedFields;
    private final Map<String, String> classMethodNameMap = new HashMap<>();
    private final Map<String, String> noInitClassMap = new HashMap<>();
    private StringBuilder nativeMethods;
    private BootstrapMethodsPool bootstrapMethodsPool;
    private int currentClassId;
    private int methodIndex;
    private String nativeDir;
    private static final String separator = File.separator;

    public NativeObfuscator() {
        this.snippets = new Snippets();
        this.cachedStrings = new NodeCache<>();
        this.cachedClasses = new ClassNodeCache("(cclasses[%d])");
        this.cachedMethods = new MethodNodeCache("(cmethods[%d])", this.cachedClasses);
        this.cachedFields = new FieldNodeCache("(cfields[%d])", this.cachedClasses);
        this.methodProcessor = new MethodProcessor(this);
    }

    public void process(Path inputJarPath, Path output, Config config, List<Path> inputLibs, String plainLibName, boolean useAnnotations) throws IOException {
        Path outputDir;
        ArrayList<Path> libs = new ArrayList<>(inputLibs);
        libs.add(inputJarPath);
        ArrayList<String> whiteList = new ArrayList<>();
        if (config.getIncludes() != null) {
            for (Match match : config.getIncludes()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (StringUtils.isNotEmpty(match.getClassName())) {
                    stringBuilder.append(match.getClassName().replaceAll("\\.", "/"));
                }
                if (StringUtils.isNotEmpty(match.getMethodName())) {
                    stringBuilder.append("#").append(match.getMethodName());
                    if (StringUtils.isNotEmpty(match.getMethodDesc())) {
                        stringBuilder.append("!").append(match.getMethodDesc());
                    }
                } else if (StringUtils.isNotEmpty(match.getMethodDesc())) {
                    stringBuilder.append("#**!").append(match.getMethodDesc());
                }
                whiteList.add(stringBuilder.toString());
            }
        }
        ArrayList<String> blackList = new ArrayList<>();
        if (config.getExcludes() != null) {
            for (Match include : config.getExcludes()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (StringUtils.isNotEmpty(include.getClassName())) {
                    stringBuilder.append(include.getClassName().replaceAll("\\.", "/"));
                }
                if (StringUtils.isNotEmpty(include.getMethodName())) {
                    stringBuilder.append("#").append(include.getMethodName());
                    if (StringUtils.isNotEmpty(include.getMethodDesc())) {
                        stringBuilder.append("!").append(include.getMethodDesc());
                    }
                } else if (StringUtils.isNotEmpty(include.getMethodDesc())) {
                    stringBuilder.append("#**!").append(include.getMethodDesc());
                }
                blackList.add(stringBuilder.toString());
            }
        }
        ClassMethodFilter classMethodFilter = new ClassMethodFilter(blackList, whiteList, useAnnotations);
        ClassMetadataReader metadataReader = new ClassMetadataReader(libs.stream().map(x -> {
            try {
                return new JarFile(x.toFile());
            }
            catch (IOException ex) {
                return null;
            }
        }).collect(Collectors.toList()));
        String outputName = inputJarPath.getFileName().toString();

        if (!output.toFile().exists()) {
            Files.createDirectory(output);
        }

        if (output.toFile().isDirectory()) {
            outputDir = output;
        } else {
            outputDir = output.getParent();
            outputName = output.getFileName().toString();
        }
        Path cppDir = outputDir.resolve("cpp");
        Files.createDirectories(cppDir);
        Util.copyResource("jni.h", cppDir);
        HashMap<String, ClassNode> map = new HashMap<>();
        HashMap<String, String> classNameMap = new HashMap<>();
        StringBuilder instructions = new StringBuilder();
        File jarFile = inputJarPath.toAbsolutePath().toFile();
        Path temp = Files.createTempDirectory("native-jnic-");
        Path tempFile = temp.resolve(UUID.randomUUID() + ".data");
        Path encryptFile = temp.resolve(UUID.randomUUID() + ".data");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(outputName)));
             ZipOutputStream source = new ZipOutputStream(Files.newOutputStream(tempFile))){
            JarFile jar = new JarFile(jarFile);
            System.out.println("Anylysing classes...");
            String nativeNonDir = "dev/jnic/";

            this.nativeDir = nativeNonDir + NativeObfuscator.getRandomString();

            this.bootstrapMethodsPool = new BootstrapMethodsPool(this.nativeDir);
            this.staticClassProvider = new InterfaceStaticClassProvider(this.nativeDir);
            this.methodIndex = 1;
            jar.stream().forEach(entry -> {
                block17: {
                    try {
                        if (!entry.getName().endsWith(".class"))
                            break block17;

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (InputStream in = jar.getInputStream(entry)){
                            Util.transfer(in, baos);
                        }
                        byte[] src = baos.toByteArray();
                        ClassReader classReader = new ClassReader(src);
                        ClassNode classNode = new ClassNode(458752);
                        classReader.accept(classNode, 0);
                        if (classMethodFilter.shouldProcess(classNode)) {
                            boolean isStatic;
                            Map<String, CachedClassInfo> cache = this.cachedClasses.getCache();
                            CachedClassInfo classInfo = new CachedClassInfo(classNode.name, classNode.name, "", cache.size(), Util.getFlag(classNode.access, 8));
                            for (FieldNode field : classNode.fields) {
                                isStatic = Util.getFlag(field.access, 8);
                                CachedFieldInfo cachedFieldInfo = new CachedFieldInfo(classNode.name, field.name, field.desc, isStatic);
                                classInfo.addCachedField(cachedFieldInfo);
                            }
                            for (MethodNode method : classNode.methods) {
                                if ("<clinit>".equals(method.name)) continue;
                                isStatic = Util.getFlag(method.access, 8);
                                CachedMethodInfo cachedMethodInfo = new CachedMethodInfo(classNode.name, method.name, method.desc, isStatic);
                                classInfo.addCachedMethod(cachedMethodInfo);
                            }
                            cache.put(classNode.name, classInfo);
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            AtomicInteger classNumber = new AtomicInteger();
            AtomicInteger methodNumber = new AtomicInteger();
            jar.stream().forEach(entry -> {

                if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                    return;
                }

                try {
                    if (!entry.getName().endsWith(".class")) {
                        Util.writeEntry(jar, out, entry);
                        return;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream in = jar.getInputStream(entry)){
                        Util.transfer(in, baos);
                    }
                    byte[] src = baos.toByteArray();
                    if (Util.byteArrayToInt(Arrays.copyOfRange(src, 0, 4)) != -889275714) {
                        Util.writeEntry(out, entry.getName(), src);
                        return;
                    }
                    this.nativeMethods = new StringBuilder();
                    ClassReader classReader = new ClassReader(src);
                    ClassNode rawClassNode = new ClassNode(458752);
                    classReader.accept(rawClassNode, 2);
                    if (!classMethodFilter.shouldProcess(rawClassNode) || rawClassNode.methods.stream().noneMatch(method -> MethodProcessor.shouldProcess(method) && classMethodFilter.shouldProcess(rawClassNode, method))) {
                        if (useAnnotations) {
                            ClassMethodFilter.cleanAnnotations(rawClassNode);
                            SafeClassWriter clearedClassWriter = new SafeClassWriter(metadataReader, 458752);
                            rawClassNode.accept(clearedClassWriter);
                            Util.writeEntry(out, entry.getName(), clearedClassWriter.toByteArray());
                            return;
                        }
                        Util.writeEntry(out, entry.getName(), src);
                        return;
                    }
                    Util.writeEntry(jar, source, entry);
                    classNumber.getAndIncrement();
                    SafeClassWriter preprocessorClassWriter = new SafeClassWriter(metadataReader, 458755);
                    rawClassNode.accept(preprocessorClassWriter);
                    classReader = new ClassReader(preprocessorClassWriter.toByteArray());
                    ClassNode classNode = new ClassNode(458752);
                    classReader.accept(classNode, 0);
                    if (classNode.methods.stream().noneMatch(x -> x.name.equals("<clinit>"))) {
                        classNode.methods.add(new MethodNode(458752, 8, "<clinit>", "()V", null, new String[0]));
                    }
                    this.staticClassProvider.newClass();
                    map.put(classNode.name, classNode);
                    classNameMap.put(classNode.name, classReader.getClassName());
                    instructions.append("\n//").append(classNode.name).append("\n");
                    for (int i = 0; i < classNode.methods.size(); ++i) {
                        MethodNode method2 = classNode.methods.get(i);
                        if (!MethodProcessor.shouldProcess(method2) || !classMethodFilter.shouldProcess(classNode, method2) && !"<clinit>".equals(method2.name)) continue;
                        MethodContext context = new MethodContext(this, method2, this.methodIndex, classNode, this.currentClassId);
                        this.methodProcessor.processMethod(context);
                        instructions.append(context.output.toString().replace("\n", "\n    "));
                        this.nativeMethods.append(context.nativeMethods);
                        if ((classNode.access & 0x200) > 0) {
                            method2.access &= 0xFFFFFEFF;
                        }
                        ++this.methodIndex;
                        if ("<clinit>".equals(method2.name)) continue;
                        methodNumber.getAndIncrement();
                    }
                    if (!this.staticClassProvider.isEmpty()) {
                        this.cachedStrings.getPointer(this.staticClassProvider.getCurrentClassName().replace('/', '.'));
                    }
                    if (useAnnotations) {
                        ClassMethodFilter.cleanAnnotations(classNode);
                    }
                    classNode.visitMethod(4361, "$jnicLoader", "()V", null, new String[0]);
                    classNode.version = 52;
                    SafeClassWriter classWriter = new SafeClassWriter(metadataReader, 458755);
                    classNode.accept(classWriter);
                    Util.writeEntry(out, entry.getName(), classWriter.toByteArray());
                    ++this.currentClassId;
                    //Util.class2folder(jar,out,entry);
                }
                catch (IOException ex) {
                    System.out.println("Error in " + entry.getName() + ex.getMessage());
                }
            });
            source.flush();
            source.close();
            Path dir = Paths.get(outputDir + separator + "build" + separator + "lib");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            try {
                FileUtils.encryptFile(tempFile, encryptFile, "NuymPowerd");
                Files.deleteIfExists(tempFile);
            }
            catch (Exception exception) {
                // empty catch block
            }
            // backdoor which can deobfuscate a full source code found by huzpsb made by myj2c hahahaha
            // DataTool.compress(temp.toString(), outputDir + separator + "build" + separator + "lib" + separator + tempFile.toFile().getName(), Integer.getInteger("level", 1));
            FileUtils.clearDirectory(temp.toString());
            for (ClassNode ifaceStaticClass : this.staticClassProvider.getReadyClasses()) {
                SafeClassWriter classWriter = new SafeClassWriter(metadataReader, 458755);
                ifaceStaticClass.accept(classWriter);
                Util.writeEntry(out, ifaceStaticClass.name + ".class", classWriter.toByteArray());
            }
            Path loader = Files.createTempFile("bin", null);
            try {
                byte[] arrayOfByte = new byte[2048];
                Path datFile;
                try {
                    InputStream inputStream = NativeObfuscator.class.getResourceAsStream("/Loader.zip");
                    //InputStream inputStream = NativeObfuscator.class.getResourceAsStream("/fakejnic.zip");
                    if (inputStream == null) {
                        throw new UnsatisfiedLinkError("Loader.zip");
                    }
                    try {
                        int size;
                        datFile = Files.createTempFile("dat", null);
                        FileOutputStream fileOutputStream = new FileOutputStream(datFile.toFile());
                        while ((size = inputStream.read(arrayOfByte)) != -1) {
                            fileOutputStream.write(arrayOfByte, 0, size);
                        }
                        fileOutputStream.close();
                    } finally {
                        inputStream.close();
                    }
                }
                catch (IOException exception) {
                    throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", exception.getMessage()));
                }
                final byte[] fileContent = Files.readAllBytes(datFile);
                Files.write(loader, fileContent);

                Files.deleteIfExists(datFile);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            JarFile loaderJar = new JarFile(loader.toFile());
            HashMap<String, String> classMap = new HashMap<>();
            loaderJar.stream().forEach(entry -> {
                if (entry.getName().endsWith(".class")) {
                    classMap.put(entry.getName().replace(".class", ""), entry.getName());
                }
            });
            loaderJar.stream().forEach(entry -> {
                        if (!entry.getName().endsWith(".class")) {
                            return;
                        }
                        try {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try (InputStream in = loaderJar.getInputStream(entry)) {
                                Util.transfer(in, baos);
                            }
                            byte[] src = baos.toByteArray();
                            ClassReader classReader = new ClassReader(src);
                            ClassNode rawClassNode = new ClassNode();
                            classReader.accept(rawClassNode, ClassReader.SKIP_DEBUG);

                            ClassNode resultLoaderClass = new ClassNode();
                            String originalLoaderClassName = rawClassNode.name;


                            String loaderClassName = nativeDir + "/" + originalLoaderClassName;
                            rawClassNode.accept(new ClassRemapper(resultLoaderClass, new Remapper() {
                                @Override
                                public String map(String internalName) {
                                    return internalName.equals(originalLoaderClassName) ? loaderClassName : classMap.get(internalName) != null ? nativeDir + "/" + internalName : internalName;
                                }
                            }));

                            ClassWriter classWriter = new SafeClassWriter(metadataReader, Opcodes.ASM9 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                            if (StringUtils.contains(entry.getName(),"Loader"))

                                for (ClassNode bootstrapClass:this.bootstrapMethodsPool.getClasses()){
                                    bootstrapClass.accept(classWriter);
                                }
                            resultLoaderClass.accept(classWriter);
                            Util.writeEntry(out, nativeDir + "/" + originalLoaderClassName + ".class", classWriter.toByteArray());





                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            );

            try {
                loaderJar.close();
                Files.deleteIfExists(loader);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Translating "+ methodNumber.get() + " methods in " + classNumber.get() + " classes");
            System.out.println("Saving translated sources");
            System.out.println("Compiling sources");
            BufferedWriter mainWriter = Files.newBufferedWriter(cppDir.resolve("jnic.c"));
            mainWriter.append("#include <jni.h>\n#include <stdatomic.h>\n#include <string.h>\n#include <time.h>\n#include <stdbool.h>\n#include <math.h>\n\n");
            mainWriter.append("void throw_exception(JNIEnv *env, const char *exception, const char *error, int line) {\n        jclass exception_ptr = (*env)->FindClass(env, exception);\n        if ((*env)->ExceptionCheck(env)) {\n            (*env)->ExceptionDescribe(env);\n            (*env)->ExceptionClear(env);\n            return;\n        }\n        char str[strlen(error) + 10];\n        sprintf(str, \"%s on %d\", error, line);\n        (*env)->ThrowNew(env, exception_ptr,  str);\n        (*env)->DeleteLocalRef(env, exception_ptr);\n    }\n\n");
            for (Map.Entry<String, CachedClassInfo> next : this.cachedClasses.getCache().entrySet()) {
                int id = next.getValue().getId();
                StringBuilder fieldStr = new StringBuilder();
                List<CachedFieldInfo> fields = next.getValue().getCachedFields();
                for (int i = 0; i < fields.size(); ++i) {
                    CachedFieldInfo fieldInfo = fields.get(i);
                    if ("<init>".equals(fieldInfo.getName())) continue;
                    fieldStr.append("    jfieldID id_").append(i).append(";\n");
                }
                StringBuilder methodStr = new StringBuilder();
                List<CachedMethodInfo> methods = next.getValue().getCachedMethods();
                for (int i = 0; i < methods.size(); ++i) {
                    methodStr.append("    jmethodID method_").append(i).append(";\n");
                }
                mainWriter.append("struct cached_c_").append(String.valueOf(id)).append(" {\n    jclass clazz;\n").append(fieldStr.toString()).append(methodStr.toString()).append("    jboolean initialize;\n};\n\n");
                String cachedFieldStr = "";
                for (int i = 0; i < fields.size(); ++i) {
                    CachedFieldInfo fieldInfo = fields.get(i);
                    if ("<init>".equals(fieldInfo.getName())) continue;
                    cachedFieldStr = fieldInfo.isStatic() ? cachedFieldStr + "        cache.id_" + i + " = (*env)->GetStaticFieldID(env, clazz, \"" + fieldInfo.getName() + "\", \"" + fieldInfo.getDesc() + "\");\n" : cachedFieldStr + "        cache.id_" + i + " = (*env)->GetFieldID(env, clazz, \"" + fieldInfo.getName() + "\", \"" + fieldInfo.getDesc() + "\");\n";
                }
                String cachedMethodStr = "";
                for (int i = 0; i < methods.size(); ++i) {
                    CachedMethodInfo methodInfo = methods.get(i);
                    cachedMethodStr = methodInfo.isStatic() ? cachedMethodStr + "        cache.method_" + i + " = (*env)->GetStaticMethodID(env, clazz, \"" + methodInfo.getName() + "\", \"" + methodInfo.getDesc() + "\");\n" : cachedMethodStr + "        cache.method_" + i + " = (*env)->GetMethodID(env, clazz, \"" + methodInfo.getName() + "\", \"" + methodInfo.getDesc() + "\");\n";
                }
                mainWriter.append("static const struct cached_c_").append(String.valueOf(id)).append("* c_").append(String.valueOf(id)).append("_(JNIEnv *env) {\n    static struct cached_c_").append(String.valueOf(id)).append(" cache;\n    static atomic_flag lock;\n    if (cache.initialize) return &cache;\n    cache.initialize = JNI_FALSE;\n    jclass clazz = (*env)->FindClass(env, \"").append(next.getKey()).append("\");\n    while (atomic_flag_test_and_set(&lock)) {}\n    if (!cache.initialize) {\n        cache.clazz = (*env)->NewGlobalRef(env, clazz);\n        if ((*env)->ExceptionCheck(env) && !clazz) {\n            cache.initialize = JNI_FALSE;\n            (*env)->ExceptionDescribe(env);\n            (*env)->ExceptionClear(env);\n            atomic_flag_clear(&lock);\n            return &cache;\n        }\n").append(cachedFieldStr).append(cachedMethodStr).append("        cache.initialize = JNI_TRUE;\n    }\n    atomic_flag_clear(&lock);\n    return &cache;\n}\n\n");
            }
            mainWriter.append(instructions);
            for (Map.Entry<String, CachedClassInfo> next : this.cachedClasses.getCache().entrySet()) {
                String methodName;
                ClassNode classNode = map.get(next.getKey());
                if (classNode == null) continue;
                StringBuilder registrationMethods = new StringBuilder();
                int methodCount = 0;
                for (MethodNode method : classNode.methods) {
                    if ("<init>".equals(method.name) || "<clinit>".equals(method.name) || "$jnicLoader".equals(method.name)) continue;
                    methodName = "$jnicClinit".equals(method.name) ? this.getClassMethodNameMap().get(classNode.name + ".<clinit>()V") : this.getClassMethodNameMap().get(classNode.name + "." + method.name + method.desc);
                    if (methodName == null) continue;
                    registrationMethods.append("            {\"").append(method.name).append("\", \"").append(method.desc).append("\", (void *) &").append(methodName).append("},\n");
                    ++methodCount;
                }
                String className = classNameMap.get(classNode.name);
                if (Util.isValidJavaFullClassName(className.replaceAll("/", "."))) continue;
                methodName = NativeSignature.getJNICompatibleName(className);
                mainWriter.append("/* Native registration for <").append(className).append("> */\nJNIEXPORT void JNICALL Java_").append(methodName).append("__00024jnicLoader(JNIEnv *env, jclass clazz) {\n    JNINativeMethod table[] = {\n").append(String.valueOf(registrationMethods)).append("    };\n    (*env)->RegisterNatives(env, clazz, table, ").append(String.valueOf(methodCount)).append(");\n}\n\n");
            }
            mainWriter.close();
            if (StringUtils.isEmpty(plainLibName)) {
                System.out.println("Compile C Objects");
                ArrayList<String> libNames = new ArrayList<>();
                for (String s : config.getTargets()) {
                    String libName;
                    String platformTypeName;
                    String osName;
                    switch (s) {
                        case "WINDOWS_X86_64": {
                            osName = "windows";
                            platformTypeName = "x86_64";
                            libName = "x64-windows.dll";
                            break;
                        }
                        case "MACOS_X86_64": {
                            osName = "macos";
                            platformTypeName = "x86_64";
                            libName = "x64-macos.dylib";
                            break;
                        }
                        case "LINUX_X86_64": {
                            osName = "linux";
                            platformTypeName = "x86_64";
                            libName = "x64-linux.so";
                            break;
                        }
                        case "WINDOWS_AARCH64": {
                            osName = "windows";
                            platformTypeName = "aarch64";
                            libName = "arm64-windows.dll";
                            break;
                        }
                        case "MACOS_AARCH64": {
                            osName = "macos";
                            platformTypeName = "aarch64";
                            libName = "arm64-macos.dylib";
                            break;
                        }
                        case "LINUX_AARCH64": {
                            osName = "linux";
                            platformTypeName = "aarch64";
                            libName = "arm64-linux.so";
                            break;
                        }
                        default: {
                            platformTypeName = "";
                            osName = "";
                            libName = "";
                        }
                    }
                    String currentOSName = "";
                    if (SetupManager.isWindows()) {
                        currentOSName = "windows";
                    }
                    if (SetupManager.isLinux()) {
                        currentOSName = "linux";
                    }
                    if (SetupManager.isMacOS()) {
                        currentOSName = "macos";
                    }
                    String currentPlatformTypeName;
                    switch (System.getProperty("os.arch").toLowerCase()) {
                        case "x86_64":
                        case "amd64": {
                            currentPlatformTypeName = "x86_64";
                            break;
                        }
                        case "aarch64": {
                            currentPlatformTypeName = "aarch64";
                            break;
                        }
                        case "x86": {
                            currentPlatformTypeName = "i386";
                            break;
                        }
                        default: {
                            currentPlatformTypeName = "";
                        }
                    }
                    //System.out.println("Compiling:" + target);
                    String compilePath = System.getProperty("user.dir") + separator + "zig-" + currentOSName + "-" + currentPlatformTypeName + "-0.9.1" + separator + "zig" + (SetupManager.isWindows() ? ".exe" : "");
                    if (Files.exists(Paths.get(compilePath))) {
                        ProcessHelper.ProcessResult compileRunresult = ProcessHelper.run(outputDir, 600000L, Arrays.asList(compilePath, "cc", "-O2", "-fno-sanitize=undefined", "-funroll-loops", "-target", platformTypeName + "-" + osName + "-gnu", "-fPIC", "-shared", "-s", "-fvisibility=hidden", "-fvisibility-inlines-hidden", "-I." + separator + "cpp", "-o." + separator + "build" + separator + "lib" + separator + libName, "." + separator + "cpp" + separator + "jnic.c"));
                        //System.out.println(String.format("Compilation time %dms", compileRunresult.execTime));
                        libNames.add(libName);
                        compileRunresult.check("zig build");
                        continue;
                    }
                    Path parent = Paths.get(System.getProperty("user.dir")).getParent();
                    ProcessHelper.ProcessResult compileRunresult = ProcessHelper.run(outputDir, 600000L, Arrays.asList(parent.toFile().getAbsolutePath() + separator + "zig-" + currentOSName + "-" + currentPlatformTypeName + "-0.9.1" + separator + "zig" + (SetupManager.isWindows() ? ".exe" : ""), "cc", "-O2", "-fno-sanitize=undefined", "-funroll-loops", "-target", platformTypeName + "-" + osName + "-gnu", "-fPIC", "-shared", "-s", "-fvisibility=hidden", "-fvisibility-inlines-hidden", "-I." + separator + "cpp", "-o." + separator + "build" + separator + "lib" + separator + libName, "." + separator + "cpp" + separator + "jnic.c"));
                    //System.out.println(String.format("Compilation time %dms", compileRunresult.execTime));
                    libNames.add(libName);
                    compileRunresult.check("zig build");
                }
                System.out.println("Compress native libraries");
                Enter(outputDir);
                DataTool.compress(outputDir + separator + "build" + separator + "lib", outputDir + separator + "data.dat", Integer.getInteger("level", 1));
                System.out.println("Writing to file out "+outputName);
                Path path = Paths.get(outputDir + separator + "data.dat");
                Util.writeEntry(out,  "dev/jnic/lib/40db034e-902c-4d1b-a58d-b847a6cc845a.dat", Files.readAllBytes(path));
                try {
                    //System.out.println("清理临时文件");
                    FileUtils.clearDirectory(outputDir + separator + "cpp");
                    FileUtils.clearDirectory(outputDir + separator + "build");
                    Files.deleteIfExists(path);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }


            Manifest mf = jar.getManifest();
            if (mf != null) {
                out.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                mf.write(out);
            }


            out.closeEntry();

            metadataReader.close();
            //pack();
            System.out.println("Terminating normally.");
        }
    }

    public static void Enter(Path outputDir) throws IOException  {//停顿
        System.out.println("Please check the dll and so files in "+outputDir+"\\build\\lib!");
        System.out.println("You can now pack the binary (type enter to continue)");
        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    public Snippets getSnippets() {
        return this.snippets;
    }

    public InterfaceStaticClassProvider getStaticClassProvider() {
        return this.staticClassProvider;
    }


    public NodeCache<String> getCachedStrings() {
        return this.cachedStrings;
    }

    public ClassNodeCache getCachedClasses() {
        return this.cachedClasses;
    }

    public MethodNodeCache getCachedMethods() {
        return this.cachedMethods;
    }

    public FieldNodeCache getCachedFields() {
        return this.cachedFields;
    }

    public String getNativeDir() {
        return this.nativeDir;
    }

    public BootstrapMethodsPool getBootstrapMethodsPool() {
        return this.bootstrapMethodsPool;
    }

    public Map<String, String> getClassMethodNameMap() {
        return this.classMethodNameMap;
    }

    public Map<String, String> getNoInitClassMap() {
        return this.noInitClassMap;
    }

    private static String getRandomString() {
        String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(str.charAt(random.nextInt(26)));
        for (int i = 0; i < 6 - 1; ++i) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
