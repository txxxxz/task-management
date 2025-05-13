import os
import re
import json
from graphviz import Digraph

# 配置
BACKEND_CONTROLLER_PATH = "backend/guinea-pig-server/src/main/java/com/taskManagement/controller"
FRONTEND_VIEWS_PATH = "frontend/src/views"
OUTPUT_DIR = "@diagrams"

# 确保输出目录存在
os.makedirs(OUTPUT_DIR, exist_ok=True)

# 定义主要场景
SCENARIOS = [
    {
        "name": "user_authentication",
        "title": "用户认证流程",
        "description": "包括用户登录、注册和获取当前用户信息的流程",
        "controller": "UserController.java",
        "methods": ["login", "register", "getCurrentUser", "logout"],
        "views": ["auth/login.vue", "auth/register.vue"]
    },
    {
        "name": "task_management",
        "title": "任务管理流程",
        "description": "包括创建、查看、更新和删除任务的流程",
        "controller": "TaskController.java",
        "methods": ["createTask", "getTaskList", "getTaskDetail", "updateTask", "deleteTask"],
        "views": ["task/list.vue", "task/detail.vue"]
    },
    {
        "name": "project_management",
        "title": "项目管理流程",
        "description": "包括创建、查看、更新和删除项目的流程",
        "controller": "ProjectController.java",
        "methods": ["createProject", "getProjectList", "getProjectDetail", "updateProject", "deleteProject"],
        "views": ["project/list.vue", "project/detail.vue"]
    },
    {
        "name": "task_comment",
        "title": "任务评论流程",
        "description": "包括添加、查看和删除任务评论的流程",
        "controller": "TaskController.java",
        "methods": ["getTaskComments", "createComment", "deleteComment"],
        "views": ["task/detail.vue"]
    },
    {
        "name": "file_upload",
        "title": "文件上传流程",
        "description": "包括上传任务附件和项目附件的流程",
        "controller": "FileController.java",
        "methods": ["uploadFile", "uploadTaskFile", "uploadProjectFile"],
        "views": ["task/detail.vue", "project/detail.vue"]
    }
]

