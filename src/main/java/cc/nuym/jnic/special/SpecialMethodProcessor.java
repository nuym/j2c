package cc.nuym.jnic.special;

import cc.nuym.jnic.utils.MethodContext;

public interface SpecialMethodProcessor
{
    String preprocess(final MethodContext p0);
    
    void postprocess(final MethodContext p0);
}
