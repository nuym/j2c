# Jnic
A powerful native Java bytecode obfuscator.

---
### Build
Build with JDK 1.8 and Gradle
```gradle
gradle build
```

---
### Features
1. Block reverse engineering
   - Jnic converts class files into native JNI libraries that Java tools cannot detect, modify, analyze, or debug. Native code significantly hinders analysis.
2. Multi-layer protection
   - Native libraries add additional layers of obfuscation, including string encryption, reference obfuscation, and control flow obfuscation. 3.
3. Interoperable with existing obfuscators
   - For additional protection, Jnic can be applied to the output of another obfuscator, resulting in extremely complex native code.

---   
### Why Jnic
Existing Java obfuscators apply transformations to Java class files, making them harder to understand.

However, these transformations are often easy to reverse and there are many free open source tools such as [java-deobfuscator](https://github.com/java-deobfuscator/deobfuscator) which can undo many existing obfuscators in seconds.

Compile and relink to the original program JNIC converts Java class files to native code, which is available through [Java Native Interface](https://en.wikipedia.org/wiki/Java_Native_Interface) .

There is no trace of the original Java bytecode, and no anti-obfuscation tool for class files can recover the original code.

Before
```java
public class App {
  public static void main(String args[]) {
    System.out.println("Hello, world!");
  }
}
```

After
```java
public class App {
public static native void main(String args[]);
}
```

---
### Jnic is easy to use

Using the Java native interface is usually a tedious process that requires knowledge of the C programming language, the setup of the cross-compilation toolchain and its dependencies, and a well-designed loader that initializes the right libraries in the right order without creating loops.

With Jnic, it's all a command start.
```
java -jar jnic.jar program.jar program-obfuscated.jar config.xml
```

Jnic coordinates the entire native obfuscation process. It converts programs to C, compiles source code for Windows, Linux, and macOS (supporting both ARM and x86 processors), protects native libraries (string encryption, stream obfuscation, reference obfuscation, compression), and injects a loader that seamlessly decodes libraries at runtime.

---
### Jnic compatible

JNIC supports all Java language features from Java 8 to Java 19. In addition, JNIC supports the entire JVM 8+ instruction set, including synchronization, multi-dimensional arrays, lambda and exception handling. This means that it can handle other JVM-targeted languages, such as [Kotlin](https://kotlinlang.org/), as well as the output of existing Java obfuscators, such as [Proguard](https://www.guardsquare.com/proguard).

---

### Jnic is more than just a translator
In addition to generating native code, JNIC can optionally apply further transformations covering the traditional Java obfuscator feature set and more.

Jnic uses algorithms such as those used in protocols such as [QUIC](https://en.wikipedia.org/wiki/QUIC), which encrypts strings using the industry standard [ChaCha20](https://datatracker.ietf.org/doc/html/rfc7539), and [ WireGuard](https://en.wikipedia.org/wiki/WireGuard)

While traditional obfuscators inject "string decryption" methods that can be easily intercepted by reverse engineers, JNIC inlines decrypted strings on the stack and does not hold them in memory longer than necessary. JNIC uses the same strong encryption algorithms to hide the names and classes of Java methods and fields used by native code.

JNIC's control-flow obfuscation performs a transformation called control-flow flattening, which fundamentally changes the structure of the code that makes up a method. This is in contrast to traditional obfuscators, many of which simply insert dead code or jump instructions that can be easily skipped. Flattening produces extremely complex control flow graphs, making native reverse engineering more difficult.

---

### Usage:
```
Usage: Jnic [-ahV] [-c=<config>] [-l=<librariesDirectory>]
[--plain-lib-name=<libraryName>] <jarFile> <outputDirectory>
Translate .jar files to .c files and generate output .jar files
<jarFile> The jar file to be translated.
<outputDirectory> The output directory.
-a, --annotations Use annotations to ignore/include locally obfuscated content.
-c, --config=<config> Config file.
-h, --help Display this help message and exit.
-l, --libraries=<librariesDirectory>
Directory of dependent libraries
--plain-lib-name=<libraryName>
Name of the dependent library to load.
-V, --version Print the version information and exit.

```

Even better if paired with a VMP


consultation [Jnic Wiki](https://jnic.dev/documentation/#tutorial)
