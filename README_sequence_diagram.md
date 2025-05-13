# 序列图生成工具

这个Python脚本用于从任务管理系统项目中自动生成序列图。它可以分析Java后端控制器和Vue前端代码，生成以下序列图：

1. **系统交互概览图** - 显示整个系统主要组件之间的关系
2. **用户认证流程图** - 包括登录、注册和获取用户信息流程
3. **任务管理流程图** - 包括创建、查看、更新和删除任务流程
4. **项目管理流程图** - 包括创建、查看、更新和删除项目流程
5. **任务评论流程图** - 包括添加和管理评论的流程
6. **文件上传流程图** - 包括上传任务和项目附件的流程

## 前提条件

1. Python 3.6+
2. Graphviz 安装在系统上 (用于绘制图形)

### 安装 Graphviz

- **macOS**: `brew install graphviz`
- **Ubuntu/Debian**: `sudo apt-get install graphviz`
- **Windows**: 从 [Graphviz 官方网站](https://graphviz.org/download/) 下载并安装

## 安装依赖

```bash
pip install -r requirements.txt
```

## 使用方法

1. 确保你在项目根目录下
2. 运行脚本：

```bash
python generate_sequence_diagram.py
```

3. 脚本会在 `@diagrams` 目录下生成以下文件：
   - `system_overview.png` - 系统交互概览图
   - `sequence_user_authentication.png` - 用户认证流程序列图
   - `sequence_task_management.png` - 任务管理流程序列图
   - `sequence_project_management.png` - 项目管理流程序列图
   - `sequence_task_comment.png` - 任务评论流程序列图
   - `sequence_file_upload.png` - 文件上传流程序列图
   - `sequence_data.json` - 包含所有解析数据的JSON文件，可用于进一步分析

## 工作原理

脚本通过以下步骤生成序列图：

1. 使用正则表达式分析Java控制器代码，提取API端点和服务调用
2. 分析Vue前端组件，查找对应的API调用和用户交互
3. 根据提取的信息，使用Graphviz生成序列图表示系统组件之间的交互

序列图遵循从左到右的流程，展示了以下组件之间的交互：
- 浏览器（用户界面）
- 前端（Vue.js应用）
- API网关
- 后端控制器
- 服务层
- 数据库

## 自定义

你可以通过修改脚本开头的`SCENARIOS`变量来自定义要生成的序列图场景：

```python
SCENARIOS = [
    {
        "name": "场景名称",
        "title": "场景标题",
        "description": "场景描述",
        "controller": "控制器文件名.java",
        "methods": ["方法1", "方法2"],
        "views": ["视图路径1.vue", "视图路径2.vue"]
    },
    # 更多场景...
]
```

## 已知限制

- 脚本使用正则表达式分析代码，对于非标准格式的代码可能无法正确解析
- 前端视图中的API调用模式识别有限，仅支持几种常见模式
- 序列图是基于静态代码分析生成的，可能无法捕捉到所有动态运行时行为 