# Jsql框架核心API使用指南

Jsql是一个轻量级的Java SQL映射框架，提供了简洁而强大的数据库操作API。本文档介绍框架的三个核心API组件：SqlSession、Model和Mapper的使用方法和场景。

## 1. SqlSession - 数据库会话管理

SqlSession代表一个数据库连接会话，是执行数据库操作的主要入口点。

### 1.1 创建和使用SqlSession

```java
// 创建SessionFactory
SessionFactoryConfig config = new SessionFactoryConfig();
config.setDataSource(dataSource);
config.setDialect(new StandardDialect(DialectDict.MYSQL));
SessionFactory sessionFactory = config.build();

// 获取SqlSession
try (SqlSession session = sessionFactory.openSession()) {
    // 执行数据库操作
    // session会自动关闭
}
```

### 1.2 基本CRUD操作

#### 插入操作

```java
// 插入单个实体
User user = new User();
user.setName("张三");
user.setEmail("zhangsan@example.com");
session.insert(user);

// 批量插入
List<User> users = Arrays.asList(user1, user2, user3);
session.batchInsert(users, 100); // 批量大小为100
```

#### 查询操作

```java
// 使用Model查询
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(Param.eq(User::getName, "张三"));

User user = session.findOne(query);
List<User> users = session.findList(query);

// 分页查询
Page page = new Page();
page.setPageNum(1);
page.setPageSize(10);
query.page(page);
List<User> pagedUsers = session.findListByPage(query);
```

#### 更新操作

```java
// 更新实体
User user = session.findOne(query);
user.setEmail("newemail@example.com");
session.update(user);
```

#### 删除操作

```java
// 根据条件删除
DeleteModel deleteModel = Model.deleteFrom(User.class)
    .where(Param.eq(User::getStatus, "INACTIVE"));
session.execute(deleteModel);
```

### 1.3 原生SQL执行

```java
// 执行查询SQL
String sql = "SELECT * FROM users WHERE age > ?";
List<Object> params = Arrays.asList(18);
List<User> users = session.queryList(sql, new BeanTransfer<>(User.class), params);

// 执行更新SQL
String updateSql = "UPDATE users SET status = ? WHERE age < ?";
List<Object> updateParams = Arrays.asList("INACTIVE", 16);
int affectedRows = session.execute(updateSql, updateParams);
```

## 2. Model - 流式SQL构建器

Model提供了类型安全的流式API来构建SQL查询，支持lambda表达式。

### 2.1 查询Model

#### 基本查询

```java
// 查询所有字段
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class);

// 查询指定字段
QueryModel<User> query = Model.select(User::getName, User::getEmail)
    .from(User.class);

// 带别名查询
QueryModel<User> query = Model.selectAlias(User::getName, "username")
    .from(User.class);
```

#### 条件查询

```java
// 等值查询
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(Param.eq(User::getName, "张三"));

// 复合条件
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(Param.eq(User::getStatus, "ACTIVE")
           .and(Param.gt(User::getAge, 18))
           .or(Param.like(User::getName, "%管理员%")));

// IN查询
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(Param.in(User::getDepartment, Arrays.asList("IT", "HR", "Finance")));

// 范围查询
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(Param.between(User::getAge, 25, 65));
```

#### 关联查询

```java
// 内连接
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .innerJoin(Department.class)
    .on(Param.eq(User::getDepartmentId, Department::getId));

// 左连接
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .leftJoin(Department.class, "dept")
    .on(Param.eq(User::getDepartmentId, Department::getId));
```

#### 排序和分页

```java
// 排序
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .orderBy(User::getCreateTime, false) // false表示降序
    .orderBy(User::getName, true);       // true表示升序

// 分页
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .limit(10)
    .offset(20);

// 使用Page对象分页
Page page = new Page();
page.setPageNum(2);
page.setPageSize(15);
query.page(page);
```

#### 聚合查询

```java
// COUNT查询
QueryModel<Long> countQuery = Model.selectCount(User.class)
    .from(User.class)
    .where(Param.eq(User::getStatus, "ACTIVE"));

// 带函数的查询
QueryModel<Object> query = Model.selectWithFunction(User::getAge, "AVG", "avg_age")
    .from(User.class)
    .groupBy(User::getDepartment);
```

### 2.2 更新Model

