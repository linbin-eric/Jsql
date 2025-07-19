# JSql 框架

JSql 是一个轻量级的Java SQL映射框架，通过基于接口的SQL绑定提供透明的数据库操作。它允许开发者将接口方法与SQL语句绑定，实现自动的结果对象转换，并支持动态SQL功能。

## 主要特性

- **简洁的API设计**：提供直观的SqlSession API进行数据库操作
- **基于接口的Mapper**：通过@Sql注解绑定SQL语句到接口方法
- **流式查询构建**：通过Model提供类型安全的Lambda表达式查询
- **动态SQL支持**：支持条件块、参数绑定、动态表名/列名
- **实体映射**：通过注解自动映射Java对象到数据库表
- **类名和属性名映射**：SQL语句中可直接使用类名和属性名，自动转换为对应的表名和字段名
- **智能Mapper绑定**：@Mapper注解指定映射到具体表的类，简化表关联操作
- **多数据库支持**：支持PostgreSQL、Oracle、DuckDB等多种数据库
- **事务管理**：支持声明式事务控制

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.jfirer</groupId>
    <artifactId>jsql</artifactId>
    <version>最新版本</version>
</dependency>
```

### 2. 配置数据源

```java
// 创建数据源
HikariDataSource dataSource = new HikariDataSource();
dataSource.setJdbcUrl("jdbc:h2:mem:testdb");
dataSource.setUsername("sa");
dataSource.setPassword("");

// 创建SessionFactory配置
SessionFactoryConfig config = new SessionFactoryConfig();
config.setDataSource(dataSource);

// 构建SessionFactory
SessionFactory sessionFactory = config.build();
```

### 3. 定义实体类

```java
@TableDef("user")
public class User {
    @Pk
    @AutoIncrement
    private Integer id;
    
    @ColumnName("name2")
    private String name;
    
    private Integer age;
    
    // getters and setters...
}
```

### 4. 基本使用

```java
try (SqlSession session = sessionFactory.openSession()) {
    // 保存实体
    User user = new User();
    user.setName("张三");
    user.setAge(25);
    session.save(user);
    
    // 查询单个实体
    User found = session.findOne(Model.selectAll()
        .from(User.class)
        .where(Param.eq(User::getName, "张三")));
    
    // 查询列表
    List<User> users = session.findList(Model.selectAll()
        .from(User.class)
        .where(Param.gt(User::getAge, 18)));
    
    // 更新
    session.execute(Model.update(User.class)
        .set(User::getAge, 26)
        .where(Param.eq(User::getId, user.getId())));
    
    // 删除
    session.execute(Model.deleteFrom(User.class)
        .where(Param.eq(User::getId, user.getId())));
}
```

## SqlSession API详解

### 核心接口

SqlSession 继承自 ConnectionOp，提供了完整的数据库操作API：

```java
public interface SqlSession extends ConnectionOp {
    // Mapper获取
    <T> T getMapper(Class<T> mapperClass);
    
    // 查询操作
    <T> T findOne(QueryModel model);
    <T> List<T> findList(QueryModel model);
    Page findListByPage(QueryModel model);
    
    // 计数操作
    int count(Model model);
    
    // 执行操作
    int execute(Model model);
    
    // 实体操作
    <T> int save(T entity);
    <T> int update(T entity);
    <T> int insert(T entity);
    <T> void batchInsert(Collection<T> collection, int batchSize);
    
    // 原生SQL操作
    int execute(String sql, List<Object> params);
    String insertReturnPk(String sql, List<Object> params, TableEntityInfo.ColumnInfo pkInfo);
    <T> T query(String sql, ResultSetTransfer transfer, List<Object> params);
    <T> List<T> queryList(String sql, ResultSetTransfer transfer, List<Object> params);
}
```

### 连接管理

```java
public interface ConnectionOp extends AutoCloseable {
    void close();                    // 关闭session，释放数据库链接
    void beginTransAction();         // 启动事务
    void commit();                   // 提交事务
    void flush();                    // 刷新事务到数据库
    void rollback();                 // 回滚事务
    Connection getConnection();      // 获取底层数据库连接
}
```

### 查询操作

#### 查询单个实体
```java
User user = session.findOne(Model.selectAll()
    .from(User.class)
    .where(Param.eq(User::getId, 1)));
