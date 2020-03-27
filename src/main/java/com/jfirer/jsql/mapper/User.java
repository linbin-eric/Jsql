package com.jfirer.jsql.mapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class User
{

    public static void main(String[] args)
    {
        for (Type each : UserOp.class.getGenericInterfaces())
        {
            if (each instanceof ParameterizedType && ((ParameterizedType) each).getRawType()== Repository.class)
            {
                Type type = ((ParameterizedType) each).getActualTypeArguments()[0];
                System.out.println(type);
            }
        }
    }
}
