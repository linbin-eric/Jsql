# JSQL SqlSession 完整文档

## 概述

SqlSession 是 JSQL 框架的核心接口，代表一个数据库连接会话，提供各种 DAO 操作入口。它封装了数据库连接管理、事务控制、SQL 执行等核心功能，为开发者提供简洁而强大的数据库操作 API。

## 核心架构

### 接口继承关系
```
ConnectionOp (自动关闭接口)
    ↓
SqlSession (核心接口)
    ↓
SqlSessionImpl (实现类)
```

### 主要组件
- **SqlSession**: 核心接口，定义所有数据库操作方法
- **SqlSessionImpl**: 具体实现类，处理实际的数据库操作
- **SessionFactory**: 工厂接口，负责创建 SqlSession 实例
- **SessionFactoryImpl**: 工厂实现类，管理数据源和 SQL 执行器

## 事务管理

### 事务控制方法

#### beginTransAction()
启动事务，将数据库连接设置为非自动提交模式。
```java
sqlSession.beginTransAction();
```

#### commit()
提交事务，并将连接恢复为自动提交模式。
```java
sqlSession.commit();
```

#### flush()
提交事务到数据库，但不改变当前连接的提交模式。
```java
sqlSession.flush();
```

#### rollback()
回滚事务，并将连接恢复为自动提交模式。
```java
sqlSession.rollback();
```

### 事务使用示例
```java
try (SqlSession session = sessionFactory.openSession()) {
    session.beginTransAction();
    
    // 执行数据库操作
    session.save(user);
    session.update(anotherUser);
    
    session.commit();
} catch (Exception e) {
    session.rollback();
    throw e;
}
```

## 实体操作

### save(T entity)
智能保存方法，根据实体主键状态决定插入或更新操作。

**行为规则**:
1. **无主键**: 按全量插入处理
2. **有主键且主键有值**: 按全量插入处理  
3. **有主键但主键为空**:
   - 有 `@PkGenerator` 注解: 使用生成器生成主键后插入
   - 有 `@AutoIncrement` 注解: 插入除主键外的所有属性，返回自动生成的主键
   - 有 `@Sequence` 注解: 插入除主键外的所有属性，返回序列生成的主键
   - 其他情况: 抛出异常

```java
User user = new User();
user.setName("张三");
user.setAge(25);
session.save(user); // 插入操作，自动生成主键

user.setAge(26);
session.save(user); // 更新操作，因为已有主键
```

### insert(T entity)
直接执行插入操作，忽略主键状态。

```java
User user = new User();
user.setName("李四");
user.setAge(30);
session.insert(user);
```

### update(T entity)
直接执行更新操作，需要实体包含有效主键。

```java
User user = new User();
user.setId(1L);
user.setName("王五");
user.setAge(35);
session.update(user);
```

### batchInsert(Collection<T> collection, int batchSize)
批量插入操作，支持分批处理大数据量。

```java
List<User> users = Arrays.asList(user1, user2, user3, user4, user5);
session.batchInsert(users, 2); // 每批插入2条记录
```

## 查询操作

### findOne(QueryModel model)
查询单条记录，返回实体对象。

```java
User user = session.findOne(
    Model.selectAll()
         .from(User.class)
         .where(Param.eq(User::getId, 1))
);
```

### findList(QueryModel model)
查询多条记录，返回实体列表。支持分页查询（当最后一个参数是 Page 对象时）。

```java
List<User> users = session.findList(
    Model.selectAll()
         .from(User.class)
         .where(Param.eq(User::getAge, 25))
         .orderBy(User::getName, true)
);
```

### findListByPage(QueryModel model)
分页查询，返回包含数据和总数的 Page 对象。

```java
Page page = new Page();
page.setOffset(0);
page.setSize(10);
page.setFetchSum(true);

Page result = session.findListByPage(
    Model.selectAll()
         .from(User.class)
         .where(Param.eq(User::getStatus, "active"))
         .page(page)
);

List<User> users = result.getResult();
int total = result.getTotal();
```

### count(Model model)
统计记录数量。

```java
int count = session.count(
    Model.selectCount()
         .from(User.class)
         .where(Param.eq(User::getStatus, "active"))
);
```