```

#### 查询列表
```java
List<User> users = session.findList(Model.selectAll()
    .from(User.class)
    .where(Param.gt(User::getAge, 18))
    .orderBy(User::getName, true));
```

#### 分页查询
```java
Page page = session.findListByPage(Model.selectAll()
    .from(User.class)
    .where(Param.like(User::getName, "%张%"))
    .page(new Page(0, 10)));
```

#### 计数查询
```java
int count = session.count(Model.selectCount(User.class)
    .where(Param.eq(User::getAge, 25)));
```

### 实体操作

#### 保存实体（智能保存）
```java
User user = new User();
user.setName("李四");
user.setAge(30);
session.save(user); // 自动判断插入还是更新
```

#### 插入实体
```java
User user = new User();
user.setName("王五");
user.setAge(28);
session.insert(user);
```

#### 更新实体
```java
User user = new User();
user.setId(1);
user.setName("赵六");
user.setAge(32);
session.update(user);
```

#### 批量插入
```java
List<User> users = Arrays.asList(
    new User("用户1", 25),
    new User("用户2", 30),
    new User("用户3", 35)
);
session.batchInsert(users, 100); // 批量大小为100
```

### 事务管理

```java
SqlSession session = sessionFactory.openSession();
try {
    session.beginTransAction();
    
    // 执行多个数据库操作
    session.save(user1);
    session.save(user2);
    session.execute(Model.update(User.class)...);
    
    session.commit();
} catch (Exception e) {
    session.rollback();
    throw e;
} finally {
    session.close();
}
```

## Model 流式查询API

### 基本查询

```java
// 查询所有列
Model.selectAll(User.class)

// 查询指定列
Model.select(User::getName, User::getAge)

// 查询带别名
Model.selectAlias(User::getName, "userName")

// 查询带函数
Model.selectWithFunction(User::getAge, "max", "maxAge")

// 计数查询
Model.selectCount(User.class)
```

### 条件构建

```java
// 等值条件
Param.eq(User::getName, "张三")

// 范围条件
Param.gt(User::getAge, 18)
Param.lt(User::getAge, 65)
Param.between(User::getAge, 18, 65)

// 模糊查询
Param.like(User::getName, "%张%")

// 空值判断
Param.isNull(User::getEmail)
Param.notNull(User::getEmail)

// 集合条件
Param.in(User::getId, Arrays.asList(1, 2, 3))

// 组合条件
Param.eq(User::getAge, 25).and(Param.like(User::getName, "%张%"))
Param.eq(User::getAge, 25).or(Param.eq(User::getAge, 30))
```

### 复杂查询

```java
// 连接查询
Model.selectAll()
    .fromAs(User.class, "u")
    .innerJoin(Order.class, "o")
    .on(Param.eq(User::getId, Order::getUserId))
    .where(Param.eq(User::getAge, 25))

// 分页查询
Model.selectAll(User.class)
    .where(Param.gt(User::getAge, 18))
    .orderBy(User::getName, true)
    .page(new Page(0, 10))

// 排序
Model.selectAll(User.class)
    .orderBy(User::getName, true)  // 升序
    .orderBy(User::getAge, false)  // 降序
```

### 更新操作

```java
// 更新指定字段
Model.update(User.class)
    .set(User::getName, "新姓名")
    .set(User::getAge, 30)
    .where(Param.eq(User::getId, 1))

// 更新实体
Model.update(user)
    .where(Param.eq(User::getId, user.getId()))
```

### 插入操作

```java
// 插入指定字段
Model.insert(User.class)
    .insert(User::getName, "新用户")
    .insert(User::getAge, 25)

// 插入实体
Model.insert(user)
```

### 删除操作

```java
// 删除记录
Model.deleteFrom(User.class)
    .where(Param.eq(User::getId, 1))
```

## Mapper 接口

### 定义Mapper接口

#### @Mapper注解详解

`@Mapper`注解的`value`属性用于指定该Mapper接口涉及到的实体类，这些类必须是映射到具体数据库表的实体类：

```java
// 单个实体类映射
@Mapper({User.class})
public interface UserMapper {
    // 该Mapper主要操作User表
}

