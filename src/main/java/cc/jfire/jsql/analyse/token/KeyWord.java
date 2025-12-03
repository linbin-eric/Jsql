package cc.jfire.jsql.analyse.token;

import java.util.HashMap;
import java.util.Map;

public enum KeyWord
{
    SELECT, //
    DELETE, //
    INSERT, //
    UPDATE, //
    CREATE, //
    ALTER, //
    DROP, //
    TRUNCATE, //
    REPLACE, //
    DECLARE, //
    GRANT, //
    REVOKE, //
    AS, //
    DISTINCT, //
    MAX, //
    MIN, //
    SUM, //
    AVG, //
    COUNT, //
    FROM, //
    WHERE, //
    ORDER, //
    ASC, //
    DESC, //
    GROUP, //
    BY, //
    HAVING, //
    INTO, //
    VALUES, //
    COLUMN, //
    TABLE, //
    TABLESPACE, //
    SET, //
    PRIMARY, //
    KEY, //
    INDEX, //
    CONSTRAINT, //
    CHECK, //
    UNIQUE, //
    FOREIGN, //
    REFERENCES, //
    INNER, //
    LEFT, //
    RIGHT, //
    FULL, //
    OUTER, //
    CROSS, //
    JOIN, //
    STRAIGHT_JOIN, //
    APPLY, //
    ON, //
    IS, //
    IN, //
    BETWEEN, //
    LIKE, //
    AND, //
    OR, //
    XOR, //
    NULL, //
    NOT, //
    FOR, //
    ALL, //
    UNION, //
    CAST, //
    USE, //
    USING, //
    TO, //
    CASE, //
    WHEN, //
    THEN, //
    ELSE, //
    END, //
    EXISTS, //
    NEW, //
    ESCAPE, //
    INTERVAL, //
    LOCK, //
    SOME, //
    ANY, //
    WHILE, //
    DO, //
    LEAVE, //
    ITERATE, //
    REPEAT, //
    UNTIL, //
    OPEN, //
    CLOSE, //
    OUT, //
    INOUT, //
    OVER, //
    FETCH, //
    WITH, //
    CURSOR, //
    ADVISE, //
    SIBLINGS, //
    LOOP, //
    ENABLE, //
    DISABLE, //
    EXPLAIN, //
    SCHEMA, //
    DATABASE, //
    VIEW, //
    SEQUENCE, //
    TRIGGER, //
    PROCEDURE, //
    FUNCTION, //
    DEFAULT, //
    EXCEPT, //
    INTERSECT, //
    MINUS, //
    PASSWORD, //
    CONNECT_BY_ROOT, //
    IF, //
    GLOBAL, //
    LOCAL, //
    MATCHED, //
    MERGE, //
    TEMPORARY;
    private static final Map<String, KeyWord> keywords = new HashMap<String, KeyWord>(128);
    static
    {
        for (KeyWord each : KeyWord.values())
        {
            keywords.put(each.name().toLowerCase(), each);
        }
    }
    public static boolean isKeyWord(String literals)
    {
        return keywords.containsKey(literals.toLowerCase());
    }

    public static KeyWord getKeyWord(String literals)
    {
        return keywords.get(literals.toLowerCase());
    }
}
