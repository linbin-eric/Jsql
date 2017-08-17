package com.jfireframework.sql.parse.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.parse.lexer.analyzer.CharType;
import com.jfireframework.sql.parse.lexer.analyzer.Tokenizer;
import com.jfireframework.sql.parse.lexer.token.DefaultKeyWord;
import com.jfireframework.sql.parse.lexer.token.Literals;
import com.jfireframework.sql.parse.lexer.token.Token;

public class Lexer
{
    private List<Token>                tokens     = new ArrayList<Token>();
    
    private final String               sql;
    private int                        offset     = 0;
    private Map<String, TableMetaData> entities   = new HashMap<String, TableMetaData>();
    private Map<String, String>        fieldNames = new HashMap<String, String>();
    
    public Lexer(String sql)
    {
        this.sql = sql;
        Token currentToken;
        while ((currentToken = nextToken()) != null)
        {
            tokens.add(currentToken);
        }
    }
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache();
        for (Token each : tokens)
        {
            cache.append(each.getLiterals()).append(' ');
        }
        if (tokens.isEmpty() == false)
        {
            cache.deleteLast();
        }
        return cache.toString();
    }
    
    public Lexer parseEntity(MetaContext metaContext)
    {
        for (Token token : tokens)
        {
            String entity = token.parseEntity(metaContext);
            if (entity != null)
            {
                TableMetaData tableMetaData = metaContext.get(entity);
                entities.put(entity, tableMetaData);
                for (MapField mapField : tableMetaData.getFieldInfos())
                {
                    fieldNames.put(entity + '.' + mapField.getFieldName(), tableMetaData.getTableName() + "." + mapField.getColName());
                    fieldNames.put(mapField.getFieldName(), tableMetaData.getTableName() + "." + mapField.getColName());
                }
            }
        }
        return this;
    }
    
    public Lexer parseEntityAlias(MetaContext metaContext)
    {
        for (int i = 0; i < tokens.size(); i++)
        {
            Token token = tokens.get(i);
            if (token.getTokenType() == DefaultKeyWord.AS && tokens.get(i - 1).getTokenType() == Literals.ENTITY)
            {
                Token entityToken = tokens.get(i - 1);
                Token aliasToken = tokens.get(i + 1);
                if (aliasToken.getTokenType() == Literals.TEXT)
                {
                    String entityAlias = aliasToken.getLiterals();
                    TableMetaData tableMetaData = metaContext.get(entityToken.getOriginalLiterals());
                    entities.put(entityAlias, tableMetaData);
                    for (MapField mapField : tableMetaData.getFieldInfos())
                    {
                        fieldNames.put(entityAlias + '.' + mapField.getFieldName(), entityAlias + "." + mapField.getColName());
                    }
                }
            }
        }
        return this;
    }
    
    public Lexer parseFieldName()
    {
        for (Token token : tokens)
        {
            token.parseFieldName(fieldNames);
        }
        return this;
    }
    
    public Lexer parseEntityAndField(MetaContext metaContext)
    {
        parseEntity(metaContext);
        parseEntityAlias(metaContext);
        parseFieldName();
        return this;
    }
    
    Token nextToken()
    {
        skipIgnoredToken();
        Token currentToken = null;
        if (isLeftBraceBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanBrace();
        }
        else if (isVariableBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanVariable();
        }
        else if (isIfBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanIf();
        }
        else if (isEndIfBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanEndIf();
        }
        else if (isIdentifierBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanIdentifier();
        }
        else if (isHexDecimalBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanHexDecimal();
        }
        else if (isNumberBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanNumber();
        }
        else if (isSymbolBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanSymbol();
        }
        else if (isCharsBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanChars();
        }
        else if (isEnd())
        {
            return null;
        }
        offset = currentToken.getEndPosition();
        return currentToken;
    }
    
    /**
     * 
     * @return
     */
    private boolean isLeftBraceBegin()
    {
        return getCurrentChar(0) == '{';
    }
    
    private boolean isIdentifierBegin()
    {
        return isIdentifierBegin(getCurrentChar(0));
    }
    
    private boolean isIdentifierBegin(final char ch)
    {
        return CharType.isAlphabet(ch);
    }
    
    private boolean isIfBegin()
    {
        if (getCurrentChar(0) == '<' && getCurrentChar(1) == 'i' && getCurrentChar(2) == 'f' && getCurrentChar(3) == '(')
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private boolean isEndIfBegin()
    {
        if (getCurrentChar(0) == '<' && getCurrentChar(1) == '/' && getCurrentChar(2) == 'i' && getCurrentChar(3) == 'f' && getCurrentChar(4) == '>')
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    protected boolean isVariableBegin()
    {
        return getCurrentChar(0) == '$';
    }
    
    protected boolean isHintBegin()
    {
        return false;
    }
    
    protected boolean isCommentBegin()
    {
        char current = getCurrentChar(0);
        char next = getCurrentChar(1);
        return '/' == current && '/' == next || '-' == current && '-' == next || '/' == current && '*' == next;
    }
    
    private boolean isHexDecimalBegin()
    {
        return '0' == getCurrentChar(0) && 'x' == getCurrentChar(1);
    }
    
    private boolean isNumberBegin()
    {
        return CharType.isDigital(getCurrentChar(0)) //
                || ('.' == getCurrentChar(0) && CharType.isDigital(getCurrentChar(1))//
                        || ('-' == getCurrentChar(0) && ('.' == getCurrentChar(0) || CharType.isDigital(getCurrentChar(1)))));
    }
    
    private boolean isSymbolBegin()
    {
        return CharType.isSymbol(getCurrentChar(0));
    }
    
    private boolean isCharsBegin()
    {
        return '\'' == getCurrentChar(0) || '\"' == getCurrentChar(0);
    }
    
    private boolean isEnd()
    {
        return offset >= sql.length();
    }
    
    protected final char getCurrentChar(final int offset)
    {
        return this.offset + offset >= sql.length() ? (char) CharType.EOI : sql.charAt(this.offset + offset);
    }
    
    private void skipIgnoredToken()
    {
        offset = new Tokenizer(sql, offset).skipWhitespace();
    }
}
