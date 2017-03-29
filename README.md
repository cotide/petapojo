## 背景

由于工作的一些原因，需要从C#转成JAVA。之前PetaPoco用得真是非常舒服，在学习JAVA的过程中熟悉了一下JAVA的数据组件：

1. `MyBatis`: 非常流行，代码生成也很成熟，性能也很好。但是DEBUG的时候不方便，且XML写SQL也不是很适应，尤其是团队比较小没有专职DBA的情况下。
2. `Hibernate`： 使用过NHibernate，做企业应用倒是挺适合的。掌握并用好它不是一件很容易的事情，尤其是团队水平不够，目标项目为互联网游戏平台的时候。
3. `sql2o`： 开源项目，轻量级的ORM，与Dapper，PetaPoco非常类似，感觉上还是没有PetaPoco好用。  

基于以上的理解，便打算造一个JAVA版的PetaPoco —— PataPojo：

1. 处于学习JAVA阶段，能够学习并使用一些高级语法
2. 理解JAVA的基础数据库组件
3. 若开发成功，则原有的团队与项目基本上能够很好地迁移至JAVA开发环境，提升开发效率

## 功能概述

- 轻量级
- 用于简单的POJO
- 泛型的增删改查的帮助方法提供
- 自动分页，自动计算出总记录数及页数据
- 简单的事务支持
- 暂时仅支持MYSQL
- 更好的参数支持
- 仍然使用SQL语法，并提供强大的Sql生成器类

## 开源
下载

GitHub：  https://github.com/gejinlove/petapojo/

## 怎么使用

### 引用jar包

下载petapojo.jar包后，在项目中进行引用

## 执行查询

### 实体Mapping
```java
// 对应表 user_info
@TableName("user_info")
// 主键映射，字段名，是否自增
@PrimaryKey(value = "id",autoIncrement = true)
public class UserInfo {

    private int id;
    private String userName;

    // 列名与字段名的映射 （若无注解，则按字段名映射为列名）
    @Column("password")
    private String password;

    // 该字段不进行映射
    @Ignore
    private String none;

    //... getter and setter
}
```

下一步，使用database.java进行查询：

```java
DruidDataSource dataSource = new DruidDataSource();
// 配置dataSource
// ...
Database database = new Database(dataSource);
List<UserInfo> userList = database.query(UserInfo.class, "SELECT * FROM user_info");

for (UserInfo userInfo : userList) {
       System.out.println(userInfo.getUserName());
}
``` 


查询第一行第一列：
```java 
Integer number = database.executeScalar(Integer.class,
        "SELECT COUNT(1) FROM user_info");
// 或者，查找第一条记录：
UserInfo userInfo = database.firstOrDefault(UserInfo.class,
        "SELECT * FROM user_info WHERE id = ?",123);

```

### 分页

```java
// 参数说明：
// 1  - 页索引
// 10 - 页大小
// 20 - 是指 age > ? 的参数值
PageInfo<UserInfo> pagedList = database.pagedList(UserInfo.class, 1,10,
                "SELECT * FROM user_info WHERE age > ? ORDER BY createDate DESC",20);
```

返回的PageInfo描述：
```java
/**
 * 分页泛型
 */
public class PageInfo<T> {
    // 当前页索引
    private int currentPage;
    // 总页数
    private int totalPages;
    // 总记录数
    private int totalItems;
    // 每页记录数
    private int itemsPrePage;
    // 当前页列表
    private List<T> items;

    public PageInfo() {
        items = new ArrayList<>();
    }

    // ...
    // getter and setter
}
```

### 执行没有返回的SQL：
```java
// 返回影响行数
int row = database.executeUpdate("DELETE FROM user_info WHERE id = ?",123);
```

