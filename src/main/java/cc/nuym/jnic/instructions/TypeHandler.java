package cc.nuym.jnic.instructions;

import cc.nuym.jnic.utils.MethodContext;
import cc.nuym.jnic.MethodProcessor;
import cc.nuym.jnic.utils.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.TypeInsnNode;

public class TypeHandler extends GenericInstructionHandler<TypeInsnNode>
{
    @Override
    protected void process(final MethodContext context, final TypeInsnNode node) {
        this.props.put("desc", node.desc);
        this.props.put("class_ptr", "c_" + context.getCachedClasses().getId(node.desc) + "_");
        final String instructionName = MethodProcessor.INSTRUCTIONS.getOrDefault(node.getOpcode(), "NOTFOUND");
        if ("CHECKCAST".equals(instructionName)) {
            this.props.put("exception_ptr", "c_" + context.getCachedClasses().getId("java/lang/ClassCastException") + "_");
        }
        this.props.put("desc_ptr", node.desc);
    }
    
    @Override
    public String insnToString(final MethodContext context, final TypeInsnNode node) {
        return String.format("%s %s", Util.getOpcodeString(node.getOpcode()), node.desc);
    }
    
    @Override
    public int getNewStackPointer(final TypeInsnNode node, final int currentStackPointer) {
        switch (node.getOpcode()) {
            case Opcodes.ANEWARRAY:
            case Opcodes.CHECKCAST:
            case Opcodes.INSTANCEOF:
                return currentStackPointer;
            case Opcodes.NEW:
                return currentStackPointer + 1;
        }
        throw new RuntimeException();
    }
}