```java
// 条件更新
UpdateModel updateModel = Model.update(User.class)
    .set(User::getStatus, "INACTIVE")
    .set(User::getUpdateTime, new Date())
    .where(Param.lt(User::getLastLoginTime, lastWeek));

session.execute(updateModel);

// 实体更新
User user = new User();
user.setId(1L);
user.setName("新名称");
UpdateEntityModel updateEntity = Model.update(user);
session.execute(updateEntity);
```

### 2.3 插入Model

```java
// 指定字段插入
InsertModel insertModel = Model.insert(User.class)
    .insert(User::getName, "李四")
    .insert(User::getEmail, "lisi@example.com")
    .insert(User::getCreateTime, new Date());

session.execute(insertModel);

// 实体插入
User user = new User();
user.setName("王五");
user.setEmail("wangwu@example.com");
InsertEntityModel insertEntity = Model.insert(user);
session.execute(insertEntity);
```

### 2.4 删除Model

```java
// 条件删除
DeleteModel deleteModel = Model.deleteFrom(User.class)
    .where(Param.eq(User::getStatus, "DELETED"))
    .limit(100); // 限制删除数量

session.execute(deleteModel);
```

## 3. Mapper - 接口式数据访问

Mapper通过接口和注解提供声明式的数据库操作方式。

### 3.1 定义Mapper接口

```java
@Mapper(User.class)
public interface UserMapper extends Repository<User> {

    // 使用@Sql注解定义SQL
    @Sql(sql = "SELECT * FROM users WHERE department = ${department}",
         paramNames = "department")
    List<User> findByDepartment(String department);

    // 更多复杂SQL示例见下面的动态SQL语法详解
}
```

### 3.2 使用Repository基础接口

Repository接口提供了常用的CRUD操作：

```java
@Mapper(User.class)
public interface UserMapper extends Repository<User> {
    // 继承Repository后自动获得以下方法：
    // T findOne(Param param)
    // List<T> findList(Param param)
    // List<T> findList(Param param, Page page)
    // long count(Param param)
    // void insert(T entity)
    // void update(T entity)
    // void save(T entity)  // 根据主键判断插入或更新
    // void delete(Param param)
}

// 使用示例
UserMapper userMapper = session.getMapper(UserMapper.class);

// 使用Repository方法
User user = userMapper.findOne(Param.eq(User::getId, 1L));
List<User> users = userMapper.findList(Param.like(User::getName, "%张%"));

Page page = new Page();
page.setPageNum(1);
page.setPageSize(10);
List<User> pagedUsers = userMapper.findList(Param.eq(User::getStatus, "ACTIVE"), page);

long count = userMapper.count(Param.eq(User::getStatus, "ACTIVE"));
```

### 3.3 获取和使用Mapper

```java
// 获取Mapper实例
UserMapper userMapper = session.getMapper(UserMapper.class);

// 使用自定义方法
List<User> itUsers = userMapper.findByDepartment("IT");

// 动态SQL调用
List<User> filteredUsers = userMapper.findByConditions("张%", 25, 60);

// 插入数据
String newUserId = userMapper.insertAndReturnKey("新用户", "newuser@example.com", new Date());

// 更新数据
int updatedRows = userMapper.updateEmail("newemail@example.com", 1L);
```

## 4. 实体类映射

### 4.1 实体类注解

```java
@TableDef("users")  // 映射到users表
public class User {

    @Pk  // 主键标记
    @AutoIncrement  // 自增主键
    private Long id;

    @ColumnName("user_name")  // 映射到user_name列
    private String name;

    private String email;  // 默认映射到email列

    @ColumnName(value = "dept_id")
    private Long departmentId;

    @SqlIgnore  // 忽略此字段，不参与SQL操作
    private String tempField;

    // getter和setter方法...
}
```

### 4.2 主键生成策略

```java
// 自增主键
@Pk
@AutoIncrement
private Long id;

// 序列主键（适用于Oracle等）
@Pk
@Sequence("user_seq")
private Long id;

// 自定义主键生成器
@Pk
@PkGenerator(UUIDGenerator.class)
private String id;
```

## 5. 动态SQL语法详解

Jsql框架提供了强大的动态SQL语法，支持条件判断、参数绑定和集合操作。

### 5.1 条件控制语法 `<%%>`

使用`<% if %>`语法进行条件控制：

