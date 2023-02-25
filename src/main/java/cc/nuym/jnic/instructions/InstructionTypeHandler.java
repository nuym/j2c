package cc.nuym.jnic.instructions;

import cc.nuym.jnic.MethodContext;
import org.objectweb.asm.tree.AbstractInsnNode;

public interface InstructionTypeHandler<T extends AbstractInsnNode>
{
    void accept(final MethodContext p0, final T p1);
    
    String insnToString(final MethodContext p0, final T p1);
    
    int getNewStackPointer(final T p0, final int p1);
}
