package cc.nuym.jnic.special;

import cc.nuym.jnic.utils.MethodContext;

public interface SpecialMethodProcessor
{
    String preProcess(final MethodContext p0);
    
    void postProcess(final MethodContext p0);
}
