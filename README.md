# Jsql

一个轻量级、高性能的 Java SQL 映射框架，提供类型安全的流式 API 和声明式 Mapper 接口。

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE)

## 特性

- **类型安全的流式 API** - 使用 Lambda 表达式构建 SQL，编译时检查字段引用
- **声明式 Mapper** - 通过接口和注解定义 SQL，支持动态 SQL 语法
- **多数据库支持** - MySQL、PostgreSQL、Oracle、SQLServer、SQLite、H2、DuckDB
- **零配置映射** - 自动实体类与数据库表的映射
- **轻量级设计** - 无需复杂配置，开箱即用
- **动态条件查询** - 支持条件参数，简化动态 SQL 构建

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>cc.jfire</groupId>
    <artifactId>Jsql</artifactId>
    <version>1.0</version>
</dependency>
```

### 定义实体类

```java
@TableDef("users")
public class User {

    @Pk
    @AutoIncrement
    private Long id;

    @ColumnName("user_name")
    private String name;

    private String email;

    private Integer age;

    @SqlIgnore  // 忽略此字段
    private String tempField;

    // getter 和 setter...
}
```

### 创建 SessionFactory

```java
// 配置数据源
HikariConfig hikariConfig = new HikariConfig();
hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/test");
hikariConfig.setUsername("root");
hikariConfig.setPassword("password");
DataSource dataSource = new HikariDataSource(hikariConfig);