// 多个实体类映射
@Mapper({User.class, Order.class, UserRole.class})
public interface UserOrderMapper {
    // 该Mapper涉及User、Order、UserRole三个表的操作
}
```

**@Mapper注解的作用：**
1. **表名解析**：框架根据value中的类自动解析SQL中的类名为对应的表名
2. **字段映射**：自动处理类属性名到数据库字段名的映射关系
3. **类型转换**：确保查询结果能正确转换为指定的实体类型
4. **SQL验证**：在初始化时验证SQL语句中引用的类名是否在value列表中

#### 基本用法示例

```java
@TableDef("user_info")
public class User {
    @Pk
    @ColumnName("user_id")
    private Integer id;
    
    @ColumnName("user_name")
    private String name;
    
    private Integer age;
}

@TableDef("order_table")
public class Order {
    @Pk
    @ColumnName("order_id")
    private Integer id;
    
    @ColumnName("user_id")
    private Integer userId;
    
    @ColumnName("order_amount")
    private BigDecimal amount;
}

// 单表操作Mapper
@Mapper({User.class})
public interface UserMapper {
    
    @Sql(sql = "SELECT * FROM User WHERE User.name = ${name}", paramNames = "name")
    User findByName(String name);
    // 实际SQL: SELECT * FROM user_info WHERE user_name = ?
    
    @Sql(sql = "SELECT * FROM User WHERE User.age > ${age}", paramNames = "age")
    List<User> findByAgeGreaterThan(int age);
    // 实际SQL: SELECT * FROM user_info WHERE age > ?
    
    @Sql(sql = "UPDATE User SET User.name = ${name} WHERE User.id = ${id}", 
         paramNames = "name,id")
    int updateNameById(String name, Integer id);
    // 实际SQL: UPDATE user_info SET user_name = ? WHERE user_id = ?
}

// 多表操作Mapper
@Mapper({User.class, Order.class})
public interface UserOrderMapper {
    
    @Sql(sql = """
        SELECT u.User.name, COUNT(o.Order.id) as orderCount
        FROM User u
        LEFT JOIN Order o ON u.User.id = o.Order.userId
        WHERE u.User.age > ${minAge}
        GROUP BY u.User.id, u.User.name
        """, paramNames = "minAge")
    List<UserOrderSummary> getUserOrderSummary(int minAge);
    // 实际SQL:
    // SELECT u.user_name, COUNT(o.order_id) as orderCount
    // FROM user_info u
    // LEFT JOIN order_table o ON u.user_id = o.user_id
    // WHERE u.age > ?
    // GROUP BY u.user_id, u.user_name
    
    @Sql(sql = """
        INSERT INTO Order (Order.userId, Order.amount) 
        VALUES (${userId}, ${amount})
        """, paramNames = "userId,amount")
    int createOrder(Integer userId, BigDecimal amount);
    // 实际SQL: INSERT INTO order_table (user_id, order_amount) VALUES (?, ?)
}
```

#### 复杂多表关联示例

```java
@TableDef("user_role")
public class UserRole {
    @ColumnName("user_id")
    private Integer userId;
    
    @ColumnName("role_name")
    private String roleName;
}

// 涉及三个表的复杂Mapper
@Mapper({User.class, Order.class, UserRole.class})
public interface ComplexMapper {
    
