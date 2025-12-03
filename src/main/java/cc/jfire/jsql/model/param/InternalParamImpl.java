package cc.jfire.jsql.model.param;

import cc.jfire.jsql.model.InternalParam;
import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.Param;
import cc.jfire.jsql.model.support.SFunction;

import java.util.List;

public abstract class InternalParamImpl implements InternalParam
{
    protected Class<?>       implClass;
    protected String         fieldName;
    protected RenderConsumer consumer;

    protected InternalParamImpl(SFunction<?, ?> fn)
    {
        implClass = fn.getImplClass();
        fieldName = fn.resolveFieldName();
    }

    public InternalParamImpl()
    {
    }

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
        public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
        {
            param1.renderSql(model, builder, paramValues);
            builder.append(" and ");
            param2.renderSql(model, builder, paramValues);
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
        public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
        {
            param1.renderSql(model, builder, paramValues);
            builder.append(" or ");
            param2.renderSql(model, builder, paramValues);
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
        public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
        {
            builder.append("( ");
            param.renderSql(model, builder, paramValues);
            builder.append(" ) ");
        }
    }

    @Override
    public Param and(Param param)
    {
        if (param == NoOpParam.INSTANCE)
        {
            return this;
        }
        else
        {
            return new AndParam(this, (InternalParam) param);
        }
    }

    @Override
    public Param or(Param param)
    {
        if (param == NoOpParam.INSTANCE)
        {
            return this;
        }
        else
        {
            return new OrParam(this, (InternalParam) param);
        }
    }

    @Override
    public Param union()
    {
        return new UnionParam(this);
    }

    @Override
    public void renderSql(Model model, StringBuilder builder, List<Object> paramValues)
    {
        consumer.accept(model.findColumnName(implClass, fieldName), builder, paramValues);
    }

    @FunctionalInterface
    interface RenderConsumer
    {
        void accept(String columnName, StringBuilder builder, List<Object> paramValues);
    }
}