// 创建 SessionFactory
SessionFactoryConfig config = new SessionFactoryConfig();
config.setDataSource(dataSource);
SessionFactory sessionFactory = config.build();
```

### 使用 SqlSession

```java
try (SqlSession session = sessionFactory.openSession()) {
    // 插入
    User user = new User();
    user.setName("张三");
    user.setEmail("zhangsan@example.com");
    session.insert(user);

    // 查询
    QueryModel<User> query = Model.selectAll(User.class)
        .from(User.class)
        .where(Param.eq(User::getName, "张三"));
    User result = session.findOne(query);

    // 更新
    result.setEmail("newemail@example.com");
    session.update(result);
}
```

## 核心概念

### 1. SqlSession

`SqlSession` 是数据库操作的核心接口，代表一个数据库连接会话。

```java
try (SqlSession session = sessionFactory.openSession()) {
    // CRUD 操作
    session.insert(entity);
    session.update(entity);
    session.save(entity);  // 自动判断 insert 或 update

    // Model 查询
    User user = session.findOne(queryModel);
    List<User> users = session.findList(queryModel);
    Page<User> page = session.findListByPage(queryModel);

    // 原生 SQL
    List<User> results = session.queryList(
        "SELECT * FROM users WHERE age > ?",
        new BeanTransfer<>(User.class),
        Arrays.asList(18)
    );

    // 事务管理
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

### 2. Model - 流式 SQL 构建器

Model 提供类型安全的流式 API 构建 SQL：

#### 查询

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

// 聚合查询
QueryModel<Long> countQuery = Model.selectCount(User.class)
    .from(User.class)
    .where(Param.eq(User::getStatus, "ACTIVE"));
```

#### 条件构建

```java
// 基本条件
Param.eq(User::getName, "张三")           // name = '张三'
Param.notEq(User::getStatus, "DELETED")   // status != 'DELETED'
Param.bt(User::getAge, 18)                // age > 18
Param.lt(User::getAge, 60)                // age < 60
Param.be(User::getAge, 18)                // age >= 18
Param.le(User::getAge, 60)                // age <= 60

// 字符串匹配
Param.like(User::getName, "%管理员%")      // name LIKE '%管理员%'
Param.startWith(User::getName, "张")      // name LIKE '张%'
Param.endWith(User::getName, "三")        // name LIKE '%三'
Param.contain(User::getName, "三")        // name LIKE '%三%'

// 范围和集合
Param.between(User::getAge, 18, 60)       // age BETWEEN 18 AND 60
Param.in(User::getDept, "IT", "HR")       // dept IN ('IT', 'HR')
Param.notIn(User::getStatus, "A", "B")    // status NOT IN ('A', 'B')

// 空值判断
Param.isNull(User::getDeletedAt)          // deleted_at IS NULL
Param.notNull(User::getEmail)             // email IS NOT NULL

// 条件组合
Param.eq(User::getStatus, "ACTIVE")
     .and(Param.bt(User::getAge, 18))
     .or(Param.like(User::getName, "%VIP%"))
```

#### 动态条件（条件参数）

所有 Param 方法都支持条件参数，当条件为 false 时该参数不会被添加到查询中：

```java
String name = getSearchName();       // 可能为 null
String status = getStatusFilter();   // 可能为 null
Integer minAge = getMinAgeFilter();  // 可能为 null

QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(Param.contain(name != null, User::getName, name)
           .and(Param.eq(status != null, User::getStatus, status))
           .and(Param.be(minAge != null, User::getAge, minAge)));
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
    .orderBy(User::getCreateTime, false)  // 降序
    .orderBy(User::getName, true);        // 升序

// 分页
Page<User> page = new Page<User>()
    .setOffset(0)
    .setSize(10)
    .setFetchSum(true);  // 是否查询总数
query.page(page);

Page<User> result = session.findListByPage(query);
```

#### 更新和删除

```java
// 条件更新
UpdateModel updateModel = Model.update(User.class)
    .set(User::getStatus, "INACTIVE")
    .set(User::getUpdateTime, new Date())
    .where(Param.lt(User::getLastLoginTime, lastWeek));
session.execute(updateModel);

// 条件删除
DeleteModel deleteModel = Model.deleteFrom(User.class)
    .where(Param.eq(User::getStatus, "DELETED"))
    .limit(100);
session.execute(deleteModel);
```

### 3. Mapper - 声明式接口

通过接口和注解定义数据访问方法：

```java
@Mapper(User.class)
public interface UserMapper extends Repository<User> {

    @Sql(sql = "SELECT * FROM users WHERE department = ${department}",
         paramNames = "department")
    List<User> findByDepartment(String department);

    @Sql(sql = """
        SELECT * FROM users WHERE 1=1
        <% if(name != null) { %>
         AND name LIKE ${'%'+name+'%'}
        <% } %>
        <% if(minAge != null) { %>
         AND age >= ${minAge}
        <% } %>
        """, paramNames = "name,minAge")
    List<User> findByConditions(String name, Integer minAge);
}
```

#### Repository 基础接口

继承 `Repository<T>` 自动获得常用 CRUD 方法：

```java
@Mapper(User.class)
public interface UserMapper extends Repository<User> {
    // 继承的方法：
    // T findOne(Param param)
    // List<T> findList(Param param)
    // List<T> findList(Param param, Page page)
    // int count(Param param)
    // int delete(Param param)
    // int insert(T entity)
    // int save(T entity)
    // int update(T entity)
}

// 使用示例
UserMapper mapper = session.getMapper(UserMapper.class);
User user = mapper.findOne(Param.eq(User::getId, 1L));
List<User> users = mapper.findList(Param.like(User::getName, "%张%"));
```

#### 动态 SQL 语法

| 语法 | 说明 | 示例 |
|------|------|------|
| `${expr}` | 参数绑定 | `${name}`, `${'%'+name+'%'}` |
| `#{name}` | 字符串替换（动态表名/列名） | `#{tableName}` |
| `~{list}` | IN 子句 | `id IN ~{ids}` |
| `<% if() {} %>` | 条件判断 | `<% if(name != null) { %> AND name = ${name} <% } %>` |

## 实体映射注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@TableDef` | 映射数据库表名 | `@TableDef("users")` |
| `@Pk` | 标记主键字段 | `@Pk` |
| `@AutoIncrement` | 自增主键 | `@AutoIncrement` |
| `@Sequence` | 序列主键（Oracle等） | `@Sequence("user_seq")` |
| `@PkGenerator` | 自定义主键生成器 | `@PkGenerator(UUIDGenerator.class)` |
| `@ColumnName` | 映射列名 | `@ColumnName("user_name")` |
| `@SqlIgnore` | 忽略字段 | `@SqlIgnore` |

## 数据库支持

框架通过方言系统支持多种数据库：

| 数据库 | 支持状态 | 分页语法 |
|--------|----------|----------|
| MySQL | ✅ | LIMIT offset, size |
| PostgreSQL | ✅ | LIMIT size OFFSET offset |
| Oracle | ✅ | ROWNUM 包装 |
| SQLServer | ✅ | OFFSET-FETCH |
| SQLite | ✅ | LIMIT size OFFSET offset |
| H2 | ✅ | LIMIT size OFFSET offset |
| DuckDB | ✅ | LIMIT size OFFSET offset |

## 类型转换

框架内置常用类型转换器：

| Java 类型 | 数据库类型 |
|-----------|-----------|
| Integer, Long, Short | 整数类型 |
| Float, Double, BigDecimal | 浮点数类型 |
| String | VARCHAR/TEXT |
| Boolean | BOOLEAN/TINYINT |
| java.util.Date | TIMESTAMP |
| java.sql.Date | DATE |
| LocalDate | DATE |
| LocalDateTime | TIMESTAMP |
| Enum | VARCHAR (name) 或 INT (ordinal) |
| byte[] | BLOB/BYTEA |

### 自定义类型转换

```java
@CustomTransfer(EnumNameTransfer.class)
private UserStatus status;
```

## 批量操作

```java
// 批量插入
List<User> users = prepareUserList();
session.batchInsert(users, 500);  // 每批次 500 条

// 批量操作使用事务
session.begin();
for (User user : users) {
    session.update(user);
}
session.commit();
```

## 位运算查询

```java
// 检查权限标志位
int READ_PERMISSION = 0x01;
int WRITE_PERMISSION = 0x02;

QueryModel<User> query = Model.selectAll(User.class)
    .from(User.class)
    .where(Param.bitwiseAndByEquals(
        User::getPermissions,
        READ_PERMISSION,
        READ_PERMISSION
    ));
```

## 项目结构

```
src/main/java/cc/jfire/jsql/
├── SessionFactory.java          # 工厂接口
├── SessionFactoryConfig.java    # 配置类
├── annotation/                  # 注解定义
├── dialect/                     # 数据库方言
├── executor/                    # SQL 执行器
├── mapper/                      # Mapper 相关
├── metadata/                    # 元数据
├── model/                       # Model 流式 API
├── session/                     # Session 接口
└── transfer/                    # 结果集转换
```

## 依赖

- Java 17+
- jfireEL 1.0（表达式语言）
- 可选：Lombok

## 测试数据库驱动

- H2（内存数据库测试）
- PostgreSQL
- MySQL
- HikariCP（连接池）

## 许可证

Apache License 2.0

## 贡献

欢迎提交 Issue 和 Pull Request。