    @Sql(sql = """
        SELECT 
            u.User.name,
            ur.UserRole.roleName,
            COUNT(o.Order.id) as orderCount,
            SUM(o.Order.amount) as totalAmount
        FROM User u
        INNER JOIN UserRole ur ON u.User.id = ur.UserRole.userId
        LEFT JOIN Order o ON u.User.id = o.Order.userId
        WHERE ur.UserRole.roleName = ${role}
            AND u.User.age BETWEEN ${minAge} AND ${maxAge}
        GROUP BY u.User.id, u.User.name, ur.UserRole.roleName
        HAVING COUNT(o.Order.id) > ${minOrderCount}
        ORDER BY totalAmount DESC
        """, paramNames = "role,minAge,maxAge,minOrderCount")
    List<UserRoleOrderSummary> getUserRoleOrderSummary(
        String role, 
        Integer minAge, 
        Integer maxAge, 
        Integer minOrderCount
    );
    // 实际SQL:
    // SELECT 
    //     u.user_name,
    //     ur.role_name,
    //     COUNT(o.order_id) as orderCount,
    //     SUM(o.order_amount) as totalAmount
    // FROM user_info u
    // INNER JOIN user_role ur ON u.user_id = ur.user_id
    // LEFT JOIN order_table o ON u.user_id = o.user_id
    // WHERE ur.role_name = ?
    //     AND u.age BETWEEN ? AND ?
    // GROUP BY u.user_id, u.user_name, ur.role_name
    // HAVING COUNT(o.order_id) > ?
    // ORDER BY totalAmount DESC
}
```

#### 注意事项

1. **必须声明所有涉及的实体类**：SQL中使用的所有类名都必须在@Mapper的value中声明
2. **实体类必须有表映射**：value中的类必须使用@TableDef注解或符合默认命名规则
3. **类名大小写敏感**：SQL中的类名必须与实际类名保持一致
4. **性能优化**：框架在初始化时会预处理@Mapper中声明的所有类的映射关系

#### 错误示例

```java
// ❌ 错误：UserRole类未在@Mapper中声明，但SQL中使用了
@Mapper({User.class, Order.class})
public interface BadMapper {
    @Sql(sql = "SELECT * FROM UserRole WHERE UserRole.userId = ${userId}", paramNames = "userId")
    List<UserRole> findUserRoles(Integer userId);
    // 这会在运行时报错，因为UserRole未在@Mapper中声明
}

// ✅ 正确：所有使用的类都在@Mapper中声明
@Mapper({User.class, Order.class, UserRole.class})
public interface GoodMapper {
    @Sql(sql = "SELECT * FROM UserRole WHERE UserRole.userId = ${userId}", paramNames = "userId")
    List<UserRole> findUserRoles(Integer userId);
}
```

### 使用Mapper

```java
try (SqlSession session = sessionFactory.openSession()) {
    UserMapper mapper = session.getMapper(UserMapper.class);
    
    // 查询用户
    User user = mapper.findByName("张三");
    
    // 查询列表
    List<User> users = mapper.findByAgeGreaterThan(18);
    
    // 计数
    int count = mapper.countByAgeBetween(20, 40);
    
    // 更新
    int affected = mapper.updateNameById("新名字", 1);
}
```

### 动态SQL语法详解

JSql提供了丰富的动态SQL语法支持，允许开发者编写灵活的SQL语句：

#### 占位符语法

##### 1. 参数绑定占位符：`${}`
用于绑定参数值，支持表达式：
```java
// 基本参数绑定
@Sql(sql = "SELECT * FROM user WHERE name = ${name}", paramNames = "name")
User findByName(String name);

// 对象属性绑定
@Sql(sql = "UPDATE user SET name = ${user.name} WHERE id = ${user.id}", paramNames = "user")
int updateUser(User user);

// 表达式绑定
@Sql(sql = "SELECT * FROM user WHERE name LIKE ${'%' + name + '%'}", paramNames = "name")
List<User> findByNameLike(String name);

// 枚举值绑定
@Sql(sql = "SELECT * FROM user WHERE state = ${state.ordinal()}", paramNames = "state")
User findByState(User.State state);

@Sql(sql = "SELECT * FROM user WHERE type = ${type.name()}", paramNames = "type")
User findByType(User.Type type);
```

##### 2. 动态表名/列名：`#{}`
用于动态指定表名或列名，直接字符串替换：
```java
@Sql(sql = "SELECT COUNT(*) FROM #{tableName}", paramNames = "tableName")
int countByTable(String tableName);

@Sql(sql = "SELECT #{column} FROM user WHERE id = ${id}", paramNames = "column,id")
String getColumnValue(String column, Integer id);
```

