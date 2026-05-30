# 智享AI助手

基于Spring Boot的外卖点餐平台，集成RAG智能客服

## 技术栈

- **后端框架**: Spring Boot 3.5.6
- **Java版本**: JDK 21
- **数据库**: MySQL 8.x
- **缓存**: Redis
- **ORM**: MyBatis
- **对象存储**: 阿里云OSS
- **微信小程序**: 微信登录、支付

## 项目结构

```
zhixiang-ai-assistant/
├── scommon/             # 公共模块（工具类、常量、异常处理）
├── pojo/                # 实体类模块（DTO、VO、Entity）
├── server/              # 主服务模块（Controller、Service、Mapper）
└── pom.xml              # Maven父工程配置
```

## 功能模块

### 管理端
- 员工管理（登录、增删改查）
- 分类管理
- 菜品管理（含阿里云OSS图片上传）
- 套餐管理
- 订单管理（统计、导出）
- 店铺管理

### 用户端
- 微信登录
- 浏览菜品和套餐
- 购物车
- 下单支付
- 订单查询
- 地址管理

### AI智能客服
- 基于RAG的智能问答
- 订单查询和催单功能

## 快速开始

### 环境要求
- JDK 21+
- MySQL 8.x
- Redis
- Maven 3.8+

### 配置步骤

1. **克隆项目**
```bash
git clone https://github.com/Blank-ye/my-first-project.git
cd zhixiang-ai-assistant
```

2. **创建数据库**
```sql
CREATE DATABASE sky_take_out DEFAULT CHARACTER SET utf8mb4;
```

3. **修改配置文件**
编辑 `server/src/main/resources/application-dev.yml`，配置：
- 数据库连接信息
- Redis连接信息
- 阿里云OSS密钥
- 微信小程序AppID和Secret

4. **编译运行**
```bash
mvn clean package
java -jar server/target/server-1.0-SNAPSHOT.jar
```

## API文档

启动后访问：`http://localhost:8080/doc.html` (Swagger/Knife4j)

## 开发说明

- 启动类：`server/src/main/java/com/sky/SkyApplication.java`
- 配置文件：`server/src/main/resources/application.yml`
- 接口文档：使用Knife4j自动生成

## 许可证

MIT License