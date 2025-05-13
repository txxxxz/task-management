# 类图生成工具

这个Python脚本用于从任务管理系统项目中自动生成类图。它可以分析Java后端代码和TypeScript前端代码，生成以下类图：

1. 后端实体类图 - 显示所有数据模型
2. 后端控制器类图 - 显示所有API端点
3. 前端类型定义类图 - 显示前端使用的接口和类型
4. 完整系统类图 - 显示所有组件及其关系

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
python generate_class_diagram.py
```

3. 脚本会在 `class_diagrams` 目录下生成以下文件：
   - `backend_entities.png` - 后端实体类图
   - `backend_controllers.png` - 后端控制器类图
   - `frontend_types.png` - 前端类型定义类图
   - `complete_diagram.png` - 完整系统类图
   - `class_data.json` - 包含所有解析数据的JSON文件，可用于进一步分析

## 如何工作

该脚本通过正则表达式分析源代码文件，提取类、字段和方法定义，然后使用Graphviz生成可视化类图。

- 对于Java实体类，它提取类名和字段信息
- 对于Java控制器类，它提取类名、方法名和HTTP映射类型
- 对于TypeScript类型定义，它提取接口和类型定义

## 自定义

如果需要调整脚本行为，可以修改脚本开头的配置变量：

```python
BACKEND_ENTITY_PATH = "backend/guinea-pig-pojo/src/main/java/com/taskManagement/entity"
BACKEND_CONTROLLER_PATH = "backend/guinea-pig-server/src/main/java/com/taskManagement/controller"
FRONTEND_TYPES_PATH = "frontend/src/types"
```

## 已知限制

- 目前脚本无法解析继承关系和复杂泛型
- 对于非标准格式的代码可能无法正确解析
- 只提取了控制器中的HTTP映射方法，不包括其他方法 