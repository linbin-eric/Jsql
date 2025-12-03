package cc.jfire.jsql.annotation;

import cc.jfire.jsql.mapper.Mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SQL语句注解，用于标记Mapper接口中的方法。
 * 使用该注解表明方法会执行对应的SQL语句。
 * 
 * <p>该注解是JSQL框架中实现自定义SQL查询的核心机制。
 * 通过在Mapper接口的方法上添加此注解，可以定义任意复杂的SQL语句，包括动态SQL。</p>
 * 
 * <p>根据方法的返回类型，SQL执行结果会有不同的处理方式：</p>
 * <ul>
 *   <li><b>基本类型返回值</b>：如果方法返回基本类型（如int, String等），
 *       则查询结果必须是单行单列的数据。</li>
 *   <li><b>对象类型返回值</b>：如果方法返回一个对象（非基本类型），
 *       则查询结果应该是单行数据，JSQL会自动将该行数据转换为对象实例。</li>
 *   <li><b>List&lt;T&gt;类型返回值</b>：如果方法返回List&lt;T&gt;，
 *       则会根据T的类型进一步判断：
 *       <ul>
 *         <li>如果T是基本类型，则查询结果应该是多行单列数据。</li>
 *         <li>如果T是对象类型，则查询结果应该是多行数据，JSQL会自动将每行数据转换为对象实例。</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <p><b>SQL语句中的占位符语法：</b></p>
 * <ul>
 *   <li><b>{@code $表达式}</b>：动态表达式占位符，支持Java表达式和字符串拼接。
 *       <ul>
 *         <li>基本用法：{@code SELECT * FROM user WHERE name LIKE ${'%' + name + '%'}}</li>
 *         <li>方法调用：{@code SELECT * FROM user WHERE state = ${state.ordinal()}}</li>
 *         <li>静态常量：{@code SELECT * FROM user WHERE name = ${@(com.example.User).STATIC_NAME}}</li>
 *       </ul>
 *       <p>不管是基本用法、还是方法调用，都是对所注解的方法的参数的调用，
 *       {@code ${}}范围内变量名，都来自方法的参数名称。</p>
 *   </li>
 *   <li><b>{@code #{参数名}}</b>：字符串占位符，可以用来放入任意的字符串。
 *       例如：{@code SELECT COUNT(*) FROM #{tableName}}</li>
 *   <li><b>{@code ~{参数名}}</b>：IN子句占位符，用于处理集合类型的参数。
 *       支持数组、集合等多种类型。
 *       例如：{@code SELECT COUNT(*) FROM user WHERE id IN ~{ids}}</li>
 *   <li><b>{@code <% %>}</b>：动态SQL块，用于条件判断和循环等控制结构。
 *       <ul>
 *         <li>if语句：{@code <% if(name != null) { %> WHERE name = ${name} <% } %> }</li>
 *         <li>if-else语句：{@code <% if(id == 1) { %> id=1 <% } else if(id == 2) { %> id=2 <% } else { %> id=3 <% } %> }</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <p>使用示例:</p>
 * <pre>
 * &#64;Mapper
 * public interface UserMapper {
 *     // 查询单个用户
 *     &#64;Sql(sql = "SELECT * FROM user WHERE id = ${id}", paramNames = "id")
 *     User findUserById(int id);
 *     
 *     // 查询用户列表
 *     &#64;Sql(sql = "SELECT * FROM user WHERE name LIKE ${name}", paramNames = "name")
 *     List&lt;User&gt; findUsersByName(String name);
 *     
 *     // 统计用户数量
 *     &#64;Sql(sql = "SELECT COUNT(*) FROM user", paramNames = "")
 *     int countUsers();
 *     
 *     // 更新用户信息
 *     &#64;Sql(sql = "UPDATE user SET name = ${name} WHERE id = ${id}", paramNames = "name,id")
 *     int updateUserName(String name, int id);
 *     
 *     // 动态SQL查询 - 根据条件查询用户
 *     &#64;Sql(sql = """
 *         SELECT * FROM user 
 *         &lt;% if(name != null) { %&gt; 
 *         WHERE name LIKE ${'%' + name + '%'} 
 *         &lt;% } else { %&gt; 
 *         WHERE id = ${id} 
 *         &lt;% } %&gt;""", paramNames = "name,id")
 *     List&lt;User&gt; findUsers(String name, int id);
 *     
 *     // IN子句查询
 *     &#64;Sql(sql = "SELECT COUNT(*) FROM user WHERE id IN ~{ids}", paramNames = "ids")
 *     int countUsersByIds(List&lt;Integer&gt; ids);
 *     
 *     // 查询指定表的记录数
 *     &#64;Sql(sql = "SELECT COUNT(*) FROM #{tableName}", paramNames = "tableName")
 *     int countByTable(String tableName);
 *     
 *     // 使用枚举值查询
 *     &#64;Sql(sql = "SELECT * FROM user WHERE state = ${state.ordinal()}", paramNames = "state")
 *     User findByState(User.State state);
 * }
 * </pre>
 *
 * @author 林斌（eric@jfire.cn）
 * @see Mapper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sql
{
    /**
     * SQL语句。
     * 可以使用多种占位符语法来引用方法参数和构建动态SQL。
     * 
     * <p>支持的占位符语法包括：</p>
     * <ul>
     *   <li>{@code $表达式} - 动态表达式占位符</li>
     *   <li>{@code #{参数名}} - 字符串占位符</li>
     *   <li>{@code ~{参数名}} - IN子句占位符</li>
     *   <li>{@code <% %>} - 动态SQL块</li>
     * </ul>
     *
     * @return SQL语句字符串
     * @see #paramNames()
     */
    String sql();

    /**
     * 方法的形参名称，按顺序用逗号分隔。
     * 这些名称将用于匹配SQL语句中的占位符。
     * 例如：如果方法签名是{@code findUser(String name, int age)}，
     * 则paramNames应该是{@code "name,age"}。
     *
     * @return 参数名称列表，用逗号分隔
     */
    String paramNames();
}
