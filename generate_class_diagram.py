import os
import re
import subprocess
import json
from graphviz import Digraph

# 配置
BACKEND_ENTITY_PATH = "backend/guinea-pig-pojo/src/main/java/com/taskManagement/entity"
BACKEND_CONTROLLER_PATH = "backend/guinea-pig-server/src/main/java/com/taskManagement/controller"
FRONTEND_TYPES_PATH = "frontend/src/types"

# 输出目录配置
OUTPUT_DIR = "@diagrams"

# 创建输出目录
os.makedirs(OUTPUT_DIR, exist_ok=True)

# 解析Java实体类
def parse_java_entity(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 获取类名
    class_name_match = re.search(r'public class (\w+)', content)
    if not class_name_match:
        return None
    
    class_name = class_name_match.group(1)
    
    # 解析字段
    fields = []
    field_pattern = r'private\s+(\w+(?:<\w+>)?)\s+(\w+);'
    for match in re.finditer(field_pattern, content):
        field_type = match.group(1)
        field_name = match.group(2)
        fields.append((field_type, field_name))
    
    return {
        "name": class_name,
        "fields": fields,
        "methods": [],  # 可以扩展以解析方法
        "type": "entity"
    }

# 解析Java控制器类
def parse_java_controller(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 获取类名
    class_name_match = re.search(r'public class (\w+)', content)
    if not class_name_match:
        return None
    
    class_name = class_name_match.group(1)
    
    # 提取方法
    methods = []
    method_pattern = r'@(GetMapping|PostMapping|PutMapping|DeleteMapping).*?\s+public\s+(\w+(?:<[\w<>,\s]+>)?)\s+(\w+)'
    for match in re.finditer(method_pattern, content):
        method_type = match.group(1)
        return_type = match.group(2)
        method_name = match.group(3)
        methods.append((method_type, return_type, method_name))
    
    return {
        "name": class_name,
        "fields": [],  # 控制器通常没有重要字段
        "methods": methods,
        "type": "controller"
    }

# 解析TypeScript类型
def parse_typescript_type(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 查找接口或类型定义
    type_definitions = []
    
    # 提取接口定义
    interface_pattern = r'export interface (\w+)[\s\{]+([^}]+)'
    for match in re.finditer(interface_pattern, content):
        type_name = match.group(1)
        properties_text = match.group(2)
        
        properties = []
        property_pattern = r'(\w+)(?:\?)?:\s*([^;\n]+)'
        for prop_match in re.finditer(property_pattern, properties_text):
            prop_name = prop_match.group(1)
            prop_type = prop_match.group(2).strip()
            properties.append((prop_type, prop_name))
        
        type_definitions.append({
            "name": type_name,
            "fields": properties,
            "methods": [],
            "type": "interface"
        })
    
    # 提取类型定义
    type_pattern = r'export type (\w+) = \{([^}]+)'
    for match in re.finditer(type_pattern, content):
        type_name = match.group(1)
        properties_text = match.group(2)
        
        properties = []
        property_pattern = r'(\w+)(?:\?)?:\s*([^;\n]+)'
        for prop_match in re.finditer(property_pattern, properties_text):
            prop_name = prop_match.group(1)
            prop_type = prop_match.group(2).strip()
            properties.append((prop_type, prop_name))
        
        type_definitions.append({
            "name": type_name,
            "fields": properties,
            "methods": [],
            "type": "type"
        })
    
    return type_definitions

# 使用Graphviz生成类图
def generate_class_diagram(classes, output_name):
    # 构建完整的输出路径
    output_file = os.path.join(OUTPUT_DIR, output_name)
    
    dot = Digraph(comment='Class Diagram', format='png')
    dot.attr('node', shape='record')
    
    # 转义特殊字符的函数
    def escape_html(text):
        return text.replace('<', '\\<').replace('>', '\\>').replace('{', '\\{').replace('}', '\\}')
    
    # 添加类节点
    for cls in classes:
        class_name = cls["name"]
        
        # 构建类标签，显示字段和方法
        label = f"{{{class_name}|"
        
        # 添加字段
        field_parts = []
        for field_type, field_name in cls.get("fields", []):
            escaped_type = escape_html(field_type)
            field_parts.append(f"{field_name}: {escaped_type}")
        
        if field_parts:
            label += "\\n".join(field_parts)
        
        label += "|"
        
        # 添加方法
        method_parts = []
        for method in cls.get("methods", []):
            if len(method) == 3:  # 控制器方法
                http_method, return_type, method_name = method
                escaped_return_type = escape_html(return_type)
                method_parts.append(f"{method_name}(): {escaped_return_type} [{http_method}]")
        
        if method_parts:
            label += "\\n".join(method_parts)
        
        label += "}"
        
        dot.node(class_name, label)
    
    # 寻找可能的关系（基于字段类型与类名的匹配）
    for cls in classes:
        for field_type, _ in cls.get("fields", []):
            # 检查这个字段类型是否与其他类匹配
            base_field_type = field_type.strip("<>").split("<")[0]  # 处理泛型
            
            for target_cls in classes:
                if base_field_type == target_cls["name"]:
                    dot.edge(cls["name"], target_cls["name"])
    
    # 将节点拆分为多个子图，避免单个图过大
    if len(classes) > 15:
        dot.attr(rankdir='TB', ranksep='1.5')
    
    # 设置图像大小和DPI以提高可读性
    dot.attr(size='50,50')
    dot.attr(dpi='300')
    
    try:
        dot.render(output_file, cleanup=True)
        print(f"类图已生成: {output_file}.png")
    except Exception as e:
        print(f"生成类图时出错: {e}")
        # 保存DOT文件以便调试
        with open(f"{output_file}.dot", "w", encoding="utf-8") as f:
            f.write(dot.source)
        print(f"已保存DOT文件: {output_file}.dot")

# 主函数
def main():
    all_classes = []
    
    # 解析后端实体类
    print("解析后端实体类...")
    for file_name in os.listdir(BACKEND_ENTITY_PATH):
        if file_name.endswith(".java"):
            file_path = os.path.join(BACKEND_ENTITY_PATH, file_name)
            entity_class = parse_java_entity(file_path)
            if entity_class:
                all_classes.append(entity_class)
    
    # 解析后端控制器类
    print("解析后端控制器类...")
    for file_name in os.listdir(BACKEND_CONTROLLER_PATH):
        if file_name.endswith(".java"):
            file_path = os.path.join(BACKEND_CONTROLLER_PATH, file_name)
            controller_class = parse_java_controller(file_path)
            if controller_class:
                all_classes.append(controller_class)
    
    # 解析前端类型定义
    print("解析前端类型定义...")
    for file_name in os.listdir(FRONTEND_TYPES_PATH):
        if file_name.endswith(".ts") and not file_name.endswith(".d.ts"):
            file_path = os.path.join(FRONTEND_TYPES_PATH, file_name)
            type_classes = parse_typescript_type(file_path)
            all_classes.extend(type_classes)
    
    # 生成后端实体类图
    backend_entities = [cls for cls in all_classes if cls["type"] == "entity"]
    generate_class_diagram(backend_entities, "backend_entities")
    
    # 生成后端控制器类图
    backend_controllers = [cls for cls in all_classes if cls["type"] == "controller"]
    generate_class_diagram(backend_controllers, "backend_controllers")
    
    # 生成前端接口类图
    frontend_types = [cls for cls in all_classes if cls["type"] in ["interface", "type"]]
    generate_class_diagram(frontend_types, "frontend_types")
    
    # 生成完整系统类图
    generate_class_diagram(all_classes, "complete_diagram")
    
    # 生成JSON输出以便进一步分析
    json_output_path = os.path.join(OUTPUT_DIR, "class_data.json")
    with open(json_output_path, "w", encoding="utf-8") as f:
        json.dump(all_classes, f, indent=2, ensure_ascii=False)
    
    print(f"\n所有类图已生成到 {os.path.abspath(OUTPUT_DIR)} 目录")

if __name__ == "__main__":
    main() 