### 实体的增删改
新增：
```java
UserInfo userInfo = new UserInfo();
userInfo.setUserName("PetaPojo");
userInfo.setPassword("123123");

database.insert(userInfo);
```
修改：
```java
UserInfo userInfo = database.firstOrDefault(UserInfo.class,
            "SELECT * FROM user_info WHERE id = ?", 1);
userInfo.setPassword("123456");

database.update(userInfo);
```
删除：
```java
UserInfo userInfo = database.firstOrDefault(UserInfo.class,
            "SELECT * FROM user_info WHERE id = ?", 1);
database.delete(userInfo);

// 或者 根据主键删除
database.delete(UserInfo.class,1);

// 或者 根据条件进行删除
database.delete(UserInfo.class,"WHERE id = ?",1);
```

### 自动添加查询列

当我们在使用ORM时，我们常常需要先编写查询列名及表名的SQL语句SELECT * FROM user_info，其实是非常影响开发效率的。
因此，PetaPojo增加了自动添加查询列与表名的自动匹配功能。
例如：
```java
UserInfo userInfo = database.firstOrDefault(UserInfo.class,
        "SELECT * FROM user_info WHERE id = ?", 1);
```
PetaPojo可以允许简化为：
```java
UserInfo userInfo = database.firstOrDefault(UserInfo.class,"WHERE id = ?",1);
```

## 查询组装器 Sql Builder

在我们查询数据库时，经常需要添加一些条件或排序之类的。总之，尽可能地让SQL语句动态化或更灵活，以应对复杂的业务需要。
与此同时，如果我们只是进行单纯的SQL硬编写，开发效率将会是一个很大的问题，维护亦比较复杂费时。

在此基础上，PetaPojo提供了一个非常便捷的SQL查询组装器。

基础模式：
```java
int id = 1;
Sql sql = Sql.create()
        .append("SELECT * FROM user_info")
        .append("WHERE id = ?",id);
UserInfo userInfo = database.firstOrDefault(UserInfo.class,sql);
```
或者：
```java
int id = 1;
Sql sql = Sql.create()
        .append("SELECT * FROM user_info")
        .append("WHERE id = ?", id)
        .append("AND createDate >= ?", DateTime.now());
UserInfo userInfo = database.firstOrDefault(UserInfo.class, sql);
```
同样，可以根据不同的条件来进行组装：
```java
int id = 1;
Sql sql = Sql.create()
        .append("SELECT * FROM user_info")
        .append("WHERE id <> ?", id)

if(age != null)
     sql.append("AND age > ?", age);

if(startDate != null)
    sql.append("AND createDate >= ?", startDate);

List<UserInfo> userList = database.query(UserInfo.class,sql);
```

在SQL组装时，参数列表是无限的，只需要与SQL语句中的参数替代符?相匹配即可，如：
```java
int id = 1;
DateTime now = DateTime.now();

Sql sql = Sql.create()
        .append("SELECT * FROM user_info")
        .append("WHERE id <> ? AND createDate >= ?", id, now);

List<UserInfo> userList = database.query(UserInfo.class,sql);
```
在Sql.append的基础上，PetaPojo提供了更为便捷的链式函数：
```java
Sql sql = Sql.create()
        .select("*")
        .from("user_info")
        .where("id = ?", 1)
        .where("createDate >= ?", DateTime.now().toString())
        .where("age >= ? AND age <= ?", 10, 20)
        .orderBy("createDate DESC");

PageInfo<UserInfo> pagedList = database.pagedList(UserInfo.class,1,10,sql);
```

基于自动添加查询列的功能，上述语句可变改为：

```java
Sql sql = Sql.create() 
        .where("id = ?", 1)
        .where("createDate >= ?", DateTime.now())
        .where("age >= ? AND age <= ?", 10, 20)
        .orderBy("createDate DESC");

PageInfo<UserInfo> pagedList = database.pagedList(UserInfo.class,1,10,sql);
```
因此，在一些复杂的查询中，我们可这样用：
```java
int id = 1;
Sql sql = Sql.create() 
        .where("id <> ?", id)

if(age != null)
     sql.where("age > ?", age);

if(startDate != null)
    sql.where("createDate >= ?", startDate);

sql.orderBy("createDate DESC");

List<UserInfo> userList = database.query(UserInfo.class,sql);
```

## 枚举支持

