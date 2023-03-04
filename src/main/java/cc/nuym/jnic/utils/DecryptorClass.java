package cc.nuym.jnic.utils;

import org.objectweb.asm.*;
public class DecryptorClass implements Opcodes {
    private String className;
    private String fieldName;
    private String fieldName2;
    private String methodName;

    public DecryptorClass(String className, String fieldName, String fieldName2, String methodName) {
        this.className = className;
        this.fieldName = fieldName;
        this.fieldName2 = fieldName2;
        this.methodName = methodName;
    }

    public byte[] getBytes() {
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        {
            fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, fieldName, "[I", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, fieldName2, "I", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "(Ljava/lang/Object;)Ljava/lang/String;", null, new String[]{"java/lang/Throwable"});
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
            mv.visitVarInsn(ASTORE, 1);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitMethodInsn(INVOKESTATIC, "sun/misc/SharedSecrets", "getJavaLangAccess", "()Lsun/misc/JavaLangAccess;", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(GETSTATIC, className, fieldName, "[I");
            mv.visitFieldInsn(GETSTATIC, className, fieldName2, "I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(ISUB);
            mv.visitInsn(IALOAD);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            mv.visitMethodInsn(INVOKEINTERFACE, "sun/misc/JavaLangAccess", "getConstantPool", "(Ljava/lang/Class;)Lsun/reflect/ConstantPool;", true);
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/reflect/ConstantPool", "getSize", "()I", false);
            mv.visitVarInsn(ISTORE, 2);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(GETSTATIC, className, fieldName, "[I");
            mv.visitFieldInsn(GETSTATIC, className, fieldName2, "I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(ISUB);
            mv.visitInsn(IALOAD);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
            mv.visitVarInsn(ISTORE, 3);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(GETSTATIC, className, fieldName, "[I");
            mv.visitFieldInsn(GETSTATIC, className, fieldName2, "I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(ISUB);
            mv.visitInsn(IALOAD);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
            mv.visitVarInsn(ISTORE, 4);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 5);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
            mv.visitIntInsn(NEWARRAY, T_CHAR);
            mv.visitVarInsn(ASTORE, 6);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
            mv.visitVarInsn(ASTORE, 7);
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
            mv.visitVarInsn(ISTORE, 8);
            Label l8 = new Label();
            mv.visitLabel(l8);
            mv.visitFieldInsn(GETSTATIC, className, fieldName2, "I");
            mv.visitFieldInsn(GETSTATIC, className, fieldName, "[I");
            mv.visitFieldInsn(GETSTATIC, className, fieldName2, "I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(ISUB);
            mv.visitInsn(IALOAD);
            mv.visitInsn(ISHR);
            mv.visitFieldInsn(GETSTATIC, className, fieldName2, "I");
            mv.visitInsn(ISHR);
            mv.visitVarInsn(ISTORE, 9);
            Label l9 = new Label();
            mv.visitLabel(l9);
            mv.visitFrame(Opcodes.F_FULL, 10, new Object[]{"java/lang/Object", "[Ljava/lang/StackTraceElement;", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[]{});
            mv.visitVarInsn(ILOAD, 9);
            mv.visitVarInsn(ILOAD, 8);
            Label l10 = new Label();
            mv.visitJumpInsn(IF_ICMPLT, l10);
            Label l11 = new Label();
            mv.visitLabel(l11);
            Label l12 = new Label();
            mv.visitJumpInsn(GOTO, l12);
            mv.visitLabel(l10);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ILOAD, 9);
            mv.visitInsn(ICONST_2);
            mv.visitInsn(IREM);
            Label l13 = new Label();
            Label l14 = new Label();
            Label l15 = new Label();
            mv.visitLookupSwitchInsn(l15, new int[]{0, 1}, new Label[]{l13, l14});
            mv.visitLabel(l13);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ILOAD, 9);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ILOAD, 9);
            mv.visitInsn(CALOAD);
            mv.visitVarInsn(ILOAD, 3);
            mv.visitInsn(IXOR);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitInsn(IXOR);
            mv.visitInsn(I2C);
            mv.visitInsn(CASTORE);
            Label l16 = new Label();
            mv.visitLabel(l16);
            mv.visitJumpInsn(GOTO, l15);
            mv.visitLabel(l14);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ILOAD, 9);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ILOAD, 9);
            mv.visitInsn(CALOAD);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitInsn(IXOR);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitInsn(IXOR);
            mv.visitInsn(I2C);
            mv.visitInsn(CASTORE);
            mv.visitLabel(l15);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitIincInsn(9, 1);
            mv.visitJumpInsn(GOTO, l9);
            mv.visitLabel(l12);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitTypeInsn(NEW, "java/lang/String");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
            mv.visitInsn(ARETURN);
            Label l17 = new Label();
            mv.visitLabel(l17);
            mv.visitMaxs(5, 10);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitInsn(ICONST_1);
            mv.visitIntInsn(NEWARRAY, T_INT);
            mv.visitFieldInsn(PUTSTATIC, className, fieldName, "[I");
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitInsn(ICONST_1);
            mv.visitVarInsn(ISTORE, 0);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitFieldInsn(PUTSTATIC, className, fieldName2, "I");
            mv.visitFieldInsn(GETSTATIC, className, fieldName, "[I");
            mv.visitInsn(ICONST_0);
            mv.visitInsn(ICONST_2);
            mv.visitInsn(IASTORE);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
