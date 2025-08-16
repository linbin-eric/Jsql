# QWEN.md

This file provides guidance to Qwen Code (tongyi.aliyun.com) when working with code in this repository.

## 项目概述 (Project Overview)

JSql is a Java SQL mapping framework designed for high-performance database operations. It provides transparent database access through interface-based SQL binding, automatic result object conversion, and dynamic SQL capabilities. The framework allows developers to bind interface methods to SQL statements, simplifying database interactions.

Key features include:
- Interface-based SQL binding using `@Sql` annotations.
- Stream-based Model API (`Model.select()`, `Model.update()`, etc.) for building SQL queries.
- Support for dynamic SQL with conditional blocks.
- Automatic mapping of query results to Java objects.
- Entity mapping annotations (`@TableDef`, `@Pk`, `@ColumnName`, etc.).
- Support for multiple databases through a dialect system (PostgreSQL, Oracle, DuckDB, standard SQL).

## 构建系统与开发命令 (Build System & Development Commands)

This is a Maven-based Java project targeting Java 17.

### 常用Maven命令 (Common Maven Commands)
```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Package the project (JAR)
mvn package

# Clean build artifacts
mvn clean

# Install the project to the local Maven repository
mvn install

# Run a specific test class
mvn test -Dtest=CURDTest

# Run a specific test method
mvn test -Dtest=CURDTest#testMethodName
```

### 项目结构 (Project Structure)
- `src/main/java/com/jfirer/jsql/` - Main source code.
- `src/test/java/com/jfirer/jsql/test/` - Test code.
- `pom.xml` - Maven configuration file.
- Dependencies include JUnit 4.12, H2 database (for testing), PostgreSQL/MySQL drivers, HikariCP, and others.

## 核心架构 (Core Architecture)

### 关键组件 (Key Components)

1.  **SessionFactory** (`com.jfirer.jsql.SessionFactory`)
    -   Entry point for creating `SqlSession` instances.
    -   Implemented by `SessionFactoryImpl`.
    -   Manages the lifecycle of database sessions.

2.  **SqlSession** (`com.jfirer.jsql.session.SqlSession`)
    -   Represents a database connection/session.
    -   Provides CRUD operations and access to mappers.
    -   Handles transaction management (begin, commit, rollback).
    -   Ensures resources like connections are properly closed.

3.  **Mapper System**
    -   Binds Java interfaces to SQL queries using `@Sql` annotations.
    -   Automatically generates proxy implementations for mapper interfaces.
    -   Accessed via `session.getMapper(MapperInterface.class)`.

4.  **Model System** (`com.jfirer.jsql.model.Model`)
    -   Provides a fluent API for building SQL queries programmatically.
    -   Uses static factory methods like `Model.select()`, `Model.update()`.
    -   Supports type-safe lambda expressions via `SFunction`.

5.  **Entity Mapping Annotations**
    -   `@TableDef`: Maps a Java class to a database table.
    -   `@Pk`: Marks a field as the primary key.
    -   `@ColumnName`: Maps a field to a specific database column.
    -   `@AutoIncrement`: Indicates an auto-incrementing primary key.
    -   `@Sequence`: Supports database sequences for primary key generation.
    -   `@SqlIgnore`: Excludes a field from SQL operations.

### SQL特性 (SQL Features)

-   **Dynamic SQL**: Conditional blocks using `[condition] content #` syntax.
-   **Parameter Binding**: Bind parameters using `$paramName` or `$object.property`.
-   **Dynamic Names**: Use `{dynamicName}` for dynamic table/column names.
-   **Collection Support**: Handle `IN` clauses for arrays/lists with `$~collection`.
-   **Automatic Result Mapping**: Converts query results to Java objects automatically.

### 数据库支持 (Database Support)

The framework supports multiple databases through its dialect system:
- Standard SQL (default)
- PostgreSQL
- Oracle
- DuckDB

## 测试 (Testing)

- Unit tests are primarily run using the H2 in-memory database.
- Test classes are located under `src/test/java/com/jfirer/jsql/test/`.
- Key test files include `CURDTest.java`, `ModelTest.java`, `MapperTest2.java`.
- Test entity classes are in `src/test/java/com/jfirer/jsql/test/vo/`.

## 框架使用模式 (Framework Usage Pattern)

1.  Create a `SessionFactory` using a `DataSource`.
2.  Configure package scanning paths for entity classes.
3.  Call `sessionFactory.init()` to initialize the framework.
4.  Use `sessionFactory.openSession()` to obtain a session for database operations.
5.  Access mappers via `session.getMapper(MapperClass.class)`.
6.  Execute CRUD operations using the session or mappers.
7.  Close the session after operations are complete.

## 扩展的关键接口 (Key Extensible Interfaces)

-   `ResultSetTransfer`: For custom conversion of ResultSet to objects.
-   `Dialect`: For generating database-specific SQL.
-   `CustomTransfer`: For custom type conversions for specific data types.