##### 3. 集合展开：`~{}`
用于数组、列表等集合类型的IN子句：
```java
// 字符串数组
@Sql(sql = "SELECT * FROM user WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIds(String[] ids);

// 基本类型数组
@Sql(sql = "SELECT * FROM user WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIds(int[] ids);

// List集合
@Sql(sql = "SELECT * FROM user WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIds(List<Integer> ids);

// 逗号分隔的字符串
@Sql(sql = "SELECT * FROM user WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIds(String ids); // 如："1,2,3"
```

##### 4. 静态常量引用：`@()`
用于引用类的静态常量：
```java
@Sql(sql = "SELECT * FROM user WHERE name = ${@(com.example.User).DEFAULT_NAME}", paramNames = "")
User findByDefaultName();
```

#### 条件块语法

##### 1. 基本条件块：`<% %>`
```java
@Sql(sql = """
    SELECT * FROM user 
    <% if(name != null) { %>
        WHERE name = ${name}
    <% } %>
    """, paramNames = "name")
List<User> findByNameOptional(String name);
```

##### 2. 复杂条件块：
```java
@Sql(sql = """
    SELECT * FROM user 
    <% if(name != null) { %>
        WHERE name LIKE ${'%' + name + '%'}
    <% } %>
    <% if(age != null) { %>
        AND age = ${age}
    <% } %>
    """, paramNames = "name,age")
List<User> findByCondition(String name, Integer age);
```

##### 3. 条件分支：
```java
@Sql(sql = """
    SELECT * FROM user 
    <% if(type == 1) { %>
        WHERE status = 'active'
    <% } else if(type == 2) { %>
        WHERE status = 'inactive'
    <% } else { %>
        WHERE status = 'pending'
    <% } %>
    """, paramNames = "type")
List<User> findByType(int type);
```

#### 参数名称映射

在`@Sql`注解中，必须通过`paramNames`属性指定参数名称映射：

```java
// 单个参数
@Sql(sql = "SELECT * FROM user WHERE name = ${name}", paramNames = "name")
User findByName(String name);

// 多个参数（逗号分隔）
@Sql(sql = "SELECT * FROM user WHERE name = ${name} AND age = ${age}", paramNames = "name,age")
User findByNameAndAge(String name, Integer age);

// 对象参数
@Sql(sql = "INSERT INTO user (name, age) VALUES (${user.name}, ${user.age})", paramNames = "user")
int insertUser(User user);

// 无参数
@Sql(sql = "SELECT COUNT(*) FROM user", paramNames = "")
int countAll();
```

#### 类名和属性名自动映射

JSql支持在SQL语句中直接使用Java类名和属性名，框架会自动将其转换为对应的表名和字段名：

##### 1. 类名到表名的映射：
```java
// 使用类名，框架自动转换为表名
@Sql(sql = "SELECT * FROM User WHERE name = ${name}", paramNames = "name")
User findByName(String name);
// 实际执行的SQL: SELECT * FROM user WHERE name = ?

// 带@TableDef注解的类名映射
@TableDef("user_info")
public class UserInfo { ... }

@Sql(sql = "SELECT * FROM UserInfo WHERE age > ${age}", paramNames = "age")
List<UserInfo> findByAge(int age);
// 实际执行的SQL: SELECT * FROM user_info WHERE age > ?
```

##### 2. 属性名到字段名的映射：
```java
public class User {
    @ColumnName("user_name")
    private String name;
    
    private Integer age;  // 无注解时使用属性名
}

// 使用属性名，框架自动转换为字段名
@Sql(sql = "SELECT User.name, User.age FROM User WHERE User.name = ${name}", paramNames = "name")
User findByName(String name);
// 实际执行的SQL: SELECT user_name, age FROM user WHERE user_name = ?

// 复杂查询中的属性名映射
@Sql(sql = """
    SELECT u.User.name, u.User.age, o.Order.amount 
    FROM User u 
    INNER JOIN Order o ON u.User.id = o.Order.userId
    WHERE u.User.age > ${minAge}
    """, paramNames = "minAge")
List<UserOrderInfo> findUserOrders(int minAge);
// 实际执行的SQL: 
// SELECT u.user_name, u.age, o.amount 
// FROM user u 
// INNER JOIN order_table o ON u.id = o.user_id
// WHERE u.age > ?
```

