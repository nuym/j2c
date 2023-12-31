package cc.nuym.jnic.instructions;

import cc.nuym.jnic.utils.MethodContext;
import cc.nuym.jnic.utils.Util;
import cc.nuym.jnic.cache.CachedClassInfo;
import cc.nuym.jnic.cache.CachedFieldInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.List;

public class FieldHandler extends GenericInstructionHandler<FieldInsnNode>
{
    @Override
    protected void process(final MethodContext context, final FieldInsnNode node) {
        boolean isStatic = node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.PUTSTATIC;
        CachedFieldInfo info = new CachedFieldInfo(node.owner, node.name, node.desc, isStatic);

        instructionName += "_" + Type.getType(node.desc).getSort();

        this.props.put("class_ptr", "c_" + context.getCachedClasses().getId(node.owner) + "_");
        final CachedClassInfo classInfo = context.getCachedClasses().getCache().get(node.owner);
        final List<CachedFieldInfo> cachedFields = classInfo.getCachedFields();
        for (int i = 0; i < cachedFields.size(); ++i) {
            final CachedFieldInfo fieldNode = cachedFields.get(i);
            if (fieldNode.getName().equals(node.name)) {
                this.props.put("field_id", "id_" + i);
            }
        }
        if (this.props.get("field_id") == null) {
            cachedFields.add(info);
            this.props.put("field_id", "id_" + (cachedFields.size() - 1));
        }
    }

    @Override
    public String insnToString(MethodContext context, FieldInsnNode node) {
        return String.format("%s %s.%s %s", Util.getOpcodeString(node.getOpcode()), node.owner, node.name, node.desc);
    }

    @Override
    public int getNewStackPointer(FieldInsnNode node, int currentStackPointer) {
        if (node.getOpcode() == Opcodes.GETFIELD || node.getOpcode() == Opcodes.PUTFIELD) {
            currentStackPointer -= 1;
        }
        if (node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.GETFIELD) {
            currentStackPointer += Type.getType(node.desc).getSize();
        }
        if (node.getOpcode() == Opcodes.PUTSTATIC || node.getOpcode() == Opcodes.PUTFIELD) {
            currentStackPointer -= Type.getType(node.desc).getSize();
        }
        return currentStackPointer;
    }
}