在开发中，经常会碰到一些需要使用枚举的地方，如订单状态，用户类型等。
JAVA默认枚举类型不是很好用，最主要的问题在于：用户类型，订单状态这些枚举在系统中我们可以理解为一个键值对的列表，而JAVA默认枚举的index是不可自定义的。

因此，PetaPojo在枚举的支持上做了一些扩展。

首先，PetaPojo定义了一个枚举接口：

```java
/**
 * 枚举必须要实现的接口
 */
public interface IEnumMessage {

    int getValue();

    String getName();
} 
```
并提供了一个枚举帮助类：
```java
public class EnumUtils {

    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl = rrwl.readLock();
    private static final ReentrantReadWriteLock.WriteLock wl = rrwl.writeLock();
    private static Map<Class<? extends IEnumMessage>, Map<Integer, IEnumMessage>> ENUM_MAPS = new HashMap<>();

    /**
      * 根据key以及枚举类型，得到具体的枚举对象
      */
    public static <T extends IEnumMessage> T getEnum(Class<T> type, int value) {
        return (T) getEnumValues(type).get(value);
    }

     /**
      * 将枚举类型转化为一个键值对的Map
      */
    public static <T extends IEnumMessage> Map<Integer, String> getEnumItems(Class<T> type) {
        Map<Integer, IEnumMessage> map = getEnumValues(type);
        Map<Integer, String> resultMap = new HashMap<>();
        map.forEach((k, v) -> {
            resultMap.put(k, v.getName());
        });
        return resultMap;
    }

    private static <T extends IEnumMessage> Map<Integer, IEnumMessage> getEnumValues(Class<T> clazz) {
        rl.lock();
        try {
            if (ENUM_MAPS.containsKey(clazz))
                return ENUM_MAPS.get(clazz);
        } finally {
            rl.unlock();
        }

        wl.lock();
        try {
            if (ENUM_MAPS.containsKey(clazz))
                return ENUM_MAPS.get(clazz);

            Map<Integer, IEnumMessage> map = new HashMap<>();
            try {
                for (IEnumMessage enumMessage : clazz.getEnumConstants()) {
                    map.put(enumMessage.getValue(), enumMessage);
                }
            } catch (Exception e) {
                throw new RuntimeException("getEnumValues error", e);
            }
            ENUM_MAPS.put(clazz, map);

            return map;
        } finally {
            wl.unlock();
        }
    }
}
```
进而，在项目使用枚举（如UserType）时，实现IEnumMessage接口即可：

```java
/**
 * 用户类型
 */
public enum UserType implements IEnumMessage {

    Student(1,"学生"),
    Teacher(2,"老师"),
    Coder(4,"码农");

    private int value;
    private String name;

    UserType(int value,String name){
        this.value = value;
        this.name = name;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
```
对应的实体：
```java
@TableName("user_info")
@PrimaryKey(value = "id",autoIncrement = true)
public class UserInfo {

    private int id;
    private String userName;
    private String password;

    // 直接使用枚举，数据库中作为int型进行存储
    private UserType userType;

    // ...  getter setter
}
UserInfo userInfo = database.firstOrDefault(UserInfo.class,
        "SELECT * FROM user_info WHERE id = ?",1);
// 设置用户类型为码农
userInfo.setUserType(UserType.Coder);

database.update(userInfo);
```
##时间类型支持 org.joda.time.DateTime

`PetaPojo`支持`org.joda.time.DateTime`类型，映射的数据库表字段类型为datetime：
```java
@TableName("user_info")
@PrimaryKey(value = "id",autoIncrement = true)
public class UserInfo {

    private int id;
    private String userName;
    private String password;

    // 直接使用枚举，数据库中作为int型进行存储
    private UserType userType;

    private DateTime createDate;

    // ...  getter setter
}
```
当SQL查询中的参数值为org.joda.time.DateTime时，这样使用：
```java
Sql sql = Sql.create()
            .where("createDate >= ?",DateTime.now().toString());
// 将DateTime类型toString即可
```
以上便是整体PetaPojo的主要功能。