##### 3. 混合使用类名和属性名：
```java
// 复合查询中同时使用类名和属性名
@Sql(sql = """
    UPDATE User 
    SET User.name = ${newName}, User.age = ${newAge}
    WHERE User.id IN (
        SELECT UserRole.userId FROM UserRole WHERE UserRole.role = ${role}
    )
    """, paramNames = "newName,newAge,role")
int updateUsersByRole(String newName, Integer newAge, String role);
// 实际执行的SQL:
// UPDATE user 
// SET user_name = ?, age = ?
// WHERE id IN (
//     SELECT user_id FROM user_role WHERE role = ?
// )
```

##### 4. 多表关联查询示例：
```java
@TableDef("user_info")
public class User {
    @ColumnName("user_id")
    private Integer id;
    @ColumnName("user_name") 
    private String name;
}

@TableDef("order_info")
public class Order {
    @ColumnName("order_id")
    private Integer id;
    @ColumnName("user_id")
    private Integer userId;
    @ColumnName("order_amount")
    private BigDecimal amount;
}

@Sql(sql = """
    SELECT u.User.name, COUNT(o.Order.id) as orderCount, SUM(o.Order.amount) as totalAmount
    FROM User u
    LEFT JOIN Order o ON u.User.id = o.Order.userId
    WHERE u.User.name LIKE ${namePattern}
    GROUP BY u.User.id, u.User.name
    HAVING COUNT(o.Order.id) > ${minOrderCount}
    """, paramNames = "namePattern,minOrderCount")
List<UserOrderSummary> getUserOrderSummary(String namePattern, int minOrderCount);
// 实际执行的SQL:
// SELECT u.user_name, COUNT(o.order_id) as orderCount, SUM(o.order_amount) as totalAmount
// FROM user_info u
// LEFT JOIN order_info o ON u.user_id = o.user_id
// WHERE u.user_name LIKE ?
// GROUP BY u.user_id, u.user_name
// HAVING COUNT(o.order_id) > ?
```

#### 动态SQL实例

##### 1. 动态查询条件：
```java
@Sql(sql = """
    SELECT * FROM user 
    WHERE 1=1
    <% if(name != null && !name.isEmpty()) { %>
        AND name LIKE ${'%' + name + '%'}
    <% } %>
    <% if(minAge != null) { %>
        AND age >= ${minAge}
    <% } %>
    <% if(maxAge != null) { %>
        AND age <= ${maxAge}
    <% } %>
    <% if(status != null) { %>
        AND status = ${status}
    <% } %>
    """, paramNames = "name,minAge,maxAge,status")
List<User> findByDynamicCondition(String name, Integer minAge, Integer maxAge, String status);
```

##### 2. 动态排序：
```java
@Sql(sql = """
    SELECT * FROM user 
    WHERE age > ${age}
    <% if(orderBy != null) { %>
        ORDER BY #{orderBy}
        <% if(desc) { %>
            DESC
        <% } else { %>
            ASC
        <% } %>
    <% } %>
    """, paramNames = "age,orderBy,desc")
List<User> findWithDynamicOrder(Integer age, String orderBy, Boolean desc);
```

##### 3. 动态更新：
```java
@Sql(sql = """
    UPDATE user SET 
    <% if(user.name != null) { %>
        name = ${user.name}
    <% } %>
    <% if(user.age != null) { %>
        <% if(user.name != null) { %>, <% } %>
        age = ${user.age}
    <% } %>
    <% if(user.email != null) { %>
        <% if(user.name != null || user.age != null) { %>, <% } %>
        email = ${user.email}
    <% } %>
    WHERE id = ${user.id}
    """, paramNames = "user")
int updateUserSelective(User user);
```

### 分页查询

```java
@Sql(sql = "SELECT * FROM user WHERE age > ${age}", paramNames = "age")
List<User> findByAge(int age, Page page);

// 使用
Page page = new Page(0, 10);
page.setFetchSum(true);
List<User> users = mapper.findByAge(25, page);
System.out.println("总记录数: " + page.getTotal());
```

## 实体映射注解

### 基本注解

