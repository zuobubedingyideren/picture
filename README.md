# Picture Management System

一个基于 Vue.js + Spring Boot 的图片管理系统，支持图片上传、管理、分享和空间协作功能。

## 项目结构

```
picture/
├── frontend/          # 前端项目 (Vue.js + TypeScript)
│   ├── src/
│   │   ├── components/    # Vue 组件
│   │   ├── pages/         # 页面组件
│   │   ├── stores/        # Pinia 状态管理
│   │   ├── api/           # API 接口
│   │   └── ...
│   ├── package.json
│   └── vite.config.ts
│
├── backend/           # 后端项目 (Spring Boot + Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java 源码
│   │   │   └── resources/ # 配置文件
│   │   └── test/          # 测试代码
│   ├── pom.xml
│   └── httpTest/          # HTTP 接口测试文件
│
├── .gitignore         # Git 忽略文件配置
└── README.md          # 项目说明文档
```

## 功能特性

### 用户管理
- 用户注册、登录、权限管理
- 用户个人信息管理
- 多角色权限控制（管理员、普通用户）

### 图片管理
- 图片上传（支持多种格式）
- 图片预览、编辑、删除
- 图片分类和标签管理
- 图片颜色分析和搜索
- 图片审核机制

### 空间管理
- 私有空间和企业空间
- 空间成员管理和权限控制
- 空间使用情况分析
- 协作功能（查看者、编辑者、管理员）

### 数据分析
- 空间使用统计
- 图片分类分析
- 标签使用情况

## 技术栈

### 前端
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: Ant Design Vue
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios

### 后端
- **框架**: Spring Boot 3.x
- **数据库**: MySQL + MyBatis-Plus
- **安全**: Spring Security
- **文件存储**: 本地存储 + 云存储支持
- **API 文档**: OpenAPI 3.0

## 快速开始

### 前端开发

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 后端开发

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 数据库配置

1. 创建 MySQL 数据库
2. 执行 `backend/sql/create_user_table.sql` 中的建表语句
3. 配置 `backend/src/main/resources/application.yml` 中的数据库连接信息

## API 文档

启动后端服务后，可以通过以下地址访问 API 文档：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 测试

项目包含完整的 HTTP 接口测试文件，位于 `backend/httpTest/` 目录：
- `picture.http` - 图片相关接口测试
- `space_user.http` - 空间用户管理接口测试
- `http-client.env.json` - 测试环境配置

## 权限系统

系统实现了细粒度的权限控制：

### 空间权限
- `spaceUser:manage` - 空间成员管理
- `picture:view` - 图片查看
- `picture:upload` - 图片上传
- `picture:edit` - 图片编辑
- `picture:delete` - 图片删除

### 用户角色
- **查看者 (viewer)**: 只能查看图片
- **编辑者 (editor)**: 可以查看、上传、编辑图片
- **管理员 (admin)**: 拥有所有权限，包括成员管理

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过 Issues 或 Pull Requests 与我们联系。