package cc.nuym.jnic.instructions;

import cc.nuym.jnic.utils.MethodContext;
import org.objectweb.asm.tree.IincInsnNode;

public class IincHandler extends GenericInstructionHandler<IincInsnNode>
{
    @Override
    protected void process(final MethodContext context, final IincInsnNode node) {
        this.props.put("incr", String.valueOf(node.incr));
        this.props.put("var", String.valueOf(node.var));
    }
    
    @Override
    public String insnToString(final MethodContext context, final IincInsnNode node) {
        return String.format("IINC %d %d", node.var, node.incr);
    }
    
    @Override
    public int getNewStackPointer(final IincInsnNode node, final int currentStackPointer) {
        return currentStackPointer;
    }
}
