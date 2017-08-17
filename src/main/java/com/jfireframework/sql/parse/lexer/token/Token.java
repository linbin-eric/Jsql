package com.jfireframework.sql.parse.lexer.token;

import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;

public class Token
{
    private final TokenType tokenType;
    private final String    originalLiterals;
    private final int       endPosition;
    private String          literals;
    
    public Token(TokenType tokenType, String literals, int endPosition)
    {
        this.tokenType = tokenType;
        originalLiterals = literals;
        this.literals = literals;
        this.endPosition = endPosition;
    }
    
    public TokenType getTokenType()
    {
        return tokenType;
    }
    
    public String getOriginalLiterals()
    {
        return originalLiterals;
    }
    
    public int getEndPosition()
    {
        return endPosition;
    }
    
    public String parseEntity(MetaContext metaContext)
    {
        if (tokenType == Literals.ENTITY)
        {
            TableMetaData tableMetaData = metaContext.get(originalLiterals);
            if (tableMetaData == null)
            {
                throw new NullPointerException(StringUtil.format("无法解析:{},找不到对应的类", originalLiterals));
            }
            String entity = literals;
            literals = tableMetaData.getTableName();
            return entity;
        }
        else
        {
            return null;
        }
    }
    
    public String parseFieldName(Map<String, String> fieldNames)
    {
        if (tokenType == Literals.FIELD || tokenType == Literals.TEXT)
        {
            String colName = fieldNames.get(literals);
            if (colName != null)
            {
                literals = colName;
                return literals;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    public String getLiterals()
    {
        return literals;
    }
    
    public void setLiterals(String literals)
    {
        this.literals = literals;
    }
    
}
