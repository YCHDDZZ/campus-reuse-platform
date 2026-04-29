# 校园二手闲置循环平台

毕业设计项目：**基于 Spring Boot 的校园二手闲置循环平台设计与实现**。

## 技术栈

### 后端
- Spring Boot 2.7.18
- Spring Security + JWT
- MyBatis-Plus 3.5.5
- MySQL 8
- Redis（商品搜索缓存，Redis 不可用时自动降级为本地内存缓存）
- MinIO（文件上传、预览、删除）

### 前端
- Vue 3
- Vite 6
- Element Plus
- Pinia
- Vue Router
- Axios

## 当前已完成功能

- 用户注册、登录、JWT 鉴权
- 用户资料维护、头像上传到 MinIO
- 用户审核、商品审核、分类管理
- 商品发布、搜索筛选、详情查看、收藏
- 商品图片上传、联系二维码上传
- 商品留言沟通、违规举报
- 订单创建、取消、完成、纠纷处理
- 交易评价
- 闲置捐赠
- 管理员统计看板与循环指数
- 演示环境数据初始化

## 项目结构

- `backend`：Spring Boot + Spring Security + MyBatis-Plus 后端
- `frontend`：Vue3 + Vite + Element Plus 前端
- `backend/sql/schema-mysql.sql`：MySQL 全量建表脚本
- `backend/sql/optimize-indexes.sql`：MySQL 索引优化脚本
- `backend/sql/alter-platform-user-add-avatar-url.sql`：头像字段增量脚本
- `项目运行说明.txt`：项目启动与联调说明

## 演示账号

- 管理员：`admin / Admin@123`
- 普通用户：`student / 123456`
- 卖家用户：`seller / 123456`

## 运行环境要求

- JDK 11
- Maven 3.9+
- Node.js 20+
- npm 10+
- MySQL 8
- Redis 6/7（可选，未启动时系统会自动降级本地缓存）
- MinIO（S3 API 端口）

## 后端启动

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://127.0.0.1:8080`

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://127.0.0.1:5173`

## 关键说明

### 1. MySQL
项目默认 `prod` 环境使用 MySQL。
如需初始化数据库，可执行：

- `backend/sql/schema-mysql.sql`
- 已有旧库时再执行：
  - `backend/sql/alter-platform-user-add-avatar-url.sql`
  - `backend/sql/optimize-indexes.sql`

### 2. Redis
项目商品搜索已接入缓存。

- Redis 可用：使用 Redis 缓存
- Redis 不可用：自动降级为本地内存缓存，不影响核心功能运行

### 3. MinIO
项目已接入 MinIO：

- 商品图片上传
- 联系二维码上传
- 用户头像上传
- 文件预览
- 文件删除

当前配置应使用 **S3 API 端口 9000**，不是控制台端口 9001。

## 本地验证结果（2026-03-17）

- MySQL：已验证可连接，`campus_reuse` 数据库与业务表存在
- Redis：本机 `127.0.0.1:6379` 当前未启动；项目已实现自动降级
- MinIO：已验证可连通、可上传、可预览、可删除
- 后端：`mvn -q -DskipTests compile` 通过，`mvn spring-boot:run` 可正常启动
- 前端：`npm run build` 通过
