# Jnic
一个强大的本地Java字节码混淆器。

---
### 特征
1. 阻止逆向工程
    - Jnic 将类文件转换为本机 JNI 库，Java 工具无法检测、修改、分析或调试这些库。 本机代码显着阻碍了分析。
2. 多层防护
    - 本机库中添加了额外的混淆层，包括字符串加密、引用混淆和控制流混淆。
3. 可与现有混淆器互操作
    - 为了获得更多保护，可以将 Jnic 应用于另一个混淆器的输出，从而产生极其复杂的本机代码。

---   
### 为什么选择Jnic
现有的 Java 混淆器将转换应用于 Java 类文件，使它们更难理解。

然而，这些转换通常很容易逆转，并且有许多免费的开源工具，如 [java-deobfuscator](https://github.com/java-deobfuscator/deobfuscator)它可以在几秒钟内撤消许多现有的混淆工具。

编译并重新链接到原始程序 JNIC 将 Java 类文件转换为本地代码，该代码通过[Java Native Interface](https://en.wikipedia.org/wiki/Java_Native_Interface) 。

没有原始 Java 字节码的痕迹，并且没有任何适用于类文件的反混淆工具可以恢复原始代码。

前
```java
public class App {
  public static void main(String args[]) {
    System.out.println("Hello, world!");
  }
}
```

后
```java
public class App {
public static native void main(String args[]);
}
```

---
### Jnic易于使用

使用 Java 本机接口通常是一个繁琐的过程，需要了解 C 编程语言、交叉编译工具链及其依赖项的设置，以及以正确的顺序初始化正确的库而不创建循环的精心设计的加载器。

使用 Jnic，这都是一个命令的开始。
```
java -jar jnic.jar program.jar program-obfuscated.jar config.xml
```

Jnic 协调整个本机混淆过程。 它将程序转换为 C，为 Windows、Linux 和 macOS 编译源代码（同时支持 ARM 和 x86 处理器），保护本机库（字符串加密、流混淆、引用混淆、压缩），并注入一个加载器，无缝地在运行时解码库。

---
### Jnic兼容

JNIC 支持从 Java 8 到 Java 19 的所有 Java 语言特性。此外，JNIC 支持整个 JVM 8+ 指令集，包括同步、多维数组、lambda 和异常处理。 这意味着它可以处理其他以 JVM 为目标的语言，例如 [Kotlin](https://kotlinlang.org/) ，以及现有 Java 混淆器的输出，例如 [Proguard](https://www.guardsquare.com/proguard)。

---

### JNIC 不仅仅是一个翻译器
除了生成本机代码之外，JNIC 还可以选择应用涵盖传统 Java 混淆器功能集等的进一步转换。

Jnic 使用行业标准 [ChaCha20](https://datatracker.ietf.org/doc/html/rfc7539)对字符串进行加密等协议中使用的算法 [QUIC](https://en.wikipedia.org/wiki/QUIC) 和 [WireGuard](https://en.wikipedia.org/wiki/WireGuard)

传统的混淆器会注入“字符串解密”方法，这些方法很容易被逆向工程师拦截，Jnic 会在堆栈中内联解密字符串，并且不会将它们保留在内存中超过需要的时间。 JNIC 使用同样强大的加密算法来隐藏本机代码使用的 Java 方法和字段的名称和类。

JNIC 的控制流混淆执行一种称为控制流扁平化的转换，它从根本上改变了构成方法的代码结构。 这与传统的混淆器形成对比，其中许多混淆器只是插入一些可以轻松跳过的死代码或跳转指令。 扁平化会产生极其复杂的控制流图，使本机逆向工程变得更加困难。

---

### 用法:
```
Usage: Jnic [-ahV] [-c=<config>] [-l=<librariesDirectory>]
[--plain-lib-name=<libraryName>] <jarFile> <outputDirectory>
将.jar文件翻译成.c文件并生成输出.jar文件
<jarFile>           要转译的Jar文件。
<outputDirectory>   输出目录。
-a, --annotations       使用注解来忽略/包含本地混淆的内容。
-c, --config=<config>   Config 文件。
-h, --help              显示此帮助信息并退出。
-l, --libraries=<librariesDirectory>
依赖库的目录
--plain-lib-name=<libraryName>
用于加载的依赖库名称。
-V, --version           打印版本信息并退出。
```
