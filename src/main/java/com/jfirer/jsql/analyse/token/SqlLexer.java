package com.jfirer.jsql.analyse.token;

import com.jfirer.jsql.analyse.token.parser.TokenParser;
import com.jfirer.jsql.analyse.token.parser.impl.*;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.util.*;

public class SqlLexer
{
    private static final TokenParser firstParser;
    static
    {
        TokenParser[] parsers = new TokenParser[]{ //
                new SkipWhiteSpaceParser(), //
                new ExecutionParser(), //
                new ExpressionParser(), //
                new TemplateCharacterParser(), //
                new AutoCollectionParser(), //
                new NumberParser(), //
                new SymbolParser(), //
                new TextParser(), //
                new LiteralsParser(), //
        };
        TokenParser next = parsers[parsers.length - 1];
        next.setNextParser(new TokenParser.EmptyParser());
        for (int i = parsers.length - 2; i > -1; i--)
        {
            parsers[i].setNextParser(next);
            next = parsers[i];
        }
        firstParser = parsers[0];
    }
    private Token[] tokens;

    public static SqlLexer parse(String sql)
    {
        return new SqlLexer(sql);
    }

    private SqlLexer(String sql)
    {
        int          length = sql.length();
        int          offset = 0;
        Deque<Token> tmp    = new LinkedList<Token>();
        while (offset < length)
        {
            int pred = offset;
            offset = firstParser.parse(sql, offset, tmp);
            if (pred == offset)
            {
                throw new UnsupportedOperationException("无法解析" + sql.substring(offset));
            }
        }
        Collections.reverse((List<?>) tmp);
        tokens = tmp.toArray(new Token[tmp.size()]);
    }

    /**
     * 将类名，属性名转换为表名和列名
     *
     * @param transfers
     */
    public SqlLexer transfer(Map<String, TableEntityInfo> transfers)
    {
        List<TableEntityInfo> hit = markEntityToken(transfers);
        transferPropertyNameToColumnName(hit);
        Map<String, String> entityAliasNameMap = findEntityAliasName();
        transferPropertyNameWithAliasEntity(transfers, entityAliasNameMap);
        for (Token token : tokens)
        {
            if (token.getTokenType() == TokenType.TABLE_ENTITY)
            {
                token.setListerals(transfers.get(token.getListerals()).getTableName());
            }
        }
        return this;
    }

    /**
     * @param transfers
     * @param entityAliasNameMap
     */
    private void transferPropertyNameWithAliasEntity(Map<String, TableEntityInfo> transfers, Map<String, String> entityAliasNameMap)
    {
        Set<String> entityAliasStart = entityAliasNameMap.keySet();
        for (Token token : tokens)
        {
            if (token.getTokenType() == TokenType.IDENTIFIER)
            {
                String listerals = token.getListerals();
                for (String each : entityAliasStart)
                {
                    if (listerals.startsWith(each))
                    {
                        String propertyName = listerals.substring(each.length());
                        String columnName   = transfers.get(entityAliasNameMap.get(each)).getPropertyNameKeyMap().get(propertyName).getColumnName();
                        token.setListerals(each + columnName);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 返回该SQL中实体类名的别名，并且返回别名与数据库表名的映射Map。<br/>
     * 注意：map中key为"别名."的格式
     *
     * @return
     */
    private Map<String, String> findEntityAliasName()
    {
        Token               pred               = null;
        boolean             hitAs              = false;
        Map<String, String> entityAliasNameMap = new HashMap<String, String>();
        for (Token token : tokens)
        {
            if (hitAs)
            {
                hitAs = false;
                String entityAliasName = token.getListerals();
                entityAliasNameMap.put(entityAliasName + '.', pred.getListerals());
                pred = null;
            }
            else if (isAsKeyWord(token) && (pred != null && pred.getTokenType() == TokenType.TABLE_ENTITY))
            {
                hitAs = true;
            }
            else
            {
                pred = token;
            }
        }
        return entityAliasNameMap;
    }

    /**
     * 将属性名转换为对应的数据库表列名
     *
     * @param hit
     */
    private void transferPropertyNameToColumnName(List<TableEntityInfo> hit)
    {
        for (TableEntityInfo info : hit)
        {
            for (Token token : tokens)
            {
                if (isPropertyName(info, token))
                {
                    token.setListerals(info.getPropertyNameKeyMap().get(token.getListerals()).getColumnName());
                }
            }
        }
    }

    /**
     * 标记标识符文本内容是一个实体类的简单名称的token，标记其类型为TABLE_ENTITY
     *
     * @param transfers
     * @return
     */
    private List<TableEntityInfo> markEntityToken(Map<String, TableEntityInfo> transfers)
    {
        List<TableEntityInfo> hit = new LinkedList<TableEntityInfo>();
        for (Token token : tokens)
        {
            if (isClassSImpleName(transfers, token))
            {
                hit.add(transfers.get(token.getListerals()));
                token.setTokenType(TokenType.TABLE_ENTITY);
            }
        }
        return hit;
    }

    /**
     * @param token
     * @return
     */
    private boolean isAsKeyWord(Token token)
    {
        return token.getTokenType() == TokenType.KEYWORD && KeyWord.getKeyWord(token.getListerals()) == KeyWord.AS;
    }

    /**
     * 该标识符文本内容是一个实体类的属性名称
     *
     * @param token
     * @return
     */
    private boolean isPropertyName(TableEntityInfo info, Token token)
    {
        return token.getTokenType() == TokenType.IDENTIFIER && info.getPropertyNameKeyMap().containsKey(token.getListerals());
    }

    /**
     * 该标识符文本内容是一个实体类简单类名
     *
     * @param transfers
     * @param token
     * @return
     */
    private boolean isClassSImpleName(Map<String, TableEntityInfo> transfers, Token token)
    {
        return token.getTokenType() == TokenType.IDENTIFIER && transfers.containsKey(token.getListerals());
    }

    public String format()
    {
        StringBuilder cache = new StringBuilder();
        for (Token token : tokens)
        {
            cache.append(token.getListerals()).append(' ');
        }
        if (cache.length() != 0)
        {
            cache.setLength(cache.length() - 1);
        }
        return cache.toString();
    }
}
