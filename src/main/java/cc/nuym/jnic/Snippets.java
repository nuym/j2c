package cc.nuym.jnic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Snippets
{
    private final Properties snippets;
    
    public Snippets() {
        this.snippets = new Properties();
        try {
            String sb = "LOCAL_LOAD_ARG_1=clocal$index.i = (jint) $arg;\n" +
                    "LOCAL_LOAD_ARG_2=clocal$index.i = (jint) $arg;\n" +
                    "LOCAL_LOAD_ARG_3=clocal$index.i = (jint) $arg;\n" +
                    "LOCAL_LOAD_ARG_4=clocal$index.i = (jint) $arg;\n" +
                    "LOCAL_LOAD_ARG_5=clocal$index.i = $arg;\n" +
                    "LOCAL_LOAD_ARG_6=clocal$index.f = $arg;\n" +
                    "LOCAL_LOAD_ARG_7=clocal$index.j = $arg;\n" +
                    "LOCAL_LOAD_ARG_8=clocal$index.d = $arg;\n" +
                    "LOCAL_LOAD_ARG_9=clocal$index.l = $arg;\n" +
                    "LOCAL_LOAD_ARG_10=clocal$index.l = $arg;\n" +
                    "LOCAL_LOAD_ARG_11=clocal$index.l = $arg;\n" +
                    "NOP=;\n" +
                    "ACONST_NULL=cstack$stackindex0.l = NULL;\n" +
                    "ICONST_M1=cstack$stackindex0.i = -1;\n" +
                    "ICONST_0=cstack$stackindex0.i = 0;\n" +
                    "ICONST_1=cstack$stackindex0.i = 1;\n" +
                    "ICONST_2=cstack$stackindex0.i = 2;\n" +
                    "ICONST_3=cstack$stackindex0.i = 3;\n" +
                    "ICONST_4=cstack$stackindex0.i = 4;\n" +
                    "ICONST_5=cstack$stackindex0.i = 5;\n" +
                    "LCONST_0=cstack$stackindex0.j = 0;\n" +
                    "LCONST_1=cstack$stackindex0.j = 1;\n" +
                    "FCONST_0=cstack$stackindex0.f = 0.0f;\n" +
                    "FCONST_1=cstack$stackindex0.f = 1.0f;\n" +
                    "FCONST_2=cstack$stackindex0.f = 2.0f;\n" +
                    "DCONST_0=cstack$stackindex0.d = 0.0;\n" +
                    "DCONST_1=cstack$stackindex0.d = 1.0;\n" +
                    "BIPUSH=cstack$stackindex0.i = (jint) $operand;\n" +
                    "SIPUSH=cstack$stackindex0.i = (jint) $operand;\n" +
                    "LDC_STRING=cstack$stackindex0.l = (*env)->NewString(env, (unsigned short[]) {$cst_ptr}, $cst_length);\n" +
                    "LDC_STRING_NULL=cstack$stackindex0.l = (*env)->NewString(env, NULL, 0);\n" +
                    "LDC_INT=cstack$stackindex0.i = $cst;\n" +
                    "LDC_FLOAT=cstack$stackindex0.f = $cst;\n" +
                    "LDC_LONG=cstack$stackindex0.j = $cst;\n" +
                    "LDC_DOUBLE=cstack$stackindex0.d = $cst;\n" +
                    "LDC_CLASS=cstack$stackindex0.l = $class_ptr(env)->clazz;\n" +
                    "ILOAD=cstack$stackindex0.i = clocal$var.i;\n" +
                    "LLOAD=cstack$stackindex0.j = clocal$var.j;\n" +
                    "FLOAD=cstack$stackindex0.f = clocal$var.f;\n" +
                    "DLOAD=cstack$stackindex0.d = clocal$var.d;\n" +
                    "ALOAD=cstack$stackindex0.l = clocal$var.l;\n" +
                    "IALOAD=  (*env)->GetIntArrayRegion(env, (jintArray) cstack$stackindexm2.l, cstack$stackindexm1.i, 1, &cstack$stackindexm2.i); $trycatchhandler\n" +
                    "IALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "IALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "IALOAD_S_CONST_ERROR_DESC=IALOAD npe\n" +
                    "LALOAD=(*env)->GetLongArrayRegion(env, (jlongArray) cstack$stackindexm2.l, cstack$stackindexm1.i, 1, &cstack$stackindexm2.j); $trycatchhandler\n" +
                    "LALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "LALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "LALOAD_S_CONST_ERROR_DESC=LALOAD npe\n" +
                    "FALOAD=(*env)->GetFloatArrayRegion(env, (jfloatArray) cstack$stackindexm2.l, cstack$stackindexm1.i, 1, &cstack$stackindexm2.f); $trycatchhandler\n" +
                    "FALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "FALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "FALOAD_S_CONST_ERROR_DESC=FALOAD npe\n" +
                    "DALOAD=(*env)->GetDoubleArrayRegion(env, (jdoubleArray) cstack$stackindexm2.l, cstack$stackindexm1.i, 1, &cstack$stackindexm2.d); $trycatchhandler\n" +
                    "DALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "DALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "DALOAD_S_CONST_ERROR_DESC=DALOAD npe\n" +
                    "AALOAD=cstack$stackindexm2.l = (*env)->GetObjectArrayElement(env, cstack$stackindexm2.l, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "AALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "AALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "AALOAD_S_CONST_ERROR_DESC=AALOAD npe\n" +
                    "BALOAD= (*env)->GetByteArrayRegion(env, cstack$stackindexm2.l, cstack$stackindexm1.i, 1, &cstack$stackindexm2.b); \\n    cstack$stackindexm2.i = cstack$stackindexm2.b; $trycatchhandler\n" +
                    "BALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "BALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "BALOAD_S_CONST_ERROR_DESC=BALOAD npe\n" +
                    "CALOAD={ \\njchar temp = 0; \\n(*env)->GetCharArrayRegion(env, (jcharArray) cstack$stackindexm2.l, cstack$stackindexm1.i, 1, &temp); \\n    cstack$stackindexm2.i = (jint) temp; \\n} $trycatchhandler\n" +
                    "CALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "CALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "CALOAD_S_CONST_ERROR_DESC=CALOAD npe\n" +
                    "SALOAD={ \\njshort temp = 0; \\n(*env)->GetShortArrayRegion(env, (jshortArray) cstack$stackindexm2.l, cstack$stackindexm1.i, 1, &temp); \\n    cstack$stackindexm2.i = (jint) temp; \\n} $trycatchhandler\n" +
                    "SALOAD_S_VARS=#NPE,#ERROR_DESC\n" +
                    "SALOAD_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "SALOAD_S_CONST_ERROR_DESC=SALOAD npe\n" +
                    "ISTORE=clocal$var.i = cstack$stackindexm1.i;\n" +
                    "LSTORE=clocal$var.j = cstack$stackindexm2.j;\n" +
                    "FSTORE=clocal$var.f = cstack$stackindexm1.f;\n" +
                    "DSTORE=clocal$var.d = cstack$stackindexm2.d;\n" +
                    "ASTORE=clocal$var.l = cstack$stackindexm1.l;\n" +
                    "IASTORE=(*env)->SetIntArrayRegion(env, (jintArray) cstack$stackindexm3.l, cstack$stackindexm2.i, 1, &cstack$stackindexm1.i); $trycatchhandler\n" +
                    "IASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "IASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "IASTORE_S_CONST_ERROR_DESC=IASTORE npe\n" +
                    "LASTORE=(*env)->SetLongArrayRegion(env, (jlongArray) cstack$stackindexm4.l, cstack$stackindexm3.i, 1, &cstack$stackindexm2.j); $trycatchhandler\n" +
                    "LASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "LASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "LASTORE_S_CONST_ERROR_DESC=LASTORE npe\n" +
                    "FASTORE=(*env)->SetFloatArrayRegion(env,(jfloatArray) cstack$stackindexm3.l, cstack$stackindexm2.i, 1, &cstack$stackindexm1.f); $trycatchhandler\n" +
                    "FASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "FASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "FASTORE_S_CONST_ERROR_DESC=FASTORE npe\n" +
                    "DASTORE=(*env)->SetDoubleArrayRegion(env, (jdoubleArray) cstack$stackindexm4.l, cstack$stackindexm3.i, 1, &cstack$stackindexm2.d); $trycatchhandler\n" +
                    "DASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "DASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "DASTORE_S_CONST_ERROR_DESC=DASTORE npe\n" +
                    "AASTORE=(*env)->SetObjectArrayElement(env, cstack$stackindexm3.l, cstack$stackindexm2.i, cstack$stackindexm1.l); $trycatchhandler\n" +
                    "AASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "AASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "AASTORE_S_CONST_ERROR_DESC=AASTORE npe\n" +
                    "BASTORE=temp0.b = (jbyte) cstack$stackindexm1.i; \\n(*env)->SetByteArrayRegion(env, cstack$stackindexm3.l, cstack$stackindexm2.i, 1, &temp0.b); $trycatchhandler\n" +
                    "BASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "BASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "BASTORE_S_CONST_ERROR_DESC=BASTORE npe\n" +
                    "CASTORE={ \\njchar temp = (jchar) cstack$stackindexm1.i; \\n(*env)->SetCharArrayRegion(env, (jcharArray) cstack$stackindexm3.l, cstack$stackindexm2.i, 1, &temp); \\n} $trycatchhandler\n" +
                    "CASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "CASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "CASTORE_S_CONST_ERROR_DESC=CASTORE npe\n" +
                    "SASTORE={ \\njshort temp = (jshort) cstack$stackindexm1.i; \\n(*env)->SetShortArrayRegion(env, (jshortArray) cstack$stackindexm3.l, cstack$stackindexm2.i, 1, &temp);\\n} $trycatchhandler\n" +
                    "SASTORE_S_VARS=#NPE,#ERROR_DESC\n" +
                    "SASTORE_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "SASTORE_S_CONST_ERROR_DESC=SASTORE npe\n" +
                    "POP=;\n" +
                    "POP2=;\n" +
                    "DUP=cstack$stackindex0 = cstack$stackindexm1;\n" +
                    "DUP_X1=cstack$stackindex0 = cstack$stackindexm1; cstack$stackindexm1 = cstack$stackindexm2; cstack$stackindexm2 = cstack$stackindex0;\n" +
                    "DUP_X2=cstack$stackindex0 = cstack$stackindexm1; cstack$stackindexm1 = cstack$stackindexm2; cstack$stackindexm2 = cstack$stackindexm3; cstack$stackindexm3 = cstack$stackindex0;\n" +
                    "DUP2=cstack$stackindex0 = cstack$stackindexm2; cstack$stackindex1 = cstack$stackindexm1;\n" +
                    "DUP2_X1=cstack$stackindex0 = cstack$stackindexm2; cstack$stackindex1 = cstack$stackindexm1; cstack$stackindexm1 = cstack$stackindexm3; cstack$stackindexm2 = cstack$stackindex1; cstack$stackindexm3 = cstack$stackindex0;\n" +
                    "DUP2_X2=cstack$stackindex0 = cstack$stackindexm2; cstack$stackindex1 = cstack$stackindexm1; cstack$stackindexm1 = cstack$stackindexm3; cstack$stackindexm2 = cstack$stackindexm4; cstack$stackindexm3 = cstack$stackindex1; cstack$stackindexm4 = cstack$stackindex0;\n" +
                    "SWAP={\\njvalue tmp = cstack$stackindexm1;\\ncstack$stackindexm1 = cstack$stackindexm2;\\ncstack$stackindexm2 = tmp;\\n}\\n\n" +
                    "IADD=cstack$stackindexm2.i = cstack$stackindexm2.i + cstack$stackindexm1.i;\n" +
                    "LADD=cstack$stackindexm4.j = cstack$stackindexm4.j + cstack$stackindexm2.j;\n" +
                    "FADD=cstack$stackindexm2.f = cstack$stackindexm2.f + cstack$stackindexm1.f;\n" +
                    "DADD=cstack$stackindexm4.d = cstack$stackindexm4.d + cstack$stackindexm2.d;\n" +
                    "ISUB=cstack$stackindexm2.i = cstack$stackindexm2.i - cstack$stackindexm1.i;\n" +
                    "LSUB=cstack$stackindexm4.j = cstack$stackindexm4.j - cstack$stackindexm2.j;\n" +
                    "FSUB=cstack$stackindexm2.f = cstack$stackindexm2.f - cstack$stackindexm1.f;\n" +
                    "DSUB=cstack$stackindexm4.d = cstack$stackindexm4.d - cstack$stackindexm2.d;\n" +
                    "IMUL=cstack$stackindexm2.i = cstack$stackindexm2.i * cstack$stackindexm1.i;\n" +
                    "LMUL=cstack$stackindexm4.j = cstack$stackindexm4.j * cstack$stackindexm2.j;\n" +
                    "FMUL=cstack$stackindexm2.f = cstack$stackindexm2.f * cstack$stackindexm1.f;\n" +
                    "DMUL=cstack$stackindexm4.d = cstack$stackindexm4.d * cstack$stackindexm2.d;\n" +
                    "IDIV=cstack$stackindexm2.i = cstack$stackindexm2.i / cstack$stackindexm1.i; \n" +
                    "IDIV_S_VARS=#AE,#ERROR_DESC\n" +
                    "IDIV_S_CONST_AE=java/lang/ArithmeticException\n" +
                    "IDIV_S_CONST_ERROR_DESC=IDIV / cn 0\n" +
                    "IDIV_STATIC=if (cstack$stackindexm1.i == 0) { (*env)->ThrowNew(env, $class_ptr(env)->clazz,  \"#ERROR_DESC\"); $trycatchhandler } \\nelse { cstack$stackindexm2.i = cstack$stackindexm2.i / cstack$stackindexm1.i; }\n" +
                    "IDIV_STATIC_S_VARS=#ERROR_DESC\n" +
                    "IDIV_STATIC_S_CONST_ERROR_DESC=/ cn 0\n" +
                    "LDIV=cstack$stackindexm4.j = cstack$stackindexm4.j / cstack$stackindexm2.j; \n" +
                    "LDIV_S_VARS=#AE,#ERROR_DESC\n" +
                    "LDIV_S_CONST_AE=java/lang/ArithmeticException\n" +
                    "LDIV_S_CONST_ERROR_DESC=LDIV / cn 0\n" +
                    "FDIV=cstack$stackindexm2.f = cstack$stackindexm2.f / cstack$stackindexm1.f;\n" +
                    "DDIV=cstack$stackindexm4.d = cstack$stackindexm4.d / cstack$stackindexm2.d;\n" +
                    "IREM=cstack$stackindexm2.i = cstack$stackindexm2.i % cstack$stackindexm1.i;\n" +
                    "IREM_S_VARS=#AE,#ERROR_DESC\n" +
                    "IREM_S_CONST_AE=java/lang/ArithmeticException\n" +
                    "IREM_S_CONST_ERROR_DESC=IREM % cn 0\n" +
                    "LREM=cstack$stackindexm4.j = cstack$stackindexm4.j % cstack$stackindexm2.j;\n" +
                    "LREM_S_VARS=#AE,#ERROR_DESC\n" +
                    "LREM_S_CONST_AE=java/lang/ArithmeticException\n" +
                    "LREM_S_CONST_ERROR_DESC=LREM % cn 0\n" +
                    "FREM=cstack$stackindexm2.f = fmod(cstack$stackindexm2.f, cstack$stackindexm1.f);\n" +
                    "DREM=cstack$stackindexm4.d = fmod(cstack$stackindexm4.d, cstack$stackindexm2.d);\n" +
                    "INEG=cstack$stackindexm1.i = -cstack$stackindexm1.i;\n" +
                    "LNEG=cstack$stackindexm2.j = -cstack$stackindexm2.j;\n" +
                    "FNEG=cstack$stackindexm1.f = -cstack$stackindexm1.f;\n" +
                    "DNEG=cstack$stackindexm2.d = -cstack$stackindexm2.d;\n" +
                    "ISHL=cstack$stackindexm2.i = cstack$stackindexm2.i << (0x1f & cstack$stackindexm1.i);\n" +
                    "LSHL=cstack$stackindexm3.j = cstack$stackindexm3.j << (0x3f & cstack$stackindexm1.i);\n" +
                    "ISHR=cstack$stackindexm2.i = cstack$stackindexm2.i >> (0x1f & cstack$stackindexm1.i);\n" +
                    "LSHR=cstack$stackindexm3.j = cstack$stackindexm3.j >> (0x3f & cstack$stackindexm1.i);\n" +
                    "IUSHR=cstack$stackindexm2.i = (jint) (((uint32_t) cstack$stackindexm2.i) >> (((uint32_t) cstack$stackindexm1.i) & 0x1f));\n" +
                    "LUSHR=cstack$stackindexm3.j = (jlong) (((uint64_t) cstack$stackindexm3.j) >> (((uint64_t) cstack$stackindexm1.i) & 0x3f));\n" +
                    "IAND=cstack$stackindexm2.i = cstack$stackindexm2.i & cstack$stackindexm1.i;\n" +
                    "LAND=cstack$stackindexm4.j = cstack$stackindexm4.j & cstack$stackindexm2.j;\n" +
                    "IOR=cstack$stackindexm2.i = cstack$stackindexm2.i | cstack$stackindexm1.i;\n" +
                    "LOR=cstack$stackindexm4.j = cstack$stackindexm4.j | cstack$stackindexm2.j;\n" +
                    "IXOR=cstack$stackindexm2.i = cstack$stackindexm2.i ^ cstack$stackindexm1.i;\n" +
                    "LXOR=cstack$stackindexm4.j = cstack$stackindexm4.j ^ cstack$stackindexm2.j;\n" +
                    "IINC=clocal$var.i += $incr;\n" +
                    "I2L=cstack$stackindexm1.j = cstack$stackindexm1.i;\n" +
                    "I2F=cstack$stackindexm1.f = (jfloat) cstack$stackindexm1.i;\n" +
                    "I2D=cstack$stackindexm1.d = (jdouble) cstack$stackindexm1.i;\n" +
                    "L2I=cstack$stackindexm2.i = (jint) cstack$stackindexm2.j;\n" +
                    "L2F=cstack$stackindexm2.f = (jfloat) cstack$stackindexm2.j;\n" +
                    "L2D=cstack$stackindexm2.d = (jdouble) cstack$stackindexm2.j;\n" +
                    "F2I=cstack$stackindexm1.i = (jint) cstack$stackindexm1.f;\n" +
                    "F2L=cstack$stackindexm1.j = (jlong) cstack$stackindexm1.f;\n" +
                    "F2D=cstack$stackindexm1.d = (jdouble) cstack$stackindexm1.f;\n" +
                    "D2I=cstack$stackindexm2.i = (jint) cstack$stackindexm2.d;\n" +
                    "D2L=cstack$stackindexm2.j = (jlong) cstack$stackindexm2.d;\n" +
                    "D2F=cstack$stackindexm2.f = (jfloat) cstack$stackindexm2.d;\n" +
                    "I2B=cstack$stackindexm1.i = (jint) (jbyte) cstack$stackindexm1.i;\n" +
                    "I2C=cstack$stackindexm1.i = (jint) (jchar) cstack$stackindexm1.i;\n" +
                    "I2S=cstack$stackindexm1.i = (jint) (jshort) cstack$stackindexm1.i;\n" +
                    "LCMP=cstack$stackindexm4.i = (cstack$stackindexm4.j == cstack$stackindexm2.j) ? 0 : (cstack$stackindexm4.j > cstack$stackindexm2.j ? 1 : -1);\n" +
                    "FCMPL={ \\njfloat value1 = cstack$stackindexm2.f; \\njfloat value2 = cstack$stackindexm1.f; \\ncstack$stackindexm2.i = value1 > value2 ? 1 : ((value1 == value2) ? 0 : ((value1 < value2) ? -1 : -1)); \\n}\n" +
                    "FCMPG={ \\njfloat value1 = cstack$stackindexm2.f; \\njfloat value2 = cstack$stackindexm1.f; \\ncstack$stackindexm2.i = value1 > value2 ? 1 : ((value1 == value2) ? 0 : ((value1 < value2) ? -1 : 1)); \\n}\n" +
                    "DCMPL={ \\njdouble value1 = cstack$stackindexm4.d; \\njdouble value2 = cstack$stackindexm2.d; \\ncstack$stackindexm4.i = value1 > value2 ? 1 : ((value1 == value2) ? 0 : ((value1 < value2) ? -1 : -1)); \\n}\n" +
                    "DCMPG={ \\njdouble value1 = cstack$stackindexm4.d; \\njdouble value2 = cstack$stackindexm2.d; \\ncstack$stackindexm4.i = value1 > value2 ? 1 : ((value1 == value2) ? 0 : ((value1 < value2) ? -1 : 1)); \\n}\n" +
                    "IFEQ=if (cstack$stackindexm1.i == 0) goto $label;\n" +
                    "IFNE=if (cstack$stackindexm1.i != 0) goto $label;\n" +
                    "IFLT=if (cstack$stackindexm1.i < 0) goto $label;\n" +
                    "IFLE=if (cstack$stackindexm1.i <= 0) goto $label;\n" +
                    "IFGT=if (cstack$stackindexm1.i > 0) goto $label;\n" +
                    "IFGE=if (cstack$stackindexm1.i >= 0) goto $label;\n" +
                    "IF_ICMPEQ=if (cstack$stackindexm2.i == cstack$stackindexm1.i) goto $label;\n" +
                    "IF_ICMPNE=if (cstack$stackindexm2.i != cstack$stackindexm1.i) goto $label;\n" +
                    "IF_ICMPLT=if (cstack$stackindexm2.i < cstack$stackindexm1.i) goto $label;\n" +
                    "IF_ICMPLE=if (cstack$stackindexm2.i <= cstack$stackindexm1.i) goto $label;\n" +
                    "IF_ICMPGT=if (cstack$stackindexm2.i > cstack$stackindexm1.i) goto $label;\n" +
                    "IF_ICMPGE=if (cstack$stackindexm2.i >= cstack$stackindexm1.i) goto $label;\n" +
                    "IF_ACMPEQ=if ((*env)->IsSameObject(env, cstack$stackindexm2.l, cstack$stackindexm1.l)) goto $label;\n" +
                    "IF_ACMPNE=if (!(*env)->IsSameObject(env, cstack$stackindexm2.l, cstack$stackindexm1.l)) goto $label;\n" +
                    "GOTO=goto $label;\n" +
                    "IRETURN=return ($rettype) cstack$stackindexm1.i;\n" +
                    "LRETURN=return ($rettype) cstack$stackindexm2.j;\n" +
                    "FRETURN=return ($rettype) cstack$stackindexm1.f;\n" +
                    "DRETURN=return ($rettype) cstack$stackindexm2.d;\n" +
                    "ARETURN=return ($rettype) cstack$stackindexm1.l;\n" +
                    "RETURN=return;\n" +
                    "NEW=if(!$class_ptr(env)->clazz){$retvalue}cstack$stackindex0.l = (*env)->AllocObject(env, $class_ptr(env)->clazz);if(!cstack$stackindex0.l){$retvalue} $trycatchhandler\n" +
                    "NEW_CATCH=if(!$class_ptr(env)->clazz){$retvalue}cstack$stackindex0.l = (*env)->AllocObject(env, $class_ptr(env)->clazz);if(!cstack$stackindex0.l){$retvalue} $trycatchhandler\n" +
                    "ANEWARRAY=if (cstack$stackindexm1.i < 0) throw_exception(env, \"#NASE\", \"#ERROR_DESC\", $line); \\nelse { cstack$stackindexm1.l = (*env)->NewObjectArray(env, cstack$stackindexm1.i, $class_ptr(env)->clazz, NULL);}$trycatchhandler\n" +
                    "ANEWARRAY_S_VARS=#NASE,#ERROR_DESC\n" +
                    "ANEWARRAY_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "ANEWARRAY_S_CONST_ERROR_DESC=ANEWARRAY array size < 0\n" +
                    "ARRAYLENGTH=cstack$stackindexm1.i = (*env)->GetArrayLength(env, cstack$stackindexm1.l); $trycatchhandler\n" +
                    "ARRAYLENGTH_S_VARS=#NPE,#ERROR_DESC\n" +
                    "ARRAYLENGTH_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "ARRAYLENGTH_S_CONST_ERROR_DESC=ARRAYLENGTH npe\n" +
                    "ATHROW=if (cstack$stackindexm1.l == NULL) (*env)->ThrowNew(env, $class_ptr(env)->clazz, \"\");else (*env)->Throw(env, cstack$stackindexm1.l);$trycatchhandler\n" +
                    "ATHROW_S_VARS=#NPE,#ERROR_DESC\n" +
                    "ATHROW_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "ATHROW_S_CONST_ERROR_DESC=ATHROW npe\n" +
                    "INSTANCEOF=cstack$stackindexm1.i = cstack$stackindexm1.l != NULL && (*env)->IsInstanceOf(env, cstack$stackindexm1.l, $class_ptr(env)->clazz);\n" +
                    "MONITORENTER=(*env)->MonitorEnter(env, cstack$stackindexm1.l); $trycatchhandler\n" +
                    "MONITORENTER_S_VARS=#NPE,#ERROR_DESC\n" +
                    "MONITORENTER_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "MONITORENTER_S_CONST_ERROR_DESC=MONITORENTER npe\n" +
                    "MONITOREXIT=(*env)->MonitorExit(env, cstack$stackindexm1.l); $trycatchhandler\n" +
                    "MONITOREXIT_S_VARS=#NPE,#ERROR_DESC\n" +
                    "MONITOREXIT_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "MONITOREXIT_S_CONST_ERROR_DESC=MONITORENTER npe\n" +
                    "IFNULL=if (cstack$stackindexm1.l == NULL) goto $label;\n" +
                    "IFNONNULL=if (cstack$stackindexm1.l != NULL) goto $label;\n" +
                    "TABLESWITCH_START=switch (cstack$stackindexm1.i) {\n" +
                    "TABLESWITCH_PART=    case $index: goto $label; break;\n" +
                    "TABLESWITCH_DEFAULT=    default: goto $label; break;\n" +
                    "TABLESWITCH_END=}\n" +
                    "LOOKUPSWITCH_START=switch (cstack$stackindexm1.i) {\n" +
                    "LOOKUPSWITCH_PART=    case $key: goto $label; break;\n" +
                    "LOOKUPSWITCH_DEFAULT=    default: goto $label; break;\n" +
                    "LOOKUPSWITCH_END=}\n" +
                    "TRYCATCH_START=if ((*env)->ExceptionCheck(env)) { \n" +
                    "TRYCATCH_CHECK_STACK=if ((*env)->IsInstanceOf(env, cstack0.l, $class_ptr(env)->clazz)) { \\n(*env)->ExceptionClear(env);\\n goto $handler_block; \\n}\n" +
                    "TRYCATCH_ANY_L=goto $handler_block;\n" +
                    "TRYCATCH_END_STACK=(*env)->Throw(env, (jthrowable) cstack0.l); return temp0.$rettype;\n" +
                    "TRYCATCH_END_STACK_VOID=(*env)->Throw(env, (jthrowable) cstack0.l);return;\n" +
                    "TRYCATCH_EMPTY=if ((*env)->ExceptionCheck(env)) { return temp0.$rettype; }\n" +
                    "TRYCATCH_VOID=if ((*env)->ExceptionCheck(env)) { return; }\n" +
                    "GETSTATIC_1=cstack$stackindex0.i = (*env)->GetStaticBooleanField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_2=cstack$stackindex0.i = (*env)->GetStaticCharField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_3=cstack$stackindex0.i = (*env)->GetStaticByteField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_4=cstack$stackindex0.i = (*env)->GetStaticShortField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_5=cstack$stackindex0.i = (*env)->GetStaticIntField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_6=cstack$stackindex0.f = (*env)->GetStaticFloatField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_7=cstack$stackindex0.j = (*env)->GetStaticLongField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_8=cstack$stackindex0.d = (*env)->GetStaticDoubleField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_9=cstack$stackindex0.l = (*env)->GetStaticObjectField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_10=cstack$stackindex0.l = (*env)->GetStaticObjectField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETSTATIC_11=cstack$stackindex0.l = (*env)->GetStaticObjectField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_1=cstack$stackindexm1.i = (*env)->GetBooleanField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_1_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_1_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_1_S_CONST_ERROR_DESC=GETFIELD Boolean npe\n" +
                    "GETFIELD_2=cstack$stackindexm1.i = (*env)->GetCharField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_2_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_2_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_2_S_CONST_ERROR_DESC=GETFIELD Char npe\n" +
                    "GETFIELD_3=cstack$stackindexm1.i = (*env)->GetByteField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_3_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_3_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_3_S_CONST_ERROR_DESC=GETFIELD Byte npe\n" +
                    "GETFIELD_4=cstack$stackindexm1.i = (*env)->GetShortField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_4_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_4_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_4_S_CONST_ERROR_DESC=GETFIELD Short npe\n" +
                    "GETFIELD_5=cstack$stackindexm1.i = (*env)->GetIntField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_5_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_5_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_5_S_CONST_ERROR_DESC=GETFIELD Int npe\n" +
                    "GETFIELD_6=cstack$stackindexm1.f = (*env)->GetFloatField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_6_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_6_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_6_S_CONST_ERROR_DESC=GETFIELD Float npe\n" +
                    "GETFIELD_7=cstack$stackindexm1.j = (*env)->GetLongField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_7_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_7_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_7_S_CONST_ERROR_DESC=GETFIELD Long npe\n" +
                    "GETFIELD_8=cstack$stackindexm1.d = (*env)->GetDoubleField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_8_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_8_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_8_S_CONST_ERROR_DESC=GETFIELD Double npe\n" +
                    "GETFIELD_9=cstack$stackindexm1.l = (*env)->GetObjectField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_9_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_9_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_9_S_CONST_ERROR_DESC=GETFIELD Object npe\n" +
                    "GETFIELD_10=cstack$stackindexm1.l = (*env)->GetObjectField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_10_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_10_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_10_S_CONST_ERROR_DESC=GETFIELD Object npe\n" +
                    "GETFIELD_11=cstack$stackindexm1.l = (*env)->GetObjectField(env, cstack$stackindexm1.l, $class_ptr(env)->$field_id); $trycatchhandler\n" +
                    "GETFIELD_11_S_VARS=#NPE,#ERROR_DESC\n" +
                    "GETFIELD_11_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "GETFIELD_11_S_CONST_ERROR_DESC=GETFIELD Object npe\n" +
                    "PUTSTATIC_1=(*env)->SetStaticBooleanField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "PUTSTATIC_2=(*env)->SetStaticCharField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "PUTSTATIC_3=(*env)->SetStaticByteField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "PUTSTATIC_4=(*env)->SetStaticShortField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "PUTSTATIC_5=(*env)->SetStaticIntField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "PUTSTATIC_6=(*env)->SetStaticFloatField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.f); $trycatchhandler\n" +
                    "PUTSTATIC_7=(*env)->SetStaticLongField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm2.j); $trycatchhandler\n" +
                    "PUTSTATIC_8=(*env)->SetStaticDoubleField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm2.d); $trycatchhandler\n" +
                    "PUTSTATIC_9=(*env)->SetStaticObjectField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.l); $trycatchhandler\n" +
                    "PUTSTATIC_10=(*env)->SetStaticObjectField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.l); $trycatchhandler\n" +
                    "PUTSTATIC_11=(*env)->SetStaticObjectField(env, $class_ptr(env)->clazz, $class_ptr(env)->$field_id, cstack$stackindexm1.l); $trycatchhandler\n" +
                    "PUTFIELD_1=(*env)->SetBooleanField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.i);$trycatchhandler\n" +
                    "PUTFIELD_1_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_1_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_1_S_CONST_ERROR_DESC=PUTFIELD Boolean npe\n" +
                    "PUTFIELD_2=(*env)->SetCharField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.i);$trycatchhandler\n" +
                    "PUTFIELD_2_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_2_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_2_S_CONST_ERROR_DESC=PUTFIELD Char npe\n" +
                    "PUTFIELD_3=(*env)->SetByteField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.i);$trycatchhandler\n" +
                    "PUTFIELD_3_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_3_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_3_S_CONST_ERROR_DESC=PUTFIELD Byte npe\n" +
                    "PUTFIELD_4=(*env)->SetShortField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.i);$trycatchhandler\n" +
                    "PUTFIELD_4_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_4_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_4_S_CONST_ERROR_DESC=PUTFIELD Short npe\n" +
                    "PUTFIELD_5=(*env)->SetIntField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.i);$trycatchhandler\n" +
                    "PUTFIELD_5_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_5_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_5_S_CONST_ERROR_DESC=PUTFIELD Int npe\n" +
                    "PUTFIELD_6=(*env)->SetFloatField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.f);$trycatchhandler\n" +
                    "PUTFIELD_6_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_6_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_6_S_CONST_ERROR_DESC=PUTFIELD Float npe\n" +
                    "PUTFIELD_7=(*env)->SetLongField(env, cstack$stackindexm3.l, $class_ptr(env)->$field_id, cstack$stackindexm2.j);$trycatchhandler\n" +
                    "PUTFIELD_7_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_7_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_7_S_CONST_ERROR_DESC=PUTFIELD Long npe\n" +
                    "PUTFIELD_8=(*env)->SetDoubleField(env, cstack$stackindexm3.l, $class_ptr(env)->$field_id, cstack$stackindexm2.d);$trycatchhandler\n" +
                    "PUTFIELD_8_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_8_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_8_S_CONST_ERROR_DESC=PUTFIELD Double npe\n" +
                    "PUTFIELD_9=(*env)->SetObjectField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.l);$trycatchhandler\n" +
                    "PUTFIELD_9_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_9_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_9_S_CONST_ERROR_DESC=PUTFIELD Object npe\n" +
                    "PUTFIELD_10=(*env)->SetObjectField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.l);$trycatchhandler\n" +
                    "PUTFIELD_10_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_10_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_10_S_CONST_ERROR_DESC=PUTFIELD Object npe\n" +
                    "PUTFIELD_11=(*env)->SetObjectField(env, cstack$stackindexm2.l, $class_ptr(env)->$field_id, cstack$stackindexm1.l);$trycatchhandler\n" +
                    "PUTFIELD_11_S_VARS=#NPE,#ERROR_DESC\n" +
                    "PUTFIELD_11_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "PUTFIELD_11_S_CONST_ERROR_DESC=PUTFIELD Object npe\n" +
                    "NEWARRAY_4= cstack$stackindexm1.l = (*env)->NewBooleanArray(env, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "NEWARRAY_4_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_4_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_4_S_CONST_ERROR_DESC=NEWARRAY Boolean array size < 0\n" +
                    "NEWARRAY_5=cstack$stackindexm1.l = (*env)->NewCharArray(env, cstack$stackindexm1.i);  $trycatchhandler\n" +
                    "NEWARRAY_5_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_5_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_5_S_CONST_ERROR_DESC=NEWARRAY Char array size < 0\n" +
                    "NEWARRAY_6= cstack$stackindexm1.l = (*env)->NewFloatArray(env, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "NEWARRAY_6_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_6_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_6_S_CONST_ERROR_DESC=NEWARRAY Float array size < 0\n" +
                    "NEWARRAY_7= cstack$stackindexm1.l = (*env)->NewDoubleArray(env, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "NEWARRAY_7_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_7_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_7_S_CONST_ERROR_DESC=NEWARRAY Double array size < 0\n" +
                    "NEWARRAY_8= cstack$stackindexm1.l = (*env)->NewByteArray(env, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "NEWARRAY_8_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_8_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_8_S_CONST_ERROR_DESC=NEWARRAY Byte array size < 0\n" +
                    "NEWARRAY_9=cstack$stackindexm1.l = (*env)->NewShortArray(env, cstack$stackindexm1.i);  $trycatchhandler\n" +
                    "NEWARRAY_9_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_9_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_9_S_CONST_ERROR_DESC=NEWARRAY Short array size < 0\n" +
                    "NEWARRAY_10=cstack$stackindexm1.l = (*env)->NewIntArray(env, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "NEWARRAY_10_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_10_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_10_S_CONST_ERROR_DESC=NEWARRAY Int array size < 0\n" +
                    "NEWARRAY_11= cstack$stackindexm1.l = (*env)->NewLongArray(env, cstack$stackindexm1.i); $trycatchhandler\n" +
                    "NEWARRAY_11_S_VARS=#NASE,#ERROR_DESC\n" +
                    "NEWARRAY_11_S_CONST_NASE=java/lang/NegativeArraySizeException\n" +
                    "NEWARRAY_11_S_CONST_ERROR_DESC=NEWARRAY Long array size < 0\n" +
                    "INVOKE_ARG_1=cstack$index.i\n" +
                    "INVOKE_ARG_2=cstack$index.i\n" +
                    "INVOKE_ARG_3=cstack$index.i\n" +
                    "INVOKE_ARG_4=cstack$index.i\n" +
                    "INVOKE_ARG_5=cstack$index.i\n" +
                    "INVOKE_ARG_6=cstack$index.f\n" +
                    "INVOKE_ARG_7=cstack$index.j\n" +
                    "INVOKE_ARG_8=cstack$index.d\n" +
                    "INVOKE_ARG_9=cstack$index.l\n" +
                    "INVOKE_ARG_10=cstack$index.l\n" +
                    "INVOKE_ARG_11=cstack$index.l\n" +
                    "INVOKESPECIAL_0=(*env)->CallNonvirtualVoidMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_0_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_0_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_0_S_CONST_ERROR_DESC=INVOKESPECIAL Void npe\n" +
                    "INVOKESPECIAL_1=cstack$returnstackindex.i = (*env)->CallNonvirtualBooleanMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_1_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_1_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_1_S_CONST_ERROR_DESC=INVOKESPECIAL Boolean npe\n" +
                    "INVOKESPECIAL_2=cstack$returnstackindex.i = (*env)->CallNonvirtualCharMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_2_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_2_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_2_S_CONST_ERROR_DESC=INVOKESPECIAL Char npe\n" +
                    "INVOKESPECIAL_3=cstack$returnstackindex.i = (*env)->CallNonvirtualByteMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_3_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_3_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_3_S_CONST_ERROR_DESC=INVOKESPECIAL Bye npe\n" +
                    "INVOKESPECIAL_4=cstack$returnstackindex.i = (*env)->CallNonvirtualShortMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_4_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_4_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_4_S_CONST_ERROR_DESC=INVOKESPECIAL Short npe\n" +
                    "INVOKESPECIAL_5=cstack$returnstackindex.i = (*env)->CallNonvirtualIntMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_5_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_5_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_5_S_CONST_ERROR_DESC=INVOKESPECIAL Int npe\n" +
                    "INVOKESPECIAL_6=cstack$returnstackindex.f = (*env)->CallNonvirtualFloatMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_6_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_6_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_6_S_CONST_ERROR_DESC=INVOKESPECIAL Float npe\n" +
                    "INVOKESPECIAL_7=cstack$returnstackindex.j = (*env)->CallNonvirtualLongMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_7_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_7_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_7_S_CONST_ERROR_DESC=INVOKESPECIAL Long npe\n" +
                    "INVOKESPECIAL_8=cstack$returnstackindex.d = (*env)->CallNonvirtualDoubleMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_8_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_8_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_8_S_CONST_ERROR_DESC=INVOKESPECIAL Double npe\n" +
                    "INVOKESPECIAL_9=cstack$returnstackindex.l = (*env)->CallNonvirtualObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_9_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_9_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_9_S_CONST_ERROR_DESC=INVOKESPECIAL Object npe\n" +
                    "INVOKESPECIAL_10=cstack$returnstackindex.l = (*env)->CallNonvirtualObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_10_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_10_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_10_S_CONST_ERROR_DESC=INVOKESPECIAL Object npe\n" +
                    "INVOKESPECIAL_11=cstack$returnstackindex.l = (*env)->CallNonvirtualObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKESPECIAL_11_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKESPECIAL_11_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKESPECIAL_11_S_CONST_ERROR_DESC=INVOKESPECIAL Object npe\n" +
                    "INVOKEINTERFACE_0=(*env)->CallVoidMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "CallIntMethod(env, stack0.l, c_18_(env)->id_3, stack1.i);\n" +
                    "INVOKEINTERFACE_0_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_0_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_0_S_CONST_ERROR_DESC=INVOKEINTERFACE Void npe\n" +
                    "INVOKEINTERFACE_1=cstack$returnstackindex.i = (*env)->CallBooleanMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_1_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_1_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_1_S_CONST_ERROR_DESC=INVOKEINTERFACE Boolean npe\n" +
                    "INVOKEINTERFACE_2=if (cstack$objectstackindex.l == NULL) throw_exception(env, \"#NPE\", \"#ERROR_DESC\", $line); \\nelse cstack$returnstackindex.i = (*env)->CallCharMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_2_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_2_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_2_S_CONST_ERROR_DESC=INVOKEINTERFACE Char npe\n" +
                    "INVOKEINTERFACE_3=cstack$returnstackindex.i = (*env)->CallByteMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_3_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_3_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_3_S_CONST_ERROR_DESC=INVOKEINTERFACE Bye npe\n" +
                    "INVOKEINTERFACE_4=cstack$returnstackindex.i = (*env)->CallShortMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_4_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_4_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_4_S_CONST_ERROR_DESC=INVOKEINTERFACE Short npe\n" +
                    "INVOKEINTERFACE_5=cstack$returnstackindex.i = (*env)->CallIntMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_5_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_5_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_5_S_CONST_ERROR_DESC=INVOKEINTERFACE Int npe\n" +
                    "INVOKEINTERFACE_6=cstack$returnstackindex.f = (*env)->CallFloatMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_6_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_6_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_6_S_CONST_ERROR_DESC=INVOKEINTERFACE Float npe\n" +
                    "INVOKEINTERFACE_7=cstack$returnstackindex.j = (*env)->CallLongMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_7_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_7_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_7_S_CONST_ERROR_DESC=INVOKEINTERFACE Long npe\n" +
                    "INVOKEINTERFACE_8=cstack$returnstackindex.d = (*env)->CallDoubleMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_8_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_8_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_8_S_CONST_ERROR_DESC=INVOKEINTERFACE Double npe\n" +
                    "INVOKEINTERFACE_9=cstack$returnstackindex.l = (*env)->CallObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_9_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_9_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_9_S_CONST_ERROR_DESC=INVOKEINTERFACE Object npe\n" +
                    "INVOKEINTERFACE_10=cstack$returnstackindex.l = (*env)->CallObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_10_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_10_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_10_S_CONST_ERROR_DESC=INVOKEINTERFACE Object npe\n" +
                    "INVOKEINTERFACE_11=cstack$returnstackindex.l = (*env)->CallObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEINTERFACE_11_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEINTERFACE_11_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEINTERFACE_11_S_CONST_ERROR_DESC=INVOKEINTERFACE Object npe\n" +
                    "INVOKEVIRTUAL_0=(*env)->CallVoidMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_0_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_0_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_0_S_CONST_ERROR_DESC=INVOKEVIRTUAL Void npe\n" +
                    "INVOKEVIRTUAL_1=cstack$returnstackindex.i = (*env)->CallBooleanMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_1_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_1_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_1_S_CONST_ERROR_DESC=INVOKEVIRTUAL Boolean npe\n" +
                    "INVOKEVIRTUAL_2=cstack$returnstackindex.i = (*env)->CallCharMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_2_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_2_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_2_S_CONST_ERROR_DESC=INVOKEVIRTUAL Char npe\n" +
                    "INVOKEVIRTUAL_3=cstack$returnstackindex.i = (*env)->CallByteMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_3_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_3_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_3_S_CONST_ERROR_DESC=INVOKEVIRTUAL Bye npe\n" +
                    "INVOKEVIRTUAL_4=cstack$returnstackindex.i = (*env)->CallShortMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_4_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_4_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_4_S_CONST_ERROR_DESC=INVOKEVIRTUAL Short npe\n" +
                    "INVOKEVIRTUAL_5=cstack$returnstackindex.i = (*env)->CallIntMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_5_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_5_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_5_S_CONST_ERROR_DESC=INVOKEVIRTUAL Int npe\n" +
                    "INVOKEVIRTUAL_6=cstack$returnstackindex.f = (*env)->CallFloatMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_6_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_6_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_6_S_CONST_ERROR_DESC=INVOKEVIRTUAL Float npe\n" +
                    "INVOKEVIRTUAL_7=cstack$returnstackindex.j = (*env)->CallLongMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_7_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_7_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_7_S_CONST_ERROR_DESC=INVOKEVIRTUAL Long npe\n" +
                    "INVOKEVIRTUAL_8=cstack$returnstackindex.d = (*env)->CallDoubleMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_8_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_8_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_8_S_CONST_ERROR_DESC=INVOKEVIRTUAL Double npe\n" +
                    "INVOKEVIRTUAL_9=cstack$returnstackindex.l = (*env)->CallObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_9_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_9_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_9_S_CONST_ERROR_DESC=INVOKEVIRTUAL Object npe\n" +
                    "INVOKEVIRTUAL_10=cstack$returnstackindex.l = (*env)->CallObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_10_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_10_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_10_S_CONST_ERROR_DESC=INVOKEVIRTUAL Object npe\n" +
                    "INVOKEVIRTUAL_11=cstack$returnstackindex.l = (*env)->CallObjectMethod(env, cstack$objectstackindex.l, $class_ptr(env)->$methodid$args);$trycatchhandler\n" +
                    "INVOKEVIRTUAL_11_S_VARS=#NPE,#ERROR_DESC\n" +
                    "INVOKEVIRTUAL_11_S_CONST_NPE=java/lang/NullPointerException\n" +
                    "INVOKEVIRTUAL_11_S_CONST_ERROR_DESC=INVOKEVIRTUAL Object npe\n" +
                    "INVOKESTATIC_0=(*env)->CallStaticVoidMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_1=cstack$returnstackindex.i = (*env)->CallStaticBooleanMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_2=cstack$returnstackindex.i = (*env)->CallStaticCharMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_3=cstack$returnstackindex.i = (*env)->CallStaticByteMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_4=cstack$returnstackindex.i = (*env)->CallStaticShortMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_5=cstack$returnstackindex.i = (*env)->CallStaticIntMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_6=cstack$returnstackindex.f = (*env)->CallStaticFloatMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_7=cstack$returnstackindex.j = (*env)->CallStaticLongMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_8=cstack$returnstackindex.d = (*env)->CallStaticDoubleMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_9=cstack$returnstackindex.l = (*env)->CallStaticObjectMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_10=cstack$returnstackindex.l = (*env)->CallStaticObjectMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "INVOKESTATIC_11=cstack$returnstackindex.l = (*env)->CallStaticObjectMethod(env, $class_ptr(env)->clazz, $class_ptr(env)->$methodid$args); $trycatchhandler\n" +
                    "CHECKCAST=if (cstack$stackindexm1.l != NULL && !(*env)->IsInstanceOf(env, cstack$stackindexm1.l, $class_ptr(env)->clazz)){throw_exception(env, \"#CCE\", \"#ERROR_DESC\", $line); (*env)->ThrowNew(env, $exception_ptr(env)->clazz, \"\");}$trycatchhandler\n" +
                    "CHECKCAST_S_VARS=#CCE,#ERROR_DESC,$desc\n" +
                    "CHECKCAST_S_CONST_CCE=java/lang/ClassCastException\n" +
                    "CHECKCAST_S_CONST_ERROR_DESC=cannot cast to \n";
            this.snippets.load(new ByteArrayInputStream(sb.getBytes()));
        }
        catch (IOException e) {
            throw new RuntimeException("Can't load cpp snippets", e);
        }
    }
    
    private String[] getVars(String key) {
        key += "_S_VARS";
        final String result = this.snippets.getProperty(key);
        if (result == null || result.isEmpty()) {
            return new String[0];
        }
        return result.split(",");
    }
    
    public String getSnippet(final String key) {
        return this.getSnippet(key, Util.createMap(new Object[0]));
    }
    
    public String getSnippet(final String key, final Map<String, String> tokens) {
        final String value = this.snippets.getProperty(key);
        if (value == null) {
            return "";
        }
        final String[] stringVars = this.getVars(key);
        final Map<String, String> result = new HashMap<String, String>();
        for (final String var2 : stringVars) {
            if (var2.startsWith("#")) {
                result.put(var2, this.snippets.getProperty(key + "_S_CONST_" + var2.substring(1)));
            }
            else {
                if (!var2.startsWith("$")) {
                    throw new RuntimeException("Unknown format modifier: " + var2);
                }
                result.put(var2, tokens.get(var2.substring(1)));
            }
        }
        final RuntimeException ex;
        result.entrySet().stream().filter(var -> var.getValue() == null).findAny().ifPresent(entry -> {

            throw new RuntimeException(key + " - token value can't be null");
        });
        tokens.forEach((k, v) -> result.putIfAbsent("$" + k, v));
        return Util.dynamicRawFormat(value, result);
    }
}
