# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

JSql是一个Java SQL映射框架，通过基于接口的SQL绑定提供透明的数据库操作。它允许开发者将接口方法与SQL语句绑定，实现自动的结果对象转换，并支持动态SQL功能。

## 构建系统与开发命令

这是一个基于Maven的Java项目，使用Java 17。

### 常用Maven命令
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

# 运行特定测试类
mvn test -Dtest=CURDTest

# 运行特定测试方法
mvn test -Dtest=CURDTest#testMethodName
```

### 项目结构
- `src/main/java/com/jfirer/jsql/` - 主要源代码
- `src/test/java/com/jfirer/jsql/test/` - 测试代码
- `pom.xml` - Maven配置
- 依赖包括JUnit 4.12、H2数据库（用于测试）、PostgreSQL/MySQL驱动

## 核心架构

### 关键组件

1. **SessionFactory** (`com.jfirer.jsql.SessionFactory`)
   - 创建和管理SqlSession实例
   - 框架初始化的入口点
   - 实现类：`SessionFactoryImpl`

2. **SqlSession** (`com.jfirer.jsql.session.SqlSession`)
   - 代表一个数据库连接
   - 提供CRUD操作和mapper访问
   - 支持事务管理（开始、提交、回滚）
   - 自动关闭资源

3. **Mapper系统**
   - 基于接口的SQL绑定，使用`@Sql`注解
   - 自动代理生成接口实现
   - 通过`session.getMapper(MapperInterface.class)`访问

4. **Model系统** (`com.jfirer.jsql.model.Model`)
   - 流式API构建SQL查询
   - 静态工厂方法：`Model.select()`、`Model.update()`、`Model.insert()`等
   - 通过`SFunction`支持类型安全的lambda表达式

5. **实体映射注解**
   - `@TableDef` - 将类映射到数据库表
   - `@Pk` - 标记主键字段
   - `@ColumnName` - 将字段映射到数据库列
   - `@AutoIncrement` - 自增主键
   - `@Sequence` - 数据库序列支持
   - `@SqlIgnore` - 排除字段不参与SQL操作

### SQL特性

- **动态SQL**：使用`[condition] content #`语法的条件块
- **参数绑定**：`$paramName`或`$object.property`语法
- **动态表名/列名**：`{dynamicName}`语法
- **集合支持**：`$~collection`用于数组/列表的IN子句
- **自动结果映射**：查询结果自动转换为对象

### 数据库支持

框架通过方言系统支持多种数据库：
- 标准SQL（默认）
- PostgreSQL（支持特定分页）
- Oracle（支持特定分页）
- DuckDB（支持特定分页）

## 测试

- 测试使用H2内存数据库进行单元测试
- 测试类位于`src/test/java/com/jfirer/jsql/test/`
- 关键测试文件：`CURDTest.java`、`ModelTest.java`、`MapperTest2.java`
- 测试实体位于`src/test/java/com/jfirer/jsql/test/vo/`

## 框架使用模式

1. 使用DataSource创建`SessionFactory`
2. 配置扫描包路径用于实体类
3. 调用`sessionFactory.init()`进行初始化
4. 使用`sessionFactory.openSession()`进行数据库操作
5. 通过`session.getMapper(MapperClass.class)`访问mapper
6. 通过session或mapper执行CRUD操作
7. 操作完成后关闭session

## 扩展的关键接口

- `ResultSetTransfer` - 自定义结果集到对象的转换
- `Dialect` - 数据库特定的SQL生成
- `CustomTransfer` - 特定数据类型的自定义类型转换