## 原生 SQL 操作

### execute(String sql, List<Object> params)
执行原生 SQL 语句，返回影响的行数。

```java
int rows = session.execute(
    "UPDATE user SET status = ? WHERE age > ?",
    Arrays.asList("inactive", 60)
);
```

### insertReturnPk(String sql, List<Object> params, TableEntityInfo.ColumnInfo pkInfo)
执行插入语句并返回生成的主键值。

```java
String pk = session.insertReturnPk(
    "INSERT INTO user (name, age) VALUES (?, ?)",
    Arrays.asList("赵六", 28),
    TableEntityInfo.parse(User.class).getPkInfo()
);
```

### query(String sql, ResultSetTransfer transfer, List<Object> params)
执行查询语句，返回单个结果。

```java
String name = session.query(
    "SELECT name FROM user WHERE id = ?",
    StringTransfer.INSTANCE,
    Arrays.asList(1)
);
```

### queryList(String sql, ResultSetTransfer transfer, List<Object> params)
执行查询语句，返回多个结果。

```java
List<String> names = session.queryList(
    "SELECT name FROM user WHERE age > ?",
    StringTransfer.INSTANCE,
    Arrays.asList(25)
);
```

## Mapper 接口

### getMapper(Class<T> mapperClass)
获取 Mapper 接口的代理实现，支持自定义 Repository 操作。

```java
@Mapper
public interface UserRepository extends Repository<User> {
    // 自定义方法
}

UserRepository repository = session.getMapper(UserRepository.class);
List<User> users = repository.findList(Param.eq(User::getAge, 25));
```

## 数据转换器

### 内置转换器
SqlSession 内置了丰富的数据类型转换器，自动处理 Java 类型与数据库类型的映射：

- **基本类型**: Integer, Long, Float, Double, Boolean, Short, String
- **时间类型**: Date, java.sql.Date, Timestamp, Time, Calendar, LocalDate, LocalDateTime
- **大对象**: BigDecimal, byte[] (BLOB), Clob
- **枚举**: EnumNameTransfer, EnumOrdinalTransfer

### 自定义转换器
通过 `@CustomTransfer` 注解指定自定义转换器：

```java
@CustomTransfer(MyCustomTransfer.class)
public class MyEntity {
    // 字段定义
}
```

## 高级特性

### 连接管理
- **自动关闭**: SqlSession 实现 AutoCloseable 接口，支持 try-with-resources 语法
- **连接获取**: 通过 `getConnection()` 方法获取底层 JDBC 连接
- **状态检查**: 提供 `checkIfClosed()` 方法检查会话状态

### 异常处理
- **统一异常**: 所有 SQLException 被转换为运行时异常
- **事务异常**: 提供具体的事务状态异常信息
- **结果异常**: `NotSingleResultException` 处理非单结果情况

### 性能优化
- **转换器缓存**: ResultSetTransfer 实例缓存，避免重复创建
- **SQL 缓存**: 编译后的 SQL 语句缓存机制
- **批量操作**: 支持高效的批量插入操作

## 使用示例

### 完整 CRUD 示例
```java
public class UserService {
    private final SessionFactory sessionFactory;
    
    public User createUser(String name, int age) {
        try (SqlSession session = sessionFactory.openSession()) {
            User user = new User();
            user.setName(name);
            user.setAge(age);
            session.save(user);
            return user;
        }
    }
    
    public User getUserById(Long id) {
        try (SqlSession session = sessionFactory.openSession()) {
            return session.findOne(
                Model.selectAll()
                     .from(User.class)
                     .where(Param.eq(User::getId, id))
            );
        }
    }
    
    public List<User> getUsersByAge(int minAge) {
        try (SqlSession session = sessionFactory.openSession()) {
            return session.findList(
                Model.selectAll()
                     .from(User.class)
                     .where(Param.ge(User::getAge, minAge))
                     .orderBy(User::getName, true)
            );
        }
    }
    
    public void updateUserAge(Long id, int newAge) {
        try (SqlSession session = sessionFactory.openSession()) {
            session.beginTransAction();
            try {
                User user = session.findOne(
                    Model.selectAll()
                         .from(User.class)
                         .where(Param.eq(User::getId, id))
                );
                if (user != null) {
                    user.setAge(newAge);
                    session.update(user);
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw e;
            }
        }
    }
    
    public void deleteUser(Long id) {
        try (SqlSession session = sessionFactory.openSession()) {
            session.execute(
                Model.deleteFrom(User.class)
                     .where(Param.eq(User::getId, id))
            );
        }
    }
}
```

