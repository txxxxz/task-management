# 任务管理系统 | Task Management System

<details>
<summary>🇨🇳 中文</summary>

## 📝 项目介绍

任务管理系统是一个全栈应用，旨在帮助个人和团队高效管理任务、项目和工作流程。该系统提供看板视图、甘特图、仪表盘等多种方式来可视化和管理任务。

## 🛠️ 技术栈

### 前端
- Vue 3
- TypeScript
- Element Plus
- Pinia (状态管理)
- Vue Router
- ECharts (数据可视化)
- Frappe Gantt (甘特图)

### 后端
- Spring Boot 2.7.3
- MyBatis Plus 3.5.2
- Java 17
- Maven
- MySQL

## ✨ 功能特性

- 📊 项目和任务管理
- 📈 任务看板视图
- 📅 甘特图展示任务时间线
- 🏷️ 标签系统方便分类
- 📱 响应式设计，支持移动端
- 📊 数据统计和可视化面板
- 👤 用户管理和权限控制

## 🚀 快速开始

### 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 打包生产环境
npm run build
```

### 后端启动

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean package

# 运行服务
java -jar guinea-pig-server/target/guinea-pig-server.jar
```

### 数据库配置

1. 确保MySQL服务已启动
2. 创建数据库：`task_management`
3. 执行`backend/db`目录下的SQL脚本初始化数据库

## 💻 使用方法

1. 访问前端应用：`http://localhost:5173/`
2. 使用以下默认账号登录:
   - 用户名: admin
   - 密码: 123456
3. 开始创建项目和任务

## 📚 API文档

API文档通过Swagger提供: `http://localhost:8080/doc.html`

</details>

<details open>
<summary>🇺🇸 English</summary>

## 📝 Project Introduction

Task Management System is a full-stack application designed to help individuals and teams efficiently manage tasks, projects, and workflows. The system provides multiple ways to visualize and manage tasks, including kanban boards, Gantt charts, and dashboards.

## 🛠️ Technology Stack

### Frontend
- Vue 3
- TypeScript
- Element Plus
- Pinia (State Management)
- Vue Router
- ECharts (Data Visualization)
- Frappe Gantt (Gantt Charts)

### Backend
- Spring Boot 2.7.3
- MyBatis Plus 3.5.2
- Java 17
- Maven
- MySQL

## ✨ Features

- 📊 Project and task management
- 📈 Kanban board view
- 📅 Gantt chart for task timeline
- 🏷️ Tag system for easy classification
- 📱 Responsive design for mobile support
- 📊 Data statistics and visualization dashboard
- 👤 User management and permission control

## 🚀 Quick Start

### Frontend Setup

```bash
# Enter frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

### Backend Setup

```bash
# Enter backend directory
cd backend

# Compile project
mvn clean package

# Run server
java -jar guinea-pig-server/target/guinea-pig-server.jar
```

### Database Configuration

1. Ensure MySQL service is running
2. Create database: `task_management`
3. Execute SQL scripts in the `backend/db` directory to initialize the database

## 💻 Usage

1. Access the frontend: `http://localhost:5173/`
2. Login with default credentials:
   - Username: admin
   - Password: 123456
3. Start creating projects and tasks

## 📚 API Documentation

API documentation is available via Swagger: `http://localhost:8080/doc.html`

</details>

## 📷 Screenshots

![Dashboard](screenshots/dashboard.png)
![Kanban Board](screenshots/kanban.png)
![Gantt Chart](screenshots/gantt.png)

## 📄 License

MIT License 