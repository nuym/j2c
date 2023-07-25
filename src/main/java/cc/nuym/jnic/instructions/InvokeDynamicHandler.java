// rebuild
package cc.nuym.jnic.instructions;

import cc.nuym.jnic.MethodContext;
import cc.nuym.jnic.Util;
import cc.nuym.jnic.cache.CachedClassInfo;
import cc.nuym.jnic.cache.CachedFieldInfo;
import cc.nuym.jnic.cache.CachedMethodInfo;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvokeDynamicHandler
        extends GenericInstructionHandler<InvokeDynamicInsnNode> {
    private final Map<MethodNode, Integer> cache = new HashMap<>();

    @Override
    protected void process(MethodContext context, InvokeDynamicInsnNode node) {
        Object bsmArgument;
        int i;
        int index = 0;
        if (this.cache.containsKey(context.method)) {
            index = this.cache.get(context.method) + 1;
            this.cache.put(context.method, index);
        } else {
            this.cache.put(context.method, index);
        }
        CachedClassInfo bsmClass = context.getCachedClasses().getClass(node.bsm.getOwner());
        CachedClassInfo methodHandles = context.getCachedClasses().getClass("java/lang/invoke/MethodHandles");
        CachedClassInfo methodHandle = context.getCachedClasses().getClass("java/lang/invoke/MethodHandle");
        CachedClassInfo methodType = context.getCachedClasses().getClass("java/lang/invoke/MethodType");
        CachedClassInfo clazz = context.getCachedClasses().getClass(context.clazz.name);
        CachedClassInfo javaClass = context.getCachedClasses().getClass("java/lang/Class");
        CachedClassInfo lookup = context.getCachedClasses().getClass("java/lang/invoke/MethodHandles$Lookup");
        context.output.append("\nstatic jobject indy").append(index).append(";\n");
        if (node.bsmArgs.length > 3) {
            context.output.append("if(indy").append(index).append(" == NULL) {\n");
            int size = 3 + node.bsmArgs.length;
            context.output.append(" jobject args = (*env)->NewObjectArray(env, ").append(size).append(", c_").append(context.getCachedClasses().getClass("java/lang/Object").getId()).append("_(env)->clazz, NULL);\n");
            context.output.append("(*env)->SetObjectArrayElement(env, args, 0, (*env)->CallStaticObjectMethod(env, c_").append(methodHandles.getId()).append("_(env)->clazz, c_").append(methodHandles.getId()).append("_(env)->method_").append(methodHandles.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", true))).append("));\n");
            context.output.append("(*env)->SetObjectArrayElement(env, args, 1, (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(node.name)).append("}, ").append(node.name.length()).append("));\n");
            context.output.append("(*env)->SetObjectArrayElement(env, args, 2, (*env)->CallStaticObjectMethod(env, c_").append(methodType.getId()).append("_(env)->clazz, c_").append(methodType.getId()).append("_(env)->method_").append(methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true))).append(", (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(node.desc)).append("}, ").append(node.desc.length()).append("), (*env)->CallObjectMethod(env,c_").append(clazz.getId()).append("_(env)->clazz, c_").append(javaClass.getId()).append("_(env)->method_").append(javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false))).append(")));\n");
            for (i = 0; i < node.bsmArgs.length; ++i) {
                CachedClassInfo integer;
                bsmArgument = node.bsmArgs[i];
                if (bsmArgument instanceof String) {
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(bsmArgument.toString())).append("}, ").append(bsmArgument.toString().length()).append("));\n");
                    continue;
                }
                if (bsmArgument instanceof Type) {
                    if (((Type)bsmArgument).getSort() == 11) {
                        context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->CallStaticObjectMethod(env, c_").append(methodType.getId()).append("_(env)->clazz, c_").append(methodType.getId()).append("_(env)->method_").append(methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true))).append(", (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(bsmArgument.toString())).append("}, ").append(bsmArgument.toString().length()).append("), (*env)->CallObjectMethod(env,c_").append(clazz.getId()).append("_(env)->clazz, c_").append(javaClass.getId()).append("_(env)->method_").append(javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false))).append(")));\n");
                        continue;
                    }
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", c_").append(context.getCachedClasses().getId(bsmArgument.toString())).append("_(env)->clazz);\n");
                    continue;
                }
                if (bsmArgument instanceof Integer) {
                    integer = context.getCachedClasses().getClass("java/lang/Integer");
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->CallStaticObjectMethod(env, c_").append(integer.getId()).append("_(env)->clazz, c_").append(integer.getId()).append("_(env)->method_").append(integer.getCachedMethodId(new CachedMethodInfo("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", true))).append(", ").append(bsmArgument).append("));\n");
                    continue;
                }
                if (bsmArgument instanceof Long) {
                    integer = context.getCachedClasses().getClass("java/lang/Long");
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->CallStaticObjectMethod(env, c_").append(integer.getId()).append("_(env)->clazz, c_").append(integer.getId()).append("_(env)->method_").append(integer.getCachedMethodId(new CachedMethodInfo("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", true))).append(", ").append(bsmArgument).append("));\n");
                    continue;
                }
                if (bsmArgument instanceof Float) {
                    integer = context.getCachedClasses().getClass("java/lang/Float");
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->CallStaticObjectMethod(env, c_").append(integer.getId()).append("_(env)->clazz, c_").append(integer.getId()).append("_(env)->method_").append(integer.getCachedMethodId(new CachedMethodInfo("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", true))).append(", ").append(bsmArgument).append("));\n");
                    continue;
                }
                if (bsmArgument instanceof Double) {
                    integer = context.getCachedClasses().getClass("java/lang/Double");
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->CallStaticObjectMethod(env, c_").append(integer.getId()).append("_(env)->clazz, c_").append(integer.getId()).append("_(env)->method_").append(integer.getCachedMethodId(new CachedMethodInfo("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", true))).append(", ").append(bsmArgument).append("));\n");
                    continue;
                }
                if (bsmArgument instanceof Handle) {
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->CallObjectMethod(env, (*env)->CallStaticObjectMethod(env, c_").append(methodHandles.getId()).append("_(env)->clazz, c_").append(methodHandles.getId()).append("_(env)->method_").append(methodHandles.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", true))).append("), c_").append(lookup.getId()).append("_(env)->method_").append(lookup.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false))).append(", c_").append(context.getCachedClasses().getClass(((Handle) bsmArgument).getOwner()).getId()).append("_(env)->clazz, (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(((Handle) bsmArgument).getName())).append("}, ").append(((Handle) bsmArgument).getName().length()).append("), (*env)->CallStaticObjectMethod(env, c_").append(methodType.getId()).append("_(env)->clazz, c_").append(methodType.getId()).append("_(env)->method_").append(methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true))).append(", (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(((Handle) bsmArgument).getDesc())).append("}, ").append(((Handle) bsmArgument).getDesc().length()).append("), (*env)->CallObjectMethod(env,c_").append(clazz.getId()).append("_(env)->clazz, c_").append(javaClass.getId()).append("_(env)->method_").append(javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false))).append("))));\n");
                    continue;
                }
                if (bsmArgument instanceof Short) {
                    integer = context.getCachedClasses().getClass("java/lang/Short");
                    context.output.append("(*env)->SetObjectArrayElement(env, args, ").append(3 + i).append(", (*env)->CallStaticObjectMethod(env, c_").append(integer.getId()).append("_(env)->clazz, c_").append(integer.getId()).append("_(env)->method_").append(integer.getCachedMethodId(new CachedMethodInfo("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", true))).append(", ").append(bsmArgument).append("));\n");
                    continue;
                }
                throw new RuntimeException("Wrong argument type: " + bsmArgument.getClass());
            }
            context.output.append("jobject callSite = (*env)->CallObjectMethod(env, ");
            context.output.append("(*env)->CallObjectMethod(env, ");
            context.output.append("(*env)->CallStaticObjectMethod(env, c_").append(methodHandles.getId()).append("_(env)->clazz, c_").append(methodHandles.getId()).append("_(env)->method_").append(methodHandles.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", true))).append("), ");
            context.output.append("c_").append(lookup.getId()).append("_(env)->method_").append(lookup.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false))).append(", ");
            context.output.append("c_").append(bsmClass.getId()).append("_(env)->clazz, ");
            context.output.append("(*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(node.bsm.getName())).append("}, ").append(node.bsm.getName().length()).append("), ");
            context.output.append("(*env)->CallStaticObjectMethod(env, ");
            context.output.append("c_").append(methodType.getId()).append("_(env)->clazz, ");
            context.output.append("c_").append(methodType.getId()).append("_(env)->method_").append(methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true))).append(", ");
            context.output.append("(*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(node.bsm.getDesc())).append("}, ").append(node.bsm.getDesc().length()).append("), ");
            context.output.append("(*env)->CallObjectMethod(env, c_").append(clazz.getId()).append("_(env)->clazz, c_").append(javaClass.getId()).append("_(env)->method_").append(javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false))).append("))");
            context.output.append("), ");
            context.output.append("c_").append(methodHandle.getId()).append("_(env)->method_").append(methodHandle.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", false))).append(", ");
            context.output.append("args);");
            context.output.append(this.props.get("trycatchhandler")).append("\n");
            context.output.append("indy").append(index).append(" = (*env)->NewGlobalRef(env, callSite);\n");
            context.output.append("(*env)->DeleteLocalRef(env, callSite);\n");
            context.output.append("}\n");
        } else {
            context.output.append("if(indy").append(index).append(" == NULL) {\n");
            context.output.append("jobject callSite = (*env)->CallStaticObjectMethod(env, ");
            context.output.append("c_").append(bsmClass.getId()).append("_(env)->clazz, ");
            context.output.append("c_").append(bsmClass.getId()).append("_(env)->method_").append(bsmClass.getCachedMethodId(new CachedMethodInfo(node.bsm.getOwner(), node.bsm.getName(), node.bsm.getDesc(), true))).append(", ");
            context.output.append("(*env)->CallStaticObjectMethod(env, c_").append(methodHandles.getId()).append("_(env)->clazz, c_").append(methodHandles.getId()).append("_(env)->method_").append(methodHandles.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", true))).append("), ");
            context.output.append("(*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(node.name)).append("}, ").append(node.name.length()).append("), ");
            context.output.append("(*env)->CallStaticObjectMethod(env, c_").append(methodType.getId()).append("_(env)->clazz, c_").append(methodType.getId()).append("_(env)->method_").append(methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true))).append(", (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(node.desc)).append("}, ").append(node.desc.length()).append("), (*env)->CallObjectMethod(env, c_").append(clazz.getId()).append("_(env)->clazz, c_").append(javaClass.getId()).append("_(env)->method_").append(javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false))).append(")), ");
            StringBuilder args = new StringBuilder();
            for (i = 0; i < node.bsmArgs.length; ++i) {
                bsmArgument = node.bsmArgs[i];
                if (bsmArgument instanceof String) {
                    args.append("(*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode((String) bsmArgument)).append("}, ").append(((String) bsmArgument).length()).append(")").append(i < node.bsmArgs.length - 1 ? ", " : "");
                    continue;
                }
                if (bsmArgument instanceof Type) {
                    if (((Type)bsmArgument).getSort() == 11) {
                        args.append("(*env)->CallStaticObjectMethod(env, c_").append(methodType.getId()).append("_(env)->clazz, c_").append(methodType.getId()).append("_(env)->method_").append(methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true))).append(", (*env)->NewString(env, (unsigned short[]) {").append(Util.utf82unicode(((Type) bsmArgument).getDescriptor())).append("}, ").append(((Type) bsmArgument).getDescriptor().length()).append("), (*env)->CallObjectMethod(env, c_").append(clazz.getId()).append("_(env)->clazz, c_").append(javaClass.getId()).append("_(env)->method_").append(javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false))).append("))").append(i < node.bsmArgs.length - 1 ? ", " : "");
                        continue;
                    }
                    args.append("c_").append(context.getCachedClasses().getId(bsmArgument.toString())).append("_(env)->clazz").append(i < node.bsmArgs.length - 1 ? ", \n" : "");
                    continue;
                }
                if (bsmArgument instanceof Integer) {
                    args.append(bsmArgument).append(i < node.bsmArgs.length - 1 ? ", " : "");
                    continue;
                }
                if (bsmArgument instanceof Long) {
                    args.append(bsmArgument).append(i < node.bsmArgs.length - 1 ? ", " : "");
                    continue;
                }
                if (bsmArgument instanceof Float) {
                    args.append(bsmArgument).append(i < node.bsmArgs.length - 1 ? ", " : "");
                    continue;
                }
                if (bsmArgument instanceof Double) {
                    args.append(bsmArgument).append(i < node.bsmArgs.length - 1 ? ", " : "");
                    continue;
                }
                if (bsmArgument instanceof Handle) {
                    args.append(this.generateMethodHandleLdcInsn(context, (Handle) bsmArgument)).append(i < node.bsmArgs.length - 1 ? ", " : "");
                    continue;
                }
                throw new RuntimeException("Wrong argument type: " + bsmArgument.getClass());
            }
            context.output.append(args);
            context.output.append(");\n");
            context.output.append(this.props.get("trycatchhandler")).append("\n");
            context.output.append("indy").append(index).append(" = (*env)->NewGlobalRef(env, callSite);\n");
            context.output.append("(*env)->DeleteLocalRef(env, callSite);\n");
            context.output.append("}\n");
        }
        CachedClassInfo classInfo = context.getCachedClasses().getClass("java/lang/invoke/CallSite");
        List<CachedMethodInfo> cachedMethods = classInfo.getCachedMethods();
        boolean hasMethod = false;
        for (int i2 = 0; i2 < cachedMethods.size(); ++i2) {
            CachedMethodInfo methodInfo = cachedMethods.get(i2);
            if (!methodInfo.getName().equals("getTarget")) continue;
            hasMethod = true;
            context.output.append("temp0.l = (*env)->CallObjectMethod(env, indy").append(index).append(", c_").append(classInfo.getId()).append("_(env)->method_").append(i2).append(");\n");
        }
        if (!hasMethod) {
            CachedMethodInfo cachedMethodInfo = new CachedMethodInfo("java/lang/invoke/CallSite", "getTarget", "()Ljava/lang/invoke/MethodHandle;", false);
            classInfo.addCachedMethod(cachedMethodInfo);
            context.output.append("temp0.l = (*env)->CallObjectMethod(env, indy").append(index).append(", c_").append(classInfo.getId()).append("_(env)->method_").append(classInfo.getCachedMethods().size() - 1).append(");\n");
        }
        CachedClassInfo methodHandleClassInfo = context.getCachedClasses().getClass("java/lang/invoke/MethodHandle");
        int methodId = -1;
        int count = 0;
        for (CachedMethodInfo cachedMethod : methodHandleClassInfo.getCachedMethods()) {
            if (cachedMethod.getName().equals("invokeWithArguments")) {
                methodId = count;
            }
            ++count;
        }
        if (methodId == -1) {
            CachedMethodInfo cachedMethodInfo = new CachedMethodInfo("java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", false);
            methodHandleClassInfo.addCachedMethod(cachedMethodInfo);
            methodId = methodHandleClassInfo.getCachedMethods().size() - 1;
        }
        Type[] argTypes = Type.getArgumentTypes(node.desc);
        StringBuilder argsBuilder = new StringBuilder();
        ArrayList<Integer> argOffsets = new ArrayList<>();
        int stackOffset = context.stackPointer;
        for (Type argType : argTypes) {
            stackOffset -= argType.getSize();
        }
        int argumentOffset = stackOffset;
        for (Type argType : argTypes) {
            argOffsets.add(argumentOffset);
            argumentOffset += argType.getSize();
        }
        for (int i3 = 0; i3 < argOffsets.size(); ++i3) {
            argsBuilder.append(", ").append(context.getSnippets().getSnippet("INVOKE_ARG_" + argTypes[i3].getSort(), Util.createMap("index", argOffsets.get(i3))));
        }
        if (argOffsets.size() == 0) {
            context.output.append("cstack").append(stackOffset).append(".l = (*env)->CallObjectMethod(env, temp0.l, c_").append(methodHandleClassInfo.getId()).append("_(env)->method_").append(methodId).append(", NULL);\n");
        } else {
            String methodDesc = InvokeDynamicHandler.simplifyDesc(node.desc);
            Type[] methodArguments = Type.getArgumentTypes(methodDesc);
            Type[] newMethodArguments = new Type[methodArguments.length + 1];
            newMethodArguments[0] = Type.getObjectType("java/lang/invoke/MethodHandle");
            System.arraycopy(methodArguments, 0, newMethodArguments, 1, methodArguments.length);
            methodDesc = Type.getMethodDescriptor(Type.getReturnType(methodDesc), newMethodArguments);
            String mhDesc = InvokeDynamicHandler.simplifyDesc(Type.getMethodType(Type.getReturnType(node.desc), Util.reverse(Util.reverse(Arrays.stream(Type.getArgumentTypes(node.desc)))).toArray(Type[]::new)).getDescriptor());
            context.obfuscator.getBootstrapMethodsPool().getMethod("invoke", methodDesc, method -> {
                method.instructions.add(new VarInsnNode(25, 0));
                int idx = 1;
                for (Type argument : Type.getArgumentTypes(mhDesc)) {
                    method.instructions.add(new VarInsnNode(argument.getOpcode(21), idx));
                    idx += argument.getSize();
                }
                method.instructions.add(new MethodInsnNode(182, "java/lang/invoke/MethodHandle", "invoke", mhDesc));
                method.instructions.add(new InsnNode(Type.getReturnType(mhDesc).getOpcode(172)));
            });
            CachedClassInfo classLoader = context.getCachedClasses().getClass(context.obfuscator.getNativeDir() + "/JNICLoader");
            context.output.append("cstack").append(stackOffset).append(".l = (*env)->CallStaticObjectMethod(env, c_").append(classLoader.getId()).append("_(env)->clazz, c_").append(classLoader.getId()).append("_(env)->method_").append(classLoader.getCachedMethodId(new CachedMethodInfo(context.obfuscator.getNativeDir() + "/JNICLoader", "invoke", methodDesc, true))).append(", temp0.l").append(argsBuilder).append(");\n");
        }
        context.output.append(this.props.get("trycatchhandler"));
    }

    @Override
    public String insnToString(MethodContext context, InvokeDynamicInsnNode node) {
        return String.format("%s %s", Util.getOpcodeString(node.getOpcode()), node.desc);
    }

    @Override
    public int getNewStackPointer(InvokeDynamicInsnNode node, int currentStackPointer) {
        return currentStackPointer - Arrays.stream(Type.getArgumentTypes(node.desc)).mapToInt(Type::getSize).sum() + Type.getReturnType(node.desc).getSize();
    }

    private String generateMethodHandleLdcInsn(MethodContext context, Handle handle) {
        CachedClassInfo methodHandles = context.getCachedClasses().getClass("java/lang/invoke/MethodHandles");
        CachedClassInfo methodType = context.getCachedClasses().getClass("java/lang/invoke/MethodType");
        CachedClassInfo clazz = context.getCachedClasses().getClass(context.clazz.name);
        CachedClassInfo handleClazz = context.getCachedClasses().getClass(handle.getOwner());
        CachedClassInfo javaClass = context.getCachedClasses().getClass("java/lang/Class");
        CachedClassInfo lookup = context.getCachedClasses().getClass("java/lang/invoke/MethodHandles$Lookup");
        String code = "(*env)->CallObjectMethod(env, (*env)->CallStaticObjectMethod(env, c_" + methodHandles.getId() + "_(env)->clazz, c_" + methodHandles.getId() + "_(env)->method_" + methodHandles.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", true)) + "),";
        switch (handle.getTag()) {
            case 1:
            case 2:
            case 3:
            case 4: {
                String methodName = "";
                switch (handle.getTag()) {
                    case 1: {
                        methodName = "findGetter";
                        break;
                    }
                    case 2: {
                        methodName = "findStaticGetter";
                        break;
                    }
                    case 3: {
                        methodName = "findSetter";
                        break;
                    }
                    case 4: {
                        methodName = "findStaticSetter";
                    }
                }
                code = code + "c_" + lookup.getId() + "_(env)->method_" + lookup.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles$Lookup", methodName, "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false)) + ", c_" + handleClazz.getId() + "_(env)->clazz,";
                code = code + "(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getName()) + "}, " + handle.getName().length() + "),";
                code = code + InvokeDynamicHandler.getTypeLoadCode(context, Type.getType(handle.getDesc()));
                return code;
            }
            case 5:
            case 9: {
                code = code + "c_" + lookup.getId() + "_(env)->method_" + lookup.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false)) + ", c_" + handleClazz.getId() + "_(env)->clazz,";
                code = code + "(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getName()) + "}, " + handle.getName().length() + "),";
                code = code + "(*env)->CallStaticObjectMethod(env, c_" + methodType.getId() + "_(env)->clazz, c_" + methodType.getId() + "_(env)->method_" + methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true)) + ",(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getDesc()) + "}, " + handle.getDesc().length() + "),(*env)->CallObjectMethod(env, c_" + clazz.getId() + "_(env)->clazz, c_" + javaClass.getId() + "_(env)->method_" + javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false)) + ")))";
                return code;
            }
            case 6: {
                code = code + "c_" + lookup.getId() + "_(env)->method_" + lookup.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false)) + ", c_" + handleClazz.getId() + "_(env)->clazz,";
                code = code + "(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getName()) + "}, " + handle.getName().length() + "),";
                code = code + "(*env)->CallStaticObjectMethod(env, c_" + methodType.getId() + "_(env)->clazz, c_" + methodType.getId() + "_(env)->method_" + methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true)) + ",(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getDesc()) + "}, " + handle.getDesc().length() + "),(*env)->CallObjectMethod(env, c_" + clazz.getId() + "_(env)->clazz, c_" + javaClass.getId() + "_(env)->method_" + javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false)) + ")))";
                return code;
            }
            case 7: {
                code = code + "c_" + lookup.getId() + "_(env)->method_" + lookup.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles$Lookup", "findSpecial", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false)) + ", c_" + handleClazz.getId() + "_(env)->clazz,";
                code = code + "(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getName()) + "}, " + handle.getName().length() + "),";
                code = code + "(*env)->CallStaticObjectMethod(env, c_" + methodType.getId() + "_(env)->clazz, c_" + methodType.getId() + "_(env)->method_" + methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true)) + ",(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getDesc()) + "}, " + handle.getDesc().length() + "),(*env)->CallObjectMethod(env, c_" + clazz.getId() + "_(env)->clazz, c_" + javaClass.getId() + "_(env)->method_" + javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false)) + ")),c_" + clazz.getId() + "_(env)->clazz)";
                return code;
            }
            case 8: {
                code = code + "c_" + lookup.getId() + "_(env)->method_" + lookup.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodHandles$Lookup", "findConstructor", "(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false)) + ", c_" + handleClazz.getId() + "_(env)->clazz,";
                code = code + "(*env)->CallStaticObjectMethod(env, c_" + methodType.getId() + "_(env)->clazz, c_" + methodType.getId() + "_(env)->method_" + methodType.getCachedMethodId(new CachedMethodInfo("java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", true)) + ",(*env)->NewString(env, (unsigned short[]) {" + Util.utf82unicode(handle.getDesc()) + "}, " + handle.getDesc().length() + "),(*env)->CallObjectMethod(env, c_" + clazz.getId() + "_(env)->clazz, c_" + javaClass.getId() + "_(env)->method_" + javaClass.getCachedMethodId(new CachedMethodInfo("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false)) + ")))";
                return code;
            }
        }
        return "";
    }

    private static String getTypeLoadCode(MethodContext context, Type type) {
        String lazz;
        CachedClassInfo fieldType;
        switch (type.getSort()) {
            case 9:
            case 10: {
                return "c_" + context.getCachedClasses().getId(type.toString()) + "_(env)->clazz";
            }
            case 1: {
                lazz = "java/lang/Boolean";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
            case 3: {
                lazz = "java/lang/Byte";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
            case 2: {
                lazz = "java/lang/Character";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
            case 8: {
                lazz = "java/lang/Double";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
            case 6: {
                lazz = "java/lang/Float";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
            case 5: {
                lazz = "java/lang/Integer";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
            case 7: {
                lazz = "java/lang/Long";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
            case 4: {
                lazz = "java/lang/Short";
                fieldType = context.getCachedClasses().getClass(lazz);
                return "(*env)->GetStaticBooleanField(env, c_" + fieldType.getId() + "_(env)->clazz, c_" + fieldType.getId() + "_(env)->id_" + fieldType.getCachedFieldId(new CachedFieldInfo(lazz, "TYPE", "Ljava/lang/Class;", true)) + ")";
            }
        }
        throw new RuntimeException(String.format("Unsupported TypeLoad type: %s", type));
    }

    private static String simplifyDesc(String desc) {
        return Type.getMethodType(InvokeDynamicHandler.simplifyType(Type.getReturnType(desc)), Arrays.stream(Type.getArgumentTypes(desc)).map(InvokeDynamicHandler::simplifyType).toArray(Type[]::new)).getDescriptor();
    }

    private static Type simplifyType(Type type) {
        switch (type.getSort()) {
            case 9:
            case 10: {
                return Type.getObjectType("java/lang/Object");
            }
            case 11: {
                throw new RuntimeException();
            }
        }
        return type;
    }
}