### 复杂查询示例
```java
public class ComplexQueryExample {
    
    public Page<UserDTO> searchUsers(String keyword, int pageNum, int pageSize) {
        try (SqlSession session = sessionFactory.openSession()) {
            Page page = new Page();
            page.setOffset((pageNum - 1) * pageSize);
            page.setSize(pageSize);
            page.setFetchSum(true);
            
            List<UserDTO> users = session.findList(
                Model.select(User::getId, User::getName, User::getAge)
                     .selectAs(User::getEmail, "emailAddress")
                     .from(User.class)
                     .where(
                         Param.like(User::getName, "%" + keyword + "%")
                              .or(Param.like(User::getEmail, "%" + keyword + "%"))
                     )
                     .and(Param.eq(User::getStatus, "active"))
                     .and(Param.between(User::getAge, 18, 65))
                     .orderBy(User::getCreateTime, false)
                     .returnType(UserDTO.class)
                     .page(page)
            );
            
            return page;
        }
    }
    
    public List<UserStats> getUserStatistics() {
        try (SqlSession session = sessionFactory.openSession()) {
            return session.findList(
                Model.selectWithFunction(User::getAge, "avg", "avgAge")
                     .selectWithFunction(User::getId, "count", "userCount")
                     .select(User::getDepartment)
                     .from(User.class)
                     .groupBy(User::getDepartment)
                     .having(Param.gt(User::getId, 0))
                     .returnType(UserStats.class)
            );
        }
    }
}
```

## @Sql 注解高级用法

### 概述
`@Sql` 注解是 JSQL 框架提供的强大功能，允许在 Mapper 接口方法上直接编写原生 SQL 语句，支持多种参数替换模式和动态 SQL 构建。

### 基本语法
```java
@Mapper
public interface UserMapper {
    @Sql(sql = "SELECT * FROM user WHERE id = ${id}", paramNames = "id")
    User findById(int id);
}
```

### 参数替换模式

#### 1. #{} 字符串占位符
`#{}` 将参数的文字内容直接替换到 SQL 中，适用于字符串拼接和动态 SQL 构建：
```java
@Sql(sql = "SELECT * FROM user WHERE name LIKE #{name}", paramNames = "name")
List<User> searchByName(String name);

@Sql(sql = "SELECT COUNT(*) FROM #{table}", paramNames = "table")
int countTable(String table);

@Sql(sql = "SELECT * FROM user WHERE <% if(name != null) { %> name LIKE #{name} <% } %>", paramNames = "name")
List<User> findDynamic(String name);
```

#### 2. ${} 预编译参数
`${}` 使用预编译语句，参数值通过 JDBC PreparedStatement 设置，防止 SQL 注入：
```java
@Sql(sql = "SELECT * FROM user WHERE id = ${id}", paramNames = "id")
User findByIdSafe(int id);

@Sql(sql = "INSERT INTO user (name, age) VALUES (${name}, ${age})", paramNames = "name,age")
int insertUser(String name, int age);

@Sql(sql = "SELECT * FROM user WHERE age > ${minAge}", paramNames = "minAge")
List<User> findByMinAge(int minAge);
```

#### 3. ~{} IN 子句展开
`~{}` 自动展开数组或集合为 IN 子句：
```java
@Sql(sql = "SELECT * FROM user WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIds(int[] ids);

@Sql(sql = "SELECT * FROM user WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIdList(List<Integer> ids);

@Sql(sql = "SELECT * FROM user WHERE status IN ~{statuses}", paramNames = "statuses")
List<User> findByStatuses(String[] statuses);
```

#### 4. 参数替换区别
- **`#{}`**: 字符串替换，参数值直接嵌入 SQL 字符串中
- **`${}`**: 预编译参数，通过 PreparedStatement 设置参数值
- **`~{}`**: 特殊处理，将数组/集合展开为逗号分隔的列表

