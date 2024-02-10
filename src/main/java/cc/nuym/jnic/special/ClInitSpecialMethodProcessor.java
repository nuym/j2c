package cc.nuym.jnic.special;

import cc.nuym.jnic.utils.MethodContext;
import cc.nuym.jnic.utils.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class ClInitSpecialMethodProcessor implements SpecialMethodProcessor {

    @Override
    public String preprocess(MethodContext context) {
        final String proxyMethodName = "$jnicClinit";
        if (!Util.hasFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {
            MethodNode proxyMethod = new MethodNode(
                    Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                    proxyMethodName,
                    context.method.desc,
                    context.method.signature,
                    new String[0]);
            context.clazz.methods.add(proxyMethod);
            context.proxyMethod = proxyMethod;
        }
        return proxyMethodName;
    }

    @Override
    public void postprocess(MethodContext context) {
        InsnList instructions = context.method.instructions;
        instructions.clear();

        instructions.add(new LdcInsnNode(context.classIndex));
        instructions.add(new LdcInsnNode(Type.getObjectType(context.clazz.name)));

        // 添加调用JNICLoader.registerNativesForClass方法的指令
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                context.obfuscator.getNativeDir() + "/JNICLoader",
                "registerNativesForClass",
                "(ILjava/lang/Class;)V",
                false));

        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                context.clazz.name,
                "$jnicLoader",
                "()V",
                false));

        if (Util.hasFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {
            if (context.nativeMethod == null) {
                throw new RuntimeException("Native method not created?!");
            }
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    context.obfuscator.getStaticClassProvider().getCurrentClassName(),
                    context.nativeMethod.name,
                    context.nativeMethod.desc,
                    false));
        } else if (!context.obfuscator.getNoInitClassMap().containsKey(context.clazz.name)) {
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    context.clazz.name,
                    "$jnicClinit",
                    context.method.desc,
                    false));
        }

        instructions.add(new InsnNode(Opcodes.RETURN)); // 使用RETURN常量替代177
    }

}