def find_controller_methods(controller_path, method_names):
    """解析控制器文件，提取指定方法的信息"""
    methods_info = []
    
    try:
        with open(controller_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # 查找类名
        class_name_match = re.search(r'public class (\w+)', content)
        if not class_name_match:
            return methods_info
        
        class_name = class_name_match.group(1)
        
        # 查找 @RequestMapping 路径
        base_path = ""
        base_path_match = re.search(r'@RequestMapping\("([^"]+)"\)', content)
        if base_path_match:
            base_path = base_path_match.group(1)
        
        # 对每个方法名进行查找
        for method_name in method_names:
            # 正则表达式匹配方法定义
            pattern = r'@(GetMapping|PostMapping|PutMapping|DeleteMapping)(?:\("([^"]*)"\))?\s+(?:public|private)\s+\w+(?:<[^>]*>)?\s+' + method_name + r'\s*\([^)]*\)'
            match = re.search(pattern, content)
            
            if match:
                http_method = match.group(1)
                sub_path = match.group(2) if match.group(2) else ""
                
                # 提取方法参数
                method_start = match.end()
                open_braces = 1
                method_end = method_start
                
                for i in range(method_start, len(content)):
                    if content[i] == '{':
                        open_braces += 1
                    elif content[i] == '}':
                        open_braces -= 1
                        if open_braces == 0:
                            method_end = i
                            break
                
                method_body = content[method_start:method_end]
                
                # 查找调用的服务方法
                service_calls = re.findall(r'(\w+)Service\.(\w+)\(', method_body)
                
                methods_info.append({
                    "className": class_name,
                    "methodName": method_name,
                    "httpMethod": http_method,
                    "path": base_path + ("/" + sub_path if sub_path else ""),
                    "serviceCalls": service_calls
                })
    
    except Exception as e:
        print(f"解析控制器时出错: {e}")
    
    return methods_info

def find_view_methods(view_path, controller_methods):
    """解析前端视图文件，提取与控制器方法对应的API调用"""
    api_calls = []
    
    try:
        if os.path.exists(view_path):
            with open(view_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 查找API调用
            for method in controller_methods:
                http_method = method["httpMethod"].replace("Mapping", "").lower()
                path = method["path"]
                
                # 适配不同的API调用模式
                patterns = [
                    rf'(\w+)\.{http_method}\s*\(\s*[\'"](?:{path}|api{path})[\'"]',  # axios.get('/tasks')
                    rf'api\.{http_method}\s*\(\s*[\'"](?:{path}|{path[1:]})[\'"]',   # api.get('tasks')
                    rf'request\.{http_method}\s*\(\s*[\'"](?:{path}|api{path})[\'"]', # request.get('/tasks')
                ]
                
                for pattern in patterns:
                    matches = re.findall(pattern, content)
                    if matches:
                        for match in matches:
                            api_calls.append({
                                "viewFile": os.path.basename(view_path),
                                "apiClient": match,
                                "httpMethod": http_method,
                                "path": path,
                                "controllerMethod": method["methodName"]
                            })
    
    except Exception as e:
        print(f"解析视图文件时出错: {e}")
    
    return api_calls

def generate_sequence_diagram(scenario, methods_info, api_calls):
    """为指定场景生成序列图"""
    dot = Digraph(comment=f'Sequence Diagram: {scenario["title"]}', format='png')
    dot.attr(rankdir='LR')
    
    # 设置字体和样式
    dot.attr('node', fontname='Arial', fontsize='12')
    dot.attr('edge', fontname='Arial', fontsize='10')
    
    # 添加参与者节点
    dot.node('Browser', 'Browser', shape='box')
    dot.node('Frontend', 'Frontend\n(Vue.js)', shape='box')
    dot.node('API', 'API Gateway', shape='box')
    dot.node('Controller', 'Controller', shape='box')
    dot.node('Service', 'Service', shape='box')
    dot.node('Database', 'Database', shape='cylinder')
    
    # 定义节点顺序
    dot.edge('Browser', 'Frontend', style='invis')
    dot.edge('Frontend', 'API', style='invis')
    dot.edge('API', 'Controller', style='invis')
    dot.edge('Controller', 'Service', style='invis')
    dot.edge('Service', 'Database', style='invis')
    
    # 添加交互流程
    edge_count = 0
    
    for method in methods_info:
        # 查找对应的API调用
        matching_api_calls = [call for call in api_calls 
                             if call["controllerMethod"] == method["methodName"]]
        
        if matching_api_calls:
            for api_call in matching_api_calls:
                # 用户到前端
                edge_count += 1
                dot.edge('Browser', 'Frontend', 
                        label=f'{edge_count}. User action in {api_call["viewFile"]}', 
                        fontcolor='blue')
                
                # 前端到API
                edge_count += 1
                dot.edge('Frontend', 'API', 
                        label=f'{edge_count}. {api_call["httpMethod"].upper()} {api_call["path"]}', 
                        fontcolor='green')
                
                # API到控制器
                edge_count += 1
                dot.edge('API', 'Controller', 
                        label=f'{edge_count}. {method["className"]}.{method["methodName"]}()', 
                        fontcolor='purple')
                
                # 控制器到服务
                for service_call in method["serviceCalls"]:
                    edge_count += 1
                    service_name, service_method = service_call
                    dot.edge('Controller', 'Service', 
                            label=f'{edge_count}. {service_name}Service.{service_method}()', 
                            fontcolor='brown')
                    
                    # 服务到数据库
                    edge_count += 1
                    dot.edge('Service', 'Database', 
                            label=f'{edge_count}. DB operations', 
                            fontcolor='black')
                    
                    # 数据库到服务
                    edge_count += 1
                    dot.edge('Database', 'Service', 
                            label=f'{edge_count}. Return data', 
                            fontcolor='black', constraint='false')
                
                # 服务到控制器
                edge_count += 1
                dot.edge('Service', 'Controller', 
                        label=f'{edge_count}. Return result', 
                        fontcolor='brown', constraint='false')
                
                # 控制器到API
                edge_count += 1
                dot.edge('Controller', 'API', 
                        label=f'{edge_count}. JSON response', 
                        fontcolor='purple', constraint='false')
                
                # API到前端
                edge_count += 1
                dot.edge('API', 'Frontend', 
                        label=f'{edge_count}. HTTP response', 
                        fontcolor='green', constraint='false')
                
                # 前端到用户
                edge_count += 1
                dot.edge('Frontend', 'Browser', 
                        label=f'{edge_count}. Update UI', 
                        fontcolor='blue', constraint='false')
        else:
            # 仅有后端流程的情况
            # API到控制器
            edge_count += 1
            dot.edge('API', 'Controller', 
                    label=f'{edge_count}. {method["className"]}.{method["methodName"]}()', 
                    fontcolor='purple')
            
            # 控制器到服务
            for service_call in method["serviceCalls"]:
                edge_count += 1
                service_name, service_method = service_call
                dot.edge('Controller', 'Service', 
                        label=f'{edge_count}. {service_name}Service.{service_method}()', 
                        fontcolor='brown')
                
                # 服务到数据库
                edge_count += 1
                dot.edge('Service', 'Database', 
                        label=f'{edge_count}. DB operations', 
                        fontcolor='black')
                
                # 数据库到服务
                edge_count += 1
                dot.edge('Database', 'Service', 
                        label=f'{edge_count}. Return data', 
                        fontcolor='black', constraint='false')
            
            # 服务到控制器
            edge_count += 1
            dot.edge('Service', 'Controller', 
                    label=f'{edge_count}. Return result', 
                    fontcolor='brown', constraint='false')
            
            # 控制器到API
            edge_count += 1
            dot.edge('Controller', 'API', 
                    label=f'{edge_count}. JSON response', 
                    fontcolor='purple', constraint='false')
    
    # 设置图表属性
    dot.attr(label=f'\n\n{scenario["title"]}\n{scenario["description"]}')
    dot.attr(fontsize='14')
    
    # 保存图表
    output_file = os.path.join(OUTPUT_DIR, f'sequence_{scenario["name"]}')
    try:
        dot.render(output_file, cleanup=True)
        print(f"序列图已生成: {output_file}.png")
    except Exception as e:
        print(f"生成序列图时出错: {e}")
        # 保存DOT文件以便调试
        with open(f"{output_file}.dot", "w", encoding="utf-8") as f:
            f.write(dot.source)
        print(f"已保存DOT文件: {output_file}.dot")

def generate_all_diagrams():
    """生成所有场景的序列图"""
    results = []
    
    for scenario in SCENARIOS:
        print(f"\n处理场景: {scenario['title']}")
        
        # 构建控制器文件路径
        controller_path = os.path.join(BACKEND_CONTROLLER_PATH, scenario["controller"])
        
        # 解析控制器方法
        methods_info = find_controller_methods(controller_path, scenario["methods"])
        print(f"找到 {len(methods_info)} 个控制器方法")
        
        # 分析前端视图API调用
        api_calls = []
        for view in scenario["views"]:
            view_path = os.path.join(FRONTEND_VIEWS_PATH, view)
            api_calls.extend(find_view_methods(view_path, methods_info))
        
        print(f"找到 {len(api_calls)} 个API调用")
        
        # 生成序列图
        generate_sequence_diagram(scenario, methods_info, api_calls)
        
        # 保存结果
        results.append({
            "scenario": scenario["name"],
            "title": scenario["title"],
            "methods": methods_info,
            "apiCalls": api_calls,
            "diagramPath": f'{OUTPUT_DIR}/sequence_{scenario["name"]}.png'
        })
    
    # 保存解析结果为JSON
    with open(os.path.join(OUTPUT_DIR, "sequence_data.json"), "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    print(f"\n所有序列图已生成到 {os.path.abspath(OUTPUT_DIR)} 目录")

def generate_overview_diagram():
    """生成总体系统交互概览图"""
    dot = Digraph(comment='System Interaction Overview', format='png')
    dot.attr(rankdir='TB')
    
    # 设置字体和样式
    dot.attr('node', fontname='Arial', fontsize='12')
    dot.attr('edge', fontname='Arial', fontsize='10')
    
    # 添加主要组件
    dot.node('Browser', 'Web Browser', shape='box')
    dot.node('Frontend', 'Frontend\n(Vue.js)', shape='box')
    dot.node('API', 'API Gateway', shape='box')
    
    # 添加后端组件
    controller_nodes = set()
    for scenario in SCENARIOS:
        controller_name = scenario["controller"].replace(".java", "")
        controller_nodes.add(controller_name)
        dot.node(controller_name, controller_name, shape='box')
    
    dot.node('Service', 'Services Layer', shape='box')
    dot.node('Database', 'Database', shape='cylinder')
    
    # 添加基本流程
    dot.edge('Browser', 'Frontend', label='HTTP/HTTPS')
    dot.edge('Frontend', 'API', label='HTTP/HTTPS\nREST API')
    
    # 连接API到各控制器
    for controller in controller_nodes:
        dot.edge('API', controller)
    
    # 连接控制器到服务层
    for controller in controller_nodes:
        dot.edge(controller, 'Service')
    
    # 连接服务层到数据库
    dot.edge('Service', 'Database', label='JDBC')
    
    # 设置图表属性
    dot.attr(label='\n\n系统交互概览图\n展示了主要系统组件之间的交互')
    dot.attr(fontsize='14')
    
    # 保存图表
    output_file = os.path.join(OUTPUT_DIR, 'system_overview')
    try:
        dot.render(output_file, cleanup=True)
        print(f"系统概览图已生成: {output_file}.png")
    except Exception as e:
        print(f"生成系统概览图时出错: {e}")
        with open(f"{output_file}.dot", "w", encoding="utf-8") as f:
            f.write(dot.source)
        print(f"已保存DOT文件: {output_file}.dot")

if __name__ == "__main__":
    print("开始生成序列图...")
    generate_overview_diagram()
    generate_all_diagrams()
    print("序列图生成完成！") 