```java
@TableDef("user_table")  // 指定表名
public class User {
    @Pk  // 主键
    @AutoIncrement  // 自增主键
    private Integer id;
    
    @ColumnName("user_name")  // 列名映射
    private String name;
    
    @SqlIgnore  // 忽略该字段
    private String tempField;
    
    @Sequence("user_id_seq")  // 序列生成器
    private Long sequenceId;
    
    // getters and setters...
}
```

### 数据类型支持

JSql支持以下Java数据类型的自动映射：

- 基本类型：`int`, `long`, `float`, `double`, `boolean`
- 包装类型：`Integer`, `Long`, `Float`, `Double`, `Boolean`
- 字符串：`String`
- 日期时间：`Date`, `LocalDate`, `LocalDateTime`, `Time`, `Timestamp`
- 枚举类型：支持自动转换

## 原生SQL支持

### 执行原生SQL

```java
// 执行更新/插入/删除
int affected = session.execute(
    "UPDATE user SET age = ? WHERE name = ?", 
    Arrays.asList(30, "张三")
);

// 查询单个结果
User user = session.query(
    "SELECT * FROM user WHERE id = ?",
    (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        return u;
    },
    Arrays.asList(1)
);

// 查询列表
List<User> users = session.queryList(
    "SELECT * FROM user WHERE age > ?",
    (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setAge(rs.getInt("age"));
        return u;
    },
    Arrays.asList(18)
);
```

### 获取自增主键

```java
TableEntityInfo.ColumnInfo pkInfo = ...; // 主键信息
String generatedKey = session.insertReturnPk(
    "INSERT INTO user (name, age) VALUES (?, ?)",
    Arrays.asList("新用户", 25),
    pkInfo
);
```

## 构建和测试

### Maven命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package

# 清理构建产物
mvn clean

# 安装到本地仓库
mvn install
```

### 运行特定测试

```bash
# 运行特定测试类
mvn test -Dtest=CURDTest

# 运行特定测试方法
mvn test -Dtest=CURDTest#testMethodName
```

## 数据库支持

JSql通过方言系统支持多种数据库：

- **H2**：内存数据库，适合测试
- **PostgreSQL**：支持特定分页语法
- **Oracle**：支持特定分页语法
- **DuckDB**：支持特定分页语法
- **MySQL**：标准SQL支持

### 配置数据库方言

```java
SessionFactoryConfig config = new SessionFactoryConfig();
config.setDialect(new PostgreSQLDialect()); // 使用PostgreSQL方言
```

## 最佳实践

### 1. 资源管理

```java
// 使用 try-with-resources 自动关闭资源
try (SqlSession session = sessionFactory.openSession()) {
    // 数据库操作
}
```

### 2. 事务管理

```java
// 显式事务管理
SqlSession session = sessionFactory.openSession();
try {
    session.beginTransAction();
    // 多个操作
    session.commit();
} catch (Exception e) {
    session.rollback();
    throw e;
} finally {
    session.close();
}
```

### 3. 查询优化

```java
// 只查询需要的字段
List<String> names = session.findList(
    Model.select(User::getName).from(User.class)
);

// 使用分页查询大量数据
Page page = new Page(0, 100);
List<User> users = session.findList(
    Model.selectAll(User.class).page(page)
);
```

### 4. 批量操作

```java
// 批量插入大量数据
List<User> users = ...;
session.batchInsert(users, 1000); // 每批1000条
```

## 常见问题

### Q: 如何处理复杂的查询条件？
A: 使用Model的流式API构建复杂条件，支持链式调用和条件组合。

### Q: 如何实现动态SQL？
A: 在Mapper接口中使用`<% %>`语法块实现条件SQL。

### Q: 如何处理枚举类型？
A: JSql自动支持枚举类型的序号和字符串转换。

### Q: 如何处理大量数据？
A: 使用分页查询或批量操作来处理大量数据。

## 版本兼容性

- **Java版本**：需要Java 17或更高版本
- **数据库**：支持JDBC 4.0+的数据库
- **依赖**：Maven构建，最小化外部依赖

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 创建Pull Request

## 许可证

本项目采用 [许可证名称] 许可证。详情请参阅 LICENSE 文件。