package com.jfirer.jsql.model.impl;

import com.jfirer.jsql.model.BaseModel;
import com.jfirer.jsql.model.InternalParam;
import com.jfirer.jsql.model.Param;

import java.util.List;

public abstract class InternalParamImpl implements InternalParam
{
    class AndParam extends InternalParamImpl
    {
        private final InternalParam param1;
        private final InternalParam param2;

        public AndParam(InternalParam param1, InternalParam param2)
        {
            this.param1 = param1;
            this.param2 = param2;
        }

        @Override
        public void renderSql(BaseModel model, StringBuilder builder, List<Object> paramValues)
        {
            param1.renderSql(model, builder, paramValues);
            builder.append(" and ");
            param2.renderSql(model, builder, paramValues);
            builder.append(" ");
        }

        @Override
        public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
        {
            param1.renderSql(ckass, builder, paramValues);
            builder.append(" and ");
            param2.renderSql(ckass, builder, paramValues);
            builder.append(" ");
        }
    }

    class OrParam extends InternalParamImpl
    {
        private final InternalParam param1;
        private final InternalParam param2;

        public OrParam(InternalParam param1, InternalParam param2)
        {
            this.param1 = param1;
            this.param2 = param2;
        }

        @Override
        public void renderSql(BaseModel model, StringBuilder builder, List<Object> paramValues)
        {
            param1.renderSql(model, builder, paramValues);
            builder.append(" or ");
            param2.renderSql(model, builder, paramValues);
            builder.append(" ");
        }

        @Override
        public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
        {
            param1.renderSql(ckass, builder, paramValues);
            builder.append(" or ");
            param2.renderSql(ckass, builder, paramValues);
            builder.append(" ");
        }
    }

    class UnionParam extends InternalParamImpl
    {
        private final InternalParamImpl param;

        public UnionParam(InternalParamImpl param)
        {
            this.param = param;
        }

        @Override
        public void renderSql(BaseModel model, StringBuilder builder, List<Object> paramValues)
        {
            builder.append("( ");
            param.renderSql(model, builder, paramValues);
            builder.append(" ) ");
        }

        @Override
        public void renderSql(Class ckass, StringBuilder builder, List<Object> paramValues)
        {
            builder.append("( ");
            param.renderSql(ckass, builder, paramValues);
            builder.append(" ) ");
        }
    }

    @Override
    public Param and(Param param)
    {
        return new AndParam(this, (InternalParam) param);
    }

    @Override
    public Param or(Param param)
    {
        return new OrParam(this, (InternalParam) param);
    }

    @Override
    public Param union()
    {
        return new UnionParam(this);
    }
}
