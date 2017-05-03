package com.jfireframework.sql.util.smc;

import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.sql.session.mapper.Mapper;

public class DynamicCodeTool
{
    
    public static CompilerModel createMapper(Class<?> interCc)
    {
        return SmcHelper.createClientClass(Mapper.class, interCc);
    }
    
}