```java
// 基本条件判断
@Sql(sql = """
            SELECT * FROM users
            <%  if( name != null) {    %>
             WHERE name LIKE ${'%'+name+'%'}
            <%} else {%>
             WHERE status = 'ACTIVE'
             <%}%>
            """, paramNames = "name")
List<User> findByCondition(String name);

// 多重条件判断
@Sql(sql = """
            SELECT * FROM users WHERE
            <% if(id == 1) {%>
             id = 1
            <%} else if(id == 2) {%>
             id = 2
            <%} else {%>
             id = 3
            <%}%>
            """, paramNames = "id")
User findById(int id);

// 复杂条件组合
@Sql(sql = """
            SELECT * FROM users WHERE 1=1
            <% if(name != null) { %>
             AND name LIKE ${'%'+name+'%'}
            <% } %>
            <% if(minAge != null) { %>
             AND age >= ${minAge}
            <% } %>
            <% if(maxAge != null) { %>
             AND age <= ${maxAge}
            <% } %>
            """, paramNames = "name,minAge,maxAge")
List<User> findByMultipleConditions(String name, Integer minAge, Integer maxAge);
```

### 5.2 参数绑定语法

#### 基本参数绑定 `${}`

```java
// 简单参数绑定
@Sql(sql = "SELECT * FROM users WHERE name = ${name}", paramNames = "name")
User findByName(String name);

// 字符串拼接
@Sql(sql = "SELECT * FROM users WHERE name LIKE ${'%'+name+'%'}", paramNames = "name")
List<User> findByNameLike(String name);

@Sql(sql = "SELECT * FROM users WHERE name LIKE ${'%'+name}", paramNames = "name")
List<User> findByNameStartWith(String name);

@Sql(sql = "SELECT * FROM users WHERE name LIKE ${name+'%'}", paramNames = "name")
List<User> findByNameEndWith(String name);
```

#### 对象属性绑定

```java
// 访问对象属性
@Sql(sql = "SELECT * FROM users WHERE name = ${user.name} AND email = ${user.email}",
     paramNames = "user")
User findByUser(User user);

// 枚举处理
@Sql(sql = "SELECT * FROM users WHERE state = ${status.ordinal()}", paramNames = "status")
List<User> findByState(User.State status);

@Sql(sql = "SELECT * FROM users WHERE string_enum = ${enum.name()}", paramNames = "enum")
List<User> findByStringEnum(User.StringEnum enum);
```

#### 静态常量引用

```java
// 引用类的静态常量
@Sql(sql = "SELECT * FROM users WHERE name = ${@(com.jfirer.jsql.test.vo.User).customName}",
     paramNames = "")
User findByStaticConstant();
```

### 5.3 集合参数语法 `~{}`

支持多种集合和数组类型的IN查询：

```java
// List集合
@Sql(sql = "SELECT * FROM users WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIdList(List<Integer> ids);

// 字符串数组
@Sql(sql = "SELECT * FROM users WHERE department IN ~{departments}", paramNames = "departments")
List<User> findByDepartments(String[] departments);

// 整型数组
@Sql(sql = "SELECT * FROM users WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIds(int[] ids);

// 字符串（逗号分隔）
@Sql(sql = "SELECT * FROM users WHERE id IN ~{ids}", paramNames = "ids")
List<User> findByIdString(String ids); // 如: "1,2,3"

// 布尔数组
@Sql(sql = "SELECT * FROM users WHERE active IN ~{flags}", paramNames = "flags")
List<User> findByFlags(boolean[] flags);
```

### 5.4 动态表名和列名 `#{}`

```java
// 动态表名
@Sql(sql = "SELECT COUNT(*) FROM #{tableName}", paramNames = "tableName")
int countFromTable(String tableName);

// 动态表名和条件
@Sql(sql = "SELECT COUNT(*) FROM #{table} WHERE name = ${name}",
     paramNames = "table,name")
int countByTableAndName(String table, String name);

// 动态列名
@Sql(sql = "SELECT #{columnName} FROM users WHERE id = ${id}",
     paramNames = "columnName,id")
Object getColumnValue(String columnName, Integer id);
```

### 5.5 复杂动态SQL示例