### 动态 SQL 构建

#### 条件判断
```java
@Sql(sql = """
    SELECT * FROM user 
    <% if(name != null) { %>
        WHERE name LIKE #{name}
    <% } else { %>
        WHERE id = ${id}
    <% } %>
    """, paramNames = "name,id")
List<User> findDynamic(String name, int id);
```

#### 复杂条件分支
```java
@Sql(sql = """
    SELECT * FROM user WHERE 
    <% if(id==1) { %> id=1 
    <% } else if(id==2) { %> id=2 
    <% } else { %> id=3 <% } %>
    """, paramNames = "id")
User findByCondition(int id);
```

### 枚举处理

#### 序数值
```java
@Sql(sql = "SELECT * FROM user WHERE state = ${s.ordinal()}", paramNames = "s")
User findByState(User.State s);
```

#### 名称值
```java
@Sql(sql = "SELECT * FROM user WHERE stringEnum = #{v.name()}", paramNames = "v")
User findByStringEnum(User.StringEnum v);
```

### 别名和连接查询

#### 表别名
```java
@Sql(sql = "SELECT u.* FROM user AS u WHERE u.name = ${name}", paramNames = "name")
User findWithAlias(String name);

@Sql(sql = "SELECT u.age FROM user AS u WHERE u.name = ${name}", paramNames = "name")
int findAgeByName(String name);

@Sql(sql = "SELECT u.age AS a FROM user AS u WHERE u.name = ${name}", paramNames = "name")
User findWithColumnAlias(String name);
```

#### 连接查询
```java
@Sql(sql = """
    SELECT u.* FROM user u 
    INNER JOIN department d ON u.dept_id = d.id 
    WHERE d.name = ${deptName}
    """, paramNames = "deptName")
List<User> findByDepartment(String deptName);
```

### 静态常量访问
```java
@Sql(sql = "SELECT * FROM user WHERE name = ${@(com.example.User).CUSTOM_NAME}", paramNames = "")
User findByStaticConstant();
```

### 分页支持
```java
@Sql(sql = "SELECT * FROM user WHERE name LIKE ${'%' + name + '%'}", paramNames = "name")
List<User> findWithPage(String name, Page page);
```

### 自定义结果转换
```java
@CustomTransfer(EnumOrdinalTransfer.class)
@Sql(sql = "SELECT state FROM user WHERE name = ${name}", paramNames = "name")
User.State findState(String name);

@CustomTransfer(EnumOrdinalTransfer.class)
@Sql(sql = "SELECT state FROM user WHERE name LIKE ${'%' + name + '%'}", paramNames = "name")
List<User.State> findStateList(String name);
```

### 返回类型映射

#### 基本类型
```java
@Sql(sql = "SELECT COUNT(*) FROM user", paramNames = "")
int countUsers();

@Sql(sql = "SELECT age FROM user WHERE id = 1", paramNames = "")
int findAge();

@Sql(sql = "SELECT b FROM user WHERE id = 1", paramNames = "")
boolean findBoolean();

@Sql(sql = "SELECT time FROM user WHERE id = 1", paramNames = "")
Time findTime();

@Sql(sql = "SELECT timestamp FROM user WHERE id = 1", paramNames = "")
Timestamp findTimestamp();

@Sql(sql = "SELECT date FROM user WHERE id = 1", paramNames = "")
Date findDate();

@Sql(sql = "SELECT sqlDate FROM user WHERE id = 1", paramNames = "")
java.sql.Date findSqlDate();

@Sql(sql = "SELECT F11 FROM user WHERE id = 1", paramNames = "")
float findFloat();
```

#### 对象类型
```java
@Sql(sql = "SELECT * FROM user WHERE id = ${id}", paramNames = "id")
User findUser(int id);
```

#### 集合类型
```java
@Sql(sql = "SELECT * FROM user WHERE age > ${minAge}", paramNames = "minAge")
List<User> findUsers(int minAge);

@Sql(sql = "SELECT name FROM user WHERE status = ${status}", paramNames = "status")
List<String> findNamesByStatus(String status);
```

### 高级特性

#### 方法默认实现
```java
@Mapper
public interface UserMapper {
    @Sql(sql = "SELECT COUNT(*) FROM user", paramNames = "")
    int count();
    
    default boolean hasUsers() {
        return count() > 0;
    }
}
```

