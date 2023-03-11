// rebuild
package cc.nuym.jnic;

import cc.nuym.jnic.asm.ClassMetadataReader;
import cc.nuym.jnic.asm.SafeClassWriter;
import cc.nuym.jnic.env.SetupManager;
import cc.nuym.jnic.helpers.ProcessHelper;
import cc.nuym.jnic.utils.*;
import cc.nuym.jnic.xml.Config;
import cc.nuym.jnic.xml.Match;
import cc.nuym.jnic.cache.CachedClassInfo;
import cc.nuym.jnic.cache.CachedFieldInfo;
import cc.nuym.jnic.cache.CachedMethodInfo;
import cc.nuym.jnic.cache.ClassNodeCache;
import cc.nuym.jnic.cache.FieldNodeCache;
import cc.nuym.jnic.cache.MethodNodeCache;
import cc.nuym.jnic.cache.NodeCache;
import org.apache.commons.compress.utils.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class NativeObfuscator {
    private final Snippets snippets;
    private InterfaceStaticClassProvider staticClassProvider;
    private final MethodProcessor methodProcessor;
    private final NodeCache<String> cachedStrings;
    private final ClassNodeCache cachedClasses;
    private final MethodNodeCache cachedMethods;
    private final FieldNodeCache cachedFields;
    private Map<String, String> classMethodNameMap = new HashMap<String, String>();
    private Map<String, String> noInitClassMap = new HashMap<String, String>();
    private StringBuilder nativeMethods;
    private BootstrapMethodsPool bootstrapMethodsPool;
    private int currentClassId;
    private int methodIndex;
    private String nativeDir;
    private String nativeNonDir;
    private static String separator = File.separator;

    public NativeObfuscator() {
        this.snippets = new Snippets();
        this.cachedStrings = new NodeCache("(cstrings[%d])");
        this.cachedClasses = new ClassNodeCache("(cclasses[%d])");
        this.cachedMethods = new MethodNodeCache("(cmethods[%d])", this.cachedClasses);
        this.cachedFields = new FieldNodeCache("(cfields[%d])", this.cachedClasses);
        this.methodProcessor = new MethodProcessor(this);
    }

    public void process(Path inputJarPath, Path output, Config config, List<Path> inputLibs, String plainLibName, boolean useAnnotations) throws IOException {
        Path outputDir;
        ArrayList<Path> libs = new ArrayList<Path>(inputLibs);
        libs.add(inputJarPath);
        ArrayList<String> whiteList = new ArrayList<String>();
        if (config.getIncludes() != null) {
            for (Match match : config.getIncludes()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (StringUtils.isNotEmpty(match.getClassName())) {
                    stringBuilder.append(match.getClassName().replaceAll("\\.", "/"));
                }
                if (StringUtils.isNotEmpty(match.getMethodName())) {
                    stringBuilder.append("#" + match.getMethodName());
                    if (StringUtils.isNotEmpty(match.getMethodDesc())) {
                        stringBuilder.append("!" + match.getMethodDesc());
                    }
                } else if (StringUtils.isNotEmpty(match.getMethodDesc())) {
                    stringBuilder.append("#**!" + match.getMethodDesc());
                }
                whiteList.add(stringBuilder.toString());
            }
        }
        ArrayList<String> blackList = new ArrayList<String>();
        if (config.getExcludes() != null) {
            for (Match include : config.getExcludes()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (StringUtils.isNotEmpty(include.getClassName())) {
                    stringBuilder.append(include.getClassName().replaceAll("\\.", "/"));
                }
                if (StringUtils.isNotEmpty(include.getMethodName())) {
                    stringBuilder.append("#" + include.getMethodName());
                    if (StringUtils.isNotEmpty(include.getMethodDesc())) {
                        stringBuilder.append("!" + include.getMethodDesc());
                    }
                } else if (StringUtils.isNotEmpty(include.getMethodDesc())) {
                    stringBuilder.append("#**!" + include.getMethodDesc());
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
        Files.createDirectories(cppDir, new FileAttribute[0]);
        Util.copyResource("jni.h", cppDir);
        HashMap map = new HashMap();
        HashMap classNameMap = new HashMap();
        StringBuilder instructions = new StringBuilder();
        File jarFile = inputJarPath.toAbsolutePath().toFile();
        Path temp = Files.createTempDirectory("native-jnic-", new FileAttribute[0]);
        Path tempFile = temp.resolve(UUID.randomUUID() + ".data");
        Path encryptFile = temp.resolve(UUID.randomUUID() + ".data");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(outputName), new OpenOption[0]));
             ZipOutputStream source = new ZipOutputStream(Files.newOutputStream(tempFile, new OpenOption[0]));){
            Manifest mf;
            JarFile jar = new JarFile(jarFile);
            System.out.println("处理 " + jarFile + "中...");
            this.nativeNonDir = "dev/jnic/";
            this.nativeDir = nativeNonDir + NativeObfuscator.getRandomString(6);
            this.bootstrapMethodsPool = new BootstrapMethodsPool(this.nativeDir);
            this.staticClassProvider = new InterfaceStaticClassProvider(this.nativeDir);
            this.methodIndex = 1;
            jar.stream().forEach(entry -> {
                block17: {
                    try {
                        if (!entry.getName().endsWith(".class")) break block17;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (InputStream in = jar.getInputStream((ZipEntry)entry);){
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
                    try (InputStream in = jar.getInputStream((ZipEntry)entry);){
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
                    if (!classMethodFilter.shouldProcess(rawClassNode) || rawClassNode.methods.stream().noneMatch(method -> MethodProcessor.shouldProcess(method) && classMethodFilter.shouldProcess(rawClassNode, (MethodNode)method))) {
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
                    instructions.append("\n//" + classNode.name + "\n");
                    for (int i = 0; i < classNode.methods.size(); ++i) {
                        MethodNode method2 = classNode.methods.get(i);
                        if (!MethodProcessor.shouldProcess(method2) || !classMethodFilter.shouldProcess(classNode, method2) && !"<clinit>".equals(method2.name)) continue;
                        MethodContext context = new MethodContext(this, method2, this.methodIndex, classNode, this.currentClassId);
                        this.methodProcessor.processMethod(context);
                        instructions.append(context.output.toString().replace("\n", "\n    "));
                        this.nativeMethods.append((CharSequence)context.nativeMethods);
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
                    System.out.println("处理时出错 " + entry.getName() + ex.getMessage());
                }
            });
            source.flush();
            source.close();
            if (!Files.exists(Paths.get(outputDir + separator + "build" + separator + "lib", new String[0]), new LinkOption[0])) {
                Files.createDirectories(Paths.get(outputDir + separator + "build" + separator + "lib", new String[0]), new FileAttribute[0]);
            }
            try {
                FileUtils.encryptFile(tempFile, encryptFile, "NuymPowerd");
                Files.deleteIfExists(tempFile);
            }
            catch (Exception exception) {
                // empty catch block
            }
            DataTool.compress(temp.toString(), outputDir + separator + "build" + separator + "lib" + separator + tempFile.toFile().getName(), Integer.getInteger("level", 1));
            FileUtils.clearDirectory(temp.toString());
            for (ClassNode ifaceStaticClass : this.staticClassProvider.getReadyClasses()) {
                SafeClassWriter classWriter = new SafeClassWriter(metadataReader, 458755);
                ifaceStaticClass.accept(classWriter);
                Util.writeEntry(out, ifaceStaticClass.name + ".class", classWriter.toByteArray());
            }
            Path loader = Files.createTempFile("bin", null, new FileAttribute[0]);
            try {
                byte[] arrayOfByte = new byte[2048];
                Path datFile = null;
                try {
                    InputStream inputStream = NativeObfuscator.class.getResourceAsStream("/jnic.bin_dump.zip");
                    //InputStream inputStream = NativeObfuscator.class.getResourceAsStream("/fakejnic.zip");
                    if (inputStream == null) {
                        throw new UnsatisfiedLinkError(String.format("Failed to open zip file: jnic.bin_dump.zip", new Object[0]));
                    }
                    try {
                        int size;
                        datFile = Files.createTempFile("dat", null, new FileAttribute[0]);
                        FileOutputStream fileOutputStream = new FileOutputStream(datFile.toFile());
                        while ((size = inputStream.read(arrayOfByte)) != -1) {
                            fileOutputStream.write(arrayOfByte, 0, size);
                        }
                        fileOutputStream.close();
                    }
                    catch (Throwable throwable) {
                        throw throwable;
                    }
                    finally {
                        inputStream.close();
                    }
                }
                catch (IOException exception) {
                    throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", exception.getMessage()));
                }
                final byte[] fileContent = Files.readAllBytes(datFile);
                Files.write(loader, fileContent, new OpenOption[0]);

                Files.deleteIfExists(datFile);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            JarFile loaderJar = new JarFile(loader.toFile());
            HashMap classMap = new HashMap();
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
                    try (InputStream in = loaderJar.getInputStream((ZipEntry)entry);){
                        Util.transfer(in, baos);
                    }
                    byte[] src = baos.toByteArray();
                    ClassReader classReader = new ClassReader(src);
                    ClassNode rawClassNode = new ClassNode(458752);
                    classReader.accept(rawClassNode, 2);
                    ClassNode resultLoaderClass = new ClassNode(458752);
                    final String originalLoaderClassName = rawClassNode.name;
                    if (StringUtils.contains(entry.getName(), "Loader")) {
                        final String loaderClassName = this.nativeDir + "/JNICLoader";
                        if (plainLibName != null) {
                            if (StringUtils.contains(entry.getName(), "LoaderPlain")) {
                                rawClassNode.methods.forEach(method -> {
                                    for (int i = 0; i < method.instructions.size(); ++i) {
                                        AbstractInsnNode insnNode = method.instructions.get(i);
                                        if (!(insnNode instanceof LdcInsnNode) || !(((LdcInsnNode)insnNode).cst instanceof String) || !((LdcInsnNode)insnNode).cst.equals("%LIB_NAME%")) continue;
                                        ((LdcInsnNode)insnNode).cst = plainLibName;
                                    }
                                });
                                rawClassNode.accept(new ClassRemapper(resultLoaderClass, new Remapper(){

                                    @Override
                                    public String map(String internalName) {
                                        return internalName.equals(originalLoaderClassName) ? loaderClassName : (classMap.get(internalName) != null ? NativeObfuscator.this.nativeDir + "/" + internalName : internalName);
                                    }
                                }));
                                SafeClassWriter classWriter = new SafeClassWriter(metadataReader, 458755);
                                for (ClassNode bootstrapClass : this.bootstrapMethodsPool.getClasses()) {
                                    bootstrapClass.accept(classWriter);
                                }
                                resultLoaderClass.accept(classWriter);
                                Util.writeEntry(out, loaderClassName + ".class", classWriter.toByteArray());
                            }
                        } else if (StringUtils.contains(entry.getName(), "LoaderUnpack")) {
                            rawClassNode.accept(new ClassRemapper(resultLoaderClass, new Remapper(){

                                @Override
                                public String map(String internalName) {
                                    return internalName.equals(originalLoaderClassName) ? loaderClassName : (classMap.get(internalName) != null ? NativeObfuscator.this.nativeDir + "/" + internalName : internalName);
                                }
                            }));
                            this.rewriteClass(resultLoaderClass);
                            SafeClassWriter classWriter = new SafeClassWriter(metadataReader, 458755);
                            for (ClassNode bootstrapClass : this.bootstrapMethodsPool.getClasses()) {
                                bootstrapClass.accept(classWriter);
                            }
                            resultLoaderClass.accept(classWriter);
                            Util.writeEntry(out, loaderClassName + ".class", classWriter.toByteArray());
                        }
                    } else if (StringUtils.isEmpty(plainLibName)) {
                        final String loaderClassName = this.nativeDir + "/" + originalLoaderClassName;
                        rawClassNode.accept(new ClassRemapper(resultLoaderClass, new Remapper(){

                            @Override
                            public String map(String internalName) {
                                return internalName.equals(originalLoaderClassName) ? loaderClassName : (classMap.get(internalName) != null ? NativeObfuscator.this.nativeDir + "/" + internalName : internalName);
                            }
                        }));
                        this.rewriteClass(resultLoaderClass);
                        SafeClassWriter classWriter = new SafeClassWriter(metadataReader, 458755);
                        resultLoaderClass.accept(classWriter);
                        Util.writeEntry(out, this.nativeDir + "/" + originalLoaderClassName + ".class", classWriter.toByteArray());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });
            try {
                loaderJar.close();
                Files.deleteIfExists(loader);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("找到共 " + classNumber.get() + " classes 和 " + methodNumber.get() + " 将要被混淆的方法");
            System.out.println("您正在使用企业版本！\n");
            System.out.println("将类翻译成C代码");
            BufferedWriter mainWriter = Files.newBufferedWriter(cppDir.resolve("jnic.c"), new OpenOption[0]);
            mainWriter.append("#include <jni.h>\n#include <stdatomic.h>\n#include <string.h>\n#include <time.h>\n#include <stdbool.h>\n#include <math.h>\n\n");
            String appInfo = "";
            if (!"".equals(appInfo)) {
                mainWriter.append("struct cached_system {\n    jclass clazz;\n    jfieldID id_0;\n};\n\nstatic const struct cached_system* cc_system(JNIEnv *env) {\n    static struct cached_system cache;\n    static atomic_flag lock;\n    if (cache.clazz) return &cache;\n\n    jclass clazz = (*env)->FindClass(env, \"java/lang/System\");\n    while (atomic_flag_test_and_set(&lock)) {}\n    if (!cache.clazz) {\n        cache.clazz = (*env)->NewGlobalRef(env, clazz);\n        cache.id_0 = (*env)->GetStaticFieldID(env, clazz, \"out\", \"Ljava/io/PrintStream;\");\n    }\n    atomic_flag_clear(&lock);\n    return &cache;\n}\n\nstruct cached_print {\n    jclass clazz;\n    jmethodID method_0;\n};\n\nstatic const struct cached_print* cc_print(JNIEnv *env) {\n    static struct cached_print cache;\n    static atomic_flag lock;\n    if (cache.clazz) return &cache;\n\n    jclass clazz = (*env)->FindClass(env, \"java/io/PrintStream\");\n    while (atomic_flag_test_and_set(&lock)) {}\n    if (!cache.clazz) {\n        cache.clazz = (*env)->NewGlobalRef(env, clazz);\n        cache.method_0 = (*env)->GetMethodID(env, clazz, \"println\", \"(Ljava/lang/String;)V\");\n    }\n    atomic_flag_clear(&lock);\n    return &cache;\n}");
            }
            mainWriter.append("void throw_exception(JNIEnv *env, const char *exception, const char *error, int line) {\n        jclass exception_ptr = (*env)->FindClass(env, exception);\n        if ((*env)->ExceptionCheck(env)) {\n            (*env)->ExceptionDescribe(env);\n            (*env)->ExceptionClear(env);\n            return;\n        }\n        char str[strlen(error) + 10];\n        sprintf(str, \"%s on %d\", error, line);\n        (*env)->ThrowNew(env, exception_ptr,  str);\n        (*env)->DeleteLocalRef(env, exception_ptr);\n    }\n\n");
            for (Map.Entry<String, CachedClassInfo> next : this.cachedClasses.getCache().entrySet()) {
                int id = next.getValue().getId();
                String fieldStr = "";
                List<CachedFieldInfo> fields = next.getValue().getCachedFields();
                for (int i = 0; i < fields.size(); ++i) {
                    CachedFieldInfo fieldInfo = fields.get(i);
                    if ("<init>".equals(fieldInfo.getName())) continue;
                    fieldStr = fieldStr + "    jfieldID id_" + i + ";\n";
                }
                String methodStr = "";
                List<CachedMethodInfo> methods = next.getValue().getCachedMethods();
                for (int i = 0; i < methods.size(); ++i) {
                    CachedMethodInfo methodInfo = (CachedMethodInfo)methods.get(i);
                    methodStr = methodStr + "    jmethodID method_" + i + ";\n";
                }
                mainWriter.append("struct cached_c_" + id + " {\n    jclass clazz;\n" + fieldStr + methodStr + "    jboolean initialize;\n};\n\n");
                String cachedFieldStr = "";
                for (int i = 0; i < fields.size(); ++i) {
                    CachedFieldInfo fieldInfo = fields.get(i);
                    if ("<init>".equals(fieldInfo.getName())) continue;
                    cachedFieldStr = fieldInfo.isStatic() ? cachedFieldStr + "        cache.id_" + i + " = (*env)->GetStaticFieldID(env, clazz, \"" + fieldInfo.getName() + "\", \"" + fieldInfo.getDesc() + "\");\n" : cachedFieldStr + "        cache.id_" + i + " = (*env)->GetFieldID(env, clazz, \"" + fieldInfo.getName() + "\", \"" + fieldInfo.getDesc() + "\");\n";
                }
                String cachedMethodStr = "";
                for (int i = 0; i < methods.size(); ++i) {
                    Object methodInfo = (CachedMethodInfo)methods.get(i);
                    cachedMethodStr = ((CachedMethodInfo)methodInfo).isStatic() ? cachedMethodStr + "        cache.method_" + i + " = (*env)->GetStaticMethodID(env, clazz, \"" + ((CachedMethodInfo)methodInfo).getName() + "\", \"" + ((CachedMethodInfo)methodInfo).getDesc() + "\");\n" : cachedMethodStr + "        cache.method_" + i + " = (*env)->GetMethodID(env, clazz, \"" + ((CachedMethodInfo)methodInfo).getName() + "\", \"" + ((CachedMethodInfo)methodInfo).getDesc() + "\");\n";
                }
                mainWriter.append("static const struct cached_c_" + id + "* c_" + id + "_(JNIEnv *env) {\n    static struct cached_c_" + id + " cache;\n    static atomic_flag lock;\n    if (cache.initialize) return &cache;\n    cache.initialize = JNI_FALSE;\n    jclass clazz = (*env)->FindClass(env, \"" + next.getKey() + "\");\n    while (atomic_flag_test_and_set(&lock)) {}\n    if (!cache.initialize) {\n        cache.clazz = (*env)->NewGlobalRef(env, clazz);\n        if ((*env)->ExceptionCheck(env) && !clazz) {\n            cache.initialize = JNI_FALSE;\n            (*env)->ExceptionDescribe(env);\n            (*env)->ExceptionClear(env);\n            atomic_flag_clear(&lock);\n            return &cache;\n        }\n" + cachedFieldStr + cachedMethodStr + "        cache.initialize = JNI_TRUE;\n    }\n    atomic_flag_clear(&lock);\n    return &cache;\n}\n\n");
            }
            mainWriter.append(instructions);
            for (Map.Entry<String, CachedClassInfo> next : this.cachedClasses.getCache().entrySet()) {
                String methodName;
                ClassNode classNode = (ClassNode)map.get(next.getKey());
                if (classNode == null) continue;
                String registrationMethods = "";
                int methodCount = 0;
                for (MethodNode method : classNode.methods) {
                    if ("<init>".equals(method.name) || "<clinit>".equals(method.name) || "$jnicLoader".equals(method.name)) continue;
                    methodName = null;
                    methodName = "$jnicClinit".equals(method.name) ? this.getClassMethodNameMap().get(classNode.name + ".<clinit>()V") : this.getClassMethodNameMap().get(classNode.name + "." + method.name + method.desc);
                    if (methodName == null) continue;
                    registrationMethods = registrationMethods + "            {\"" + method.name + "\", \"" + method.desc + "\", (void *) &" + methodName + "},\n";
                    ++methodCount;
                }
                String className = (String)classNameMap.get(classNode.name);
                if (!Util.isValidJavaFullClassName(className.replaceAll("/", "."))) continue;
                methodName = NativeSignature.getJNICompatibleName(className);
                mainWriter.append("/* Native registration for <" + className + "> */\nJNIEXPORT void JNICALL Java_" + methodName + "__00024jnicLoader(JNIEnv *env, jclass clazz) {\n    JNINativeMethod table[] = {\n" + registrationMethods + "    };\n    (*env)->RegisterNatives(env, clazz, table, " + methodCount + ");\n}\n\n");
            }
            mainWriter.close();
            if (StringUtils.isEmpty(plainLibName)) {
                System.out.println("开始编译DLL");
                ArrayList<String> libNames = new ArrayList<String>();
                Iterator<String> iterator = config.getTargets().iterator();
                while (iterator.hasNext()) {
                    String libName;
                    String platformTypeName;
                    String osName;
                    String target;
                    switch (target = iterator.next()) {
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
                    String currentPlatformTypeName = "";
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
                    System.out.println("目标: " + target);
                    String compilePath = System.getProperty("user.dir") + separator + "zig-" + currentOSName + "-" + currentPlatformTypeName + "-0.9.1" + separator + "zig" + (SetupManager.isWindows() ? ".exe" : "");
                    if (Files.exists(Paths.get(compilePath, new String[0]), new LinkOption[0])) {
                        ProcessHelper.ProcessResult compileRunresult = ProcessHelper.run(outputDir, 600000L, Arrays.asList(compilePath, "cc", "-O2", "-fno-sanitize=undefined", "-funroll-loops", "-target", platformTypeName + "-" + osName + "-gnu", "-fPIC", "-shared", "-s", "-fvisibility=hidden", "-fvisibility-inlines-hidden", "-I." + separator + "cpp", "-o." + separator + "build" + separator + "lib" + separator + libName, "." + separator + "cpp" + separator + "jnic.c"));
                        System.out.println(String.format("耗时 %dms", compileRunresult.execTime));
                        libNames.add(libName);
                        compileRunresult.check("zig build");
                        continue;
                    }
                    Path parent = Paths.get(System.getProperty("user.dir"), new String[0]).getParent();
                    ProcessHelper.ProcessResult compileRunresult = ProcessHelper.run(outputDir, 600000L, Arrays.asList(parent.toFile().getAbsolutePath() + separator + "zig-" + currentOSName + "-" + currentPlatformTypeName + "-0.9.1" + separator + "zig" + (SetupManager.isWindows() ? ".exe" : ""), "cc", "-O2", "-fno-sanitize=undefined", "-funroll-loops", "-target", platformTypeName + "-" + osName + "-gnu", "-fPIC", "-shared", "-s", "-fvisibility=hidden", "-fvisibility-inlines-hidden", "-I." + separator + "cpp", "-o." + separator + "build" + separator + "lib" + separator + libName, "." + separator + "cpp" + separator + "jnic.c"));
                    System.out.println(String.format("耗时: %dms", compileRunresult.execTime));
                    libNames.add(libName);
                    compileRunresult.check("zig build");
                }
                System.out.println("压缩DLL");
                Enter(outputDir);
                DataTool.compress(outputDir + separator + "build" + separator + "lib", outputDir + separator + "data.dat", Integer.getInteger("level", 1));
                System.out.println("重新打包");
                Util.writeEntry(out, this.nativeDir + "/data.dat", Files.readAllBytes(Paths.get(outputDir + separator + "data.dat", new String[0])));
                try {
                    System.out.println("清理临时文件");
                    FileUtils.clearDirectory(outputDir + separator + "cpp");
                    FileUtils.clearDirectory(outputDir + separator + "build");
                    Files.deleteIfExists(Paths.get(outputDir + separator + "data.dat", new String[0]));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }

            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date dat = new Date(System.currentTimeMillis());
            String date = formatter.format(dat);

            out.setComment("Jnic is a powerful native Java bytecode obfuscator made by nuym.\nObfuscation time: "+date+"\nContact:1006800345@qq.com");
            out.closeEntry();
            metadataReader.close();
            System.out.println("成功!");
            /*
            System.out.println(outputName);
            System.out.println(output);
            System.out.println(outputDir);
             */
        }
    }

    public static void Enter(Path outputDir) throws IOException  {//停顿
        System.out.println("请查看"+outputDir+"\\build\\lib中的dll和so文件!");
        System.out.println("如您需要加强，将其丢入VMProtect中根据自己需求混淆!");
        System.out.println("按回车键继续!");
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
    public String getNonNativeDir() {
        return this.nativeNonDir;
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

    private static String getRandomString(int length) {
        String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        sb.append(str.charAt(random.nextInt(26)));
        for (int i = 0; i < length - 1; ++i) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    private void varargsAccess(MethodNode methodNode) {
        if ((methodNode.access & 0x1000) == 0 && (methodNode.access & 0x40) == 0) {
            methodNode.access |= 0x80;
        }
    }

    private void bridgeAccess(MethodNode methodNode) {
        if (!methodNode.name.contains("<") && !Modifier.isAbstract(methodNode.access)) {
            methodNode.access |= 0x40;
        }
    }

    private static void obf(File input, File output) throws Throwable {
        ZipFile zipFile = new ZipFile(input);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output), Charset.forName("UTF-8"));
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
    private void syntheticAccess(ClassNode classNode) {
        classNode.access |= 0x1000;
        classNode.fields.forEach(fieldNode -> fieldNode.access |= 0x1000);
        classNode.methods.forEach(methodNode -> methodNode.access |= 0x1000);
    }

    private void changeSource(ClassNode classNode) {
        classNode.sourceFile = this.getMassiveString();
        classNode.sourceDebug = this.getMassiveString();
    }

    private void changeSignature(ClassNode classNode) {
        classNode.signature = this.getMassiveString();
        classNode.fields.forEach(fieldNode -> {
            fieldNode.signature = this.getMassiveString();
        });
        classNode.methods.forEach(methodNode -> {
            methodNode.signature = this.getMassiveString();
        });
    }

    private void deprecatedAccess(ClassNode classNode) {
        classNode.access |= 0x20000;
        classNode.methods.forEach(methodNode -> methodNode.access |= 0x20000);
        classNode.fields.forEach(fieldNode -> fieldNode.access |= 0x20000);
    }

    private void transientAccess(ClassNode classNode) {
        classNode.fields.forEach(fieldNode -> fieldNode.access |= 0x80);
    }

    private void removeNop(ClassNode classNode) {
        classNode.methods.parallelStream().forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray()).filter(insnNode -> insnNode.getOpcode() == 0).forEach(insnNode -> methodNode.instructions.remove((AbstractInsnNode)insnNode)));
    }

    private String getMassiveString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Short.MAX_VALUE; ++i) {
            builder.append(" ");
        }
        return builder.toString();
    }

    private void rewriteClass(ClassNode classNode) {
        this.removeNop(classNode);
        if (!Modifier.isInterface(classNode.access)) {
            this.transientAccess(classNode);
        }
        this.deprecatedAccess(classNode);
        this.changeSource(classNode);
        this.changeSignature(classNode);
        this.syntheticAccess(classNode);
        classNode.methods.forEach(methodNode -> {
            this.bridgeAccess((MethodNode)methodNode);
            this.varargsAccess((MethodNode)methodNode);
        });
    }
    private static void folderobf(File input, File output) throws Throwable {

    }
}