```java
// 复合查询示例
@Sql(sql = """
            SELECT u.*, d.name as department_name
            FROM users u
            <% if(includeDepartment) { %>
             LEFT JOIN departments d ON u.dept_id = d.id
            <% } %>
            WHERE 1=1
            <% if(name != null) { %>
             AND u.name LIKE ${'%'+name+'%'}
            <% } %>
            <% if(departments != null && departments.size() > 0) { %>
             AND u.dept_id IN ~{departments}
            <% } %>
            <% if(minAge != null) { %>
             AND u.age >= ${minAge}
            <% } %>
            ORDER BY
            <% if(sortByName) { %>
             u.name ASC
            <% } else { %>
             u.create_time DESC
            <% } %>
            """,
     paramNames = "includeDepartment,name,departments,minAge,sortByName")
List<User> complexQuery(boolean includeDepartment, String name,
                       List<Integer> departments, Integer minAge, boolean sortByName);

// 分页查询配合动态SQL
@Sql(sql = """
            SELECT * FROM users
            WHERE 1=1
            <% if(keyword != null) { %>
             AND (name LIKE ${'%'+keyword+'%'} OR email LIKE ${'%'+keyword+'%'})
            <% } %>
            <% if(status != null) { %>
             AND status = ${status}
            <% } %>
            ORDER BY create_time DESC
            """, paramNames = "keyword,status")
List<User> searchUsers(String keyword, String status, Page page);
```

### 5.6 动态SQL最佳实践

1. **条件嵌套**：使用适当的缩进和格式化保持代码可读性
2. **参数验证**：在动态SQL中进行null检查
3. **性能考虑**：避免过于复杂的动态逻辑
4. **SQL注入防护**：使用参数绑定而非字符串拼接

```java
// 推荐的格式化方式
@Sql(sql = """
            SELECT * FROM users
            WHERE 1=1
            <% if(filters.name != null) { %>
             AND name LIKE ${'%'+filters.name+'%'}
            <% } %>
            <% if(filters.email != null) { %>
             AND email = ${filters.email}
            <% } %>
            <% if(filters.departments != null && !filters.departments.isEmpty()) { %>
             AND department IN ~{filters.departments}
            <% } %>
            """, paramNames = "filters")
List<User> findByFilters(UserSearchFilters filters);
```

## 6. 高级特性

### 6.1 事务管理

```java
try (SqlSession session = sessionFactory.openSession()) {
    // 手动事务控制
    session.begin();
    try {
        session.insert(user1);
        session.insert(user2);
        session.commit();
    } catch (Exception e) {
        session.rollback();
        throw e;
    }
}
```

### 6.2 数据库方言支持

```java
// 配置不同数据库方言
SessionFactoryConfig config = new SessionFactoryConfig();
config.setDialect(new StandardDialect(DialectDict.MYSQL));      // MySQL
config.setDialect(new StandardDialect(DialectDict.POSTGRESQL)); // PostgreSQL
config.setDialect(new StandardDialect(DialectDict.ORACLE));     // Oracle
config.setDialect(new StandardDialect(DialectDict.H2));         // H2
```

### 6.3 自定义类型转换

```java
// 自定义枚举转换
public enum UserStatus {
    ACTIVE, INACTIVE, DELETED
}

// 在实体类中使用
@CustomTransfer(EnumNameTransfer.class)
private UserStatus status;
```

## 7. 最佳实践

### 7.1 资源管理

```java
// 推荐使用try-with-resources
try (SqlSession session = sessionFactory.openSession()) {
    // 数据库操作
} // session自动关闭
```

### 7.2 参数化查询

```java
// 推荐：使用参数化查询防止SQL注入
@Sql(sql = "SELECT * FROM users WHERE name = ${name}", paramNames = "name")
List<User> findByName(String name);

// 不推荐：字符串拼接
// "SELECT * FROM users WHERE name = '" + name + "'"
```

### 7.3 分页查询优化

```java
// 使用框架提供的分页功能
QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(conditions)
    .orderBy(User::getId, true);

Page page = new Page();
page.setPageNum(pageNum);
page.setPageSize(pageSize);
query.page(page);

List<User> results = session.findListByPage(query);
```

### 7.4 批量操作

```java
// 批量插入
List<User> users = prepareUserList();
session.batchInsert(users, 500); // 每批次500条记录

// 批量更新使用事务
try (SqlSession session = sessionFactory.openSession()) {
    session.begin();
    for (User user : users) {
        session.update(user);
    }
    session.commit();
}
```

通过以上API的组合使用，Jsql框架能够满足大部分数据库操作需求，既提供了类型安全的Model API，也支持灵活的Mapper接口定义，同时保持了简洁的使用方式。