### 使用示例

#### 完整 Mapper 接口示例
```java
@Mapper({User.class, Department.class})
public interface UserRepository {
    
    // 基本查询
    @Sql(sql = "SELECT * FROM user WHERE id = ${id}", paramNames = "id")
    User findById(int id);
    
    // 条件查询
    @Sql(sql = """
        SELECT * FROM user 
        WHERE <% if(name != null) { %> name LIKE #{name} <% } %>
        AND <% if(minAge != null) { %> age >= ${minAge} <% } %>
        ORDER BY name
        """, paramNames = "name,minAge")
    List<User> searchUsers(String name, Integer minAge);
    
    // IN 查询
    @Sql(sql = "SELECT * FROM user WHERE id IN ~{ids}", paramNames = "ids")
    List<User> findByIds(int[] ids);
    
    // 统计查询
    @Sql(sql = "SELECT COUNT(*) FROM user WHERE status = ${status}", paramNames = "status")
    int countByStatus(String status);
    
    // 分页查询
    @Sql(sql = "SELECT * FROM user WHERE age >= ${minAge}", paramNames = "minAge")
    List<User> findByAgeWithPage(int minAge, Page page);
    
    // 枚举查询
    @Sql(sql = "SELECT * FROM user WHERE state = ${state.ordinal()}", paramNames = "state")
    List<User> findByState(User.State state);
    
    // 复杂连接查询
    @Sql(sql = """
        SELECT u.* FROM user u
        INNER JOIN department d ON u.dept_id = d.id
        WHERE d.name = ${deptName}
        AND u.status = 'active'
        """, paramNames = "deptName")
    List<User> findActiveByDepartment(String deptName);
}
```

### 注意事项

1. **SQL 注入防护**: 使用 `#{}` 预编译参数防止 SQL 注入
2. **参数顺序**: `paramNames` 中的参数名必须与方法参数顺序一致
3. **类型匹配**: 确保 SQL 参数类型与方法参数类型匹配
4. **枚举处理**: 根据数据库字段类型选择合适的枚举访问方式
5. **动态 SQL**: 使用条件判断构建灵活的查询语句
6. **性能考虑**: 避免在循环中频繁调用 SQL 方法

## 最佳实践

### 1. 资源管理
始终使用 try-with-resources 语句确保 SqlSession 正确关闭：
```java
try (SqlSession session = sessionFactory.openSession()) {
    // 数据库操作
}
```

### 2. 事务边界
明确事务边界，避免长时间持有事务：
```java
try (SqlSession session = sessionFactory.openSession()) {
    session.beginTransAction();
    try {
        // 事务操作
        session.commit();
    } catch (Exception e) {
        session.rollback();
        throw e;
    }
}
```

### 3. 查询优化
- 使用具体的字段选择而非 `selectAll()`
- 合理使用索引字段进行查询
- 避免 N+1 查询问题

### 4. 批量操作
对于大量数据操作，使用批量方法：
```java
session.batchInsert(largeUserList, 100); // 每批100条
```

### 5. 类型安全
充分利用泛型和类型安全的查询构建器，避免 SQL 注入风险。

### 6. @Sql 注解最佳实践
- **理解参数替换机制**: `#{}` 用于字符串替换，`${}` 用于预编译参数
- **合理使用动态 SQL**: 条件判断使查询更灵活
- **枚举处理一致性**: 统一使用序数或名称方式处理枚举
- **参数命名清晰**: 使用有意义的参数名提高可读性
- **返回类型明确**: 确保 SQL 结果与返回类型匹配
- **SQL 注入防护**: 用户输入应使用 `${}` 预编译参数，避免直接字符串拼接

## 总结

SqlSession 提供了完整而强大的数据库操作能力，通过统一的 API 封装了复杂的 JDBC 操作，支持事务管理、多种查询模式、批量操作等高级特性。结合 Model 构建器和 @Sql 注解系统，开发者可以以类型安全、简洁的方式完成各种数据库操作任务。@Sql 注解特别适用于需要精细控制 SQL 语句的场景，提供了灵活的参数替换和动态 SQL 构建能力。