package cc.nuym.jnic.special;

import cc.nuym.jnic.utils.MethodContext;
import cc.nuym.jnic.utils.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class DefaultSpecialMethodProcessor implements SpecialMethodProcessor {

    @Override
    public String preprocess(MethodContext context) {
        context.proxyMethod = context.method;
        MethodNode method = context.method;
        // 添加ACC_NATIVE标志，表示此方法是原生方法
        method.access |= Opcodes.ACC_NATIVE;
        return "native_" + method.name + context.methodIndex;
    }

    @Override

    public void postprocess(MethodContext context) {

        context.method.instructions.clear();


        if (Util.hasFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {

            InsnList instructions = new InsnList();

            int localVarsPosition = 0;


            for (Type arg : context.argTypes) {

                instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));

                localVarsPosition += arg.getSize();
            }

            if (context.nativeMethod == null) {
                throw new RuntimeException("Native method not created?!");
            }
            // 调用原生方法的指令

            instructions.add(new MethodInsnNode(

                    Opcodes.INVOKESTATIC,

                    context.obfuscator.getStaticClassProvider().getCurrentClassName(),

                    context.nativeMethod.name,

                    context.nativeMethod.desc,

                    false));


            Type returnType = Type.getReturnType(context.method.desc);

            // 根据返回类型添加合适的返回指令

            switch (returnType.getSort()) {
                case Type.VOID:
                    instructions.add(new InsnNode(Opcodes.RETURN));
                    break;

                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    instructions.add(new InsnNode(Opcodes.IRETURN));
                    break;

                case Type.LONG:
                    instructions.add(new InsnNode(Opcodes.LRETURN));
                    break;

                case Type.FLOAT:
                    instructions.add(new InsnNode(Opcodes.FRETURN));
                    break;

                case Type.DOUBLE:
                    instructions.add(new InsnNode(Opcodes.DRETURN));
                    break;

                case Type.OBJECT:
                case Type.ARRAY:
                    instructions.add(new InsnNode(Opcodes.ARETURN));
                    break;

            }
            context.method.instructions = instructions;

        }
    }
}