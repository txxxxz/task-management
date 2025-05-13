import os
import graphviz

# Output directory
OUTPUT_DIR = "@diagrams"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Function to create the design class diagram
def generate_design_class_diagram():
    # Create a new directed graph
    dot = graphviz.Digraph('TaskManagementSystem', comment='Logical Design Class Diagram', format='png')
    
    # 设置更高的DPI和大尺寸，以提高分辨率
    dot.attr(rankdir='TB', size='24,36', ratio='fill', dpi='300')
    dot.attr('node', shape='record', fontname='Arial', fontsize='14')
    dot.attr('edge', fontname='Arial', fontsize='10')
    dot.attr('graph', bgcolor='white')
    
    # Define all classes for the system
    
    # User related classes
    dot.node('User', '{User|+ id: Long\l+ username: String\l+ email: String\l+ password: String\l+ role: Role\l+ avatar: String\l+ createTime: DateTime\l+ status: UserStatus\l|+ login()\l+ register()\l+ updateProfile()\l+ changePassword()\l}')
    
    dot.node('Role', '{Role|+ ADMIN\l+ PROJECT_MANAGER\l+ TEAM_MEMBER\l+ GUEST\l|}')
    
    dot.node('UserStatus', '{UserStatus|+ ACTIVE\l+ INACTIVE\l+ SUSPENDED\l|}')
    
    # Project related classes
    dot.node('Project', '{Project|+ id: Long\l+ name: String\l+ description: String\l+ startDate: Date\l+ endDate: Date\l+ status: ProjectStatus\l+ createdBy: User\l+ createTime: DateTime\l|+ addMember()\l+ removeMember()\l+ addTask()\l+ updateStatus()\l}')
    
    dot.node('ProjectStatus', '{ProjectStatus|+ PLANNING\l+ IN_PROGRESS\l+ COMPLETED\l+ CANCELLED\l|}')
    
    dot.node('ProjectMember', '{ProjectMember|+ id: Long\l+ project: Project\l+ user: User\l+ role: ProjectRole\l+ joinTime: DateTime\l|}')
    
    dot.node('ProjectRole', '{ProjectRole|+ OWNER\l+ MANAGER\l+ MEMBER\l+ VIEWER\l|}')
    
    dot.node('ProjectAttachment', '{ProjectAttachment|+ id: Long\l+ project: Project\l+ fileName: String\l+ filePath: String\l+ fileSize: Long\l+ uploadTime: DateTime\l+ uploadedBy: User\l|}')
    
    # Task related classes
    dot.node('Task', '{Task|+ id: Long\l+ name: String\l+ description: String\l+ project: Project\l+ status: TaskStatus\l+ priority: Priority\l+ assignee: User\l+ reporter: User\l+ startTime: DateTime\l+ dueTime: DateTime\l+ createTime: DateTime\l+ estimatedHours: Double\l|+ assignTo()\l+ updateStatus()\l+ addComment()\l+ addAttachment()\l}')
    
    dot.node('TaskStatus', '{TaskStatus|+ PENDING\l+ IN_PROGRESS\l+ COMPLETED\l+ CANCELLED\l|}')
    
    dot.node('Priority', '{Priority|+ HIGH\l+ MEDIUM\l+ LOW\l|}')
    
    dot.node('TaskMember', '{TaskMember|+ id: Long\l+ task: Task\l+ user: User\l+ role: TaskRole\l+ assignTime: DateTime\l|}')
    
    dot.node('TaskRole', '{TaskRole|+ ASSIGNEE\l+ REPORTER\l+ WATCHER\l|}')
    
    dot.node('TaskAttachment', '{TaskAttachment|+ id: Long\l+ task: Task\l+ fileName: String\l+ filePath: String\l+ fileSize: Long\l+ uploadTime: DateTime\l+ uploadedBy: User\l|}')
    
    # Comment related classes
    dot.node('Comment', '{Comment|+ id: Long\l+ task: Task\l+ content: String\l+ author: User\l+ createTime: DateTime\l+ parentComment: Comment\l|+ reply()\l+ edit()\l+ delete()\l}')
    
    # Tag related classes
    dot.node('Tag', '{Tag|+ id: Long\l+ name: String\l+ color: String\l+ description: String\l+ createdBy: User\l+ createTime: DateTime\l|}')
    
    dot.node('TaskTag', '{TaskTag|+ id: Long\l+ task: Task\l+ tag: Tag\l+ addTime: DateTime\l|}')
    
    # Notification related classes
    dot.node('Notification', '{Notification|+ id: Long\l+ type: NotificationType\l+ content: String\l+ targetUser: User\l+ sender: User\l+ relatedTask: Task\l+ relatedProject: Project\l+ createTime: DateTime\l+ isRead: Boolean\l|+ markAsRead()\l}')
    
    dot.node('NotificationType', '{NotificationType|+ TASK_ASSIGNED\l+ TASK_UPDATED\l+ COMMENT_ADDED\l+ PROJECT_INVITE\l+ DEADLINE_APPROACHING\l|}')
    
    # Service classes
    dot.node('UserService', '{UserService||+ login()\l+ register()\l+ getUserById()\l+ updateUserInfo()\l+ changePassword()\l+ uploadAvatar()\l+ getUserList()\l}')
    
    dot.node('ProjectService', '{ProjectService||+ createProject()\l+ getProjectList()\l+ getProjectDetail()\l+ updateProject()\l+ deleteProject()\l+ addProjectMember()\l+ removeProjectMember()\l}')
    
    dot.node('TaskService', '{TaskService||+ createTask()\l+ getTaskList()\l+ getTaskDetail()\l+ updateTask()\l+ deleteTask()\l+ assignTask()\l+ updateTaskStatus()\l}')
    
    dot.node('CommentService', '{CommentService||+ addComment()\l+ getCommentsByTaskId()\l+ updateComment()\l+ deleteComment()\l}')
    
    dot.node('TagService', '{TagService||+ createTag()\l+ getTagList()\l+ updateTag()\l+ deleteTag()\l+ addTaskTag()\l+ removeTaskTag()\l}')
    
    dot.node('NotificationService', '{NotificationService||+ createNotification()\l+ getUserNotifications()\l+ markAsRead()\l+ markAllAsRead()\l}')
    
    dot.node('FileService', '{FileService||+ uploadFile()\l+ downloadFile()\l+ deleteFile()\l}')
    
    # Define relationships between classes
    
    # User relationships
    dot.edge('User', 'Role', label='has')
    dot.edge('User', 'UserStatus', label='has')
    
    # Project relationships
    dot.edge('Project', 'ProjectStatus', label='has')
    dot.edge('Project', 'User', label='created by')
    dot.edge('ProjectMember', 'Project', label='belongs to')
    dot.edge('ProjectMember', 'User', label='refers to')
    dot.edge('ProjectMember', 'ProjectRole', label='has')
    dot.edge('ProjectAttachment', 'Project', label='belongs to')
    dot.edge('ProjectAttachment', 'User', label='uploaded by')
    
    # Task relationships
    dot.edge('Task', 'Project', label='belongs to')
    dot.edge('Task', 'TaskStatus', label='has')
    dot.edge('Task', 'Priority', label='has')
    dot.edge('Task', 'User', label='assigned to', constraint='false')
    dot.edge('TaskMember', 'Task', label='belongs to')
    dot.edge('TaskMember', 'User', label='refers to')
    dot.edge('TaskMember', 'TaskRole', label='has')
    dot.edge('TaskAttachment', 'Task', label='belongs to')
    dot.edge('TaskAttachment', 'User', label='uploaded by')
    
    # Comment relationships
    dot.edge('Comment', 'Task', label='belongs to')
    dot.edge('Comment', 'User', label='authored by')
    dot.edge('Comment', 'Comment', label='child of', constraint='false')
    
    # Tag relationships
    dot.edge('Tag', 'User', label='created by')
    dot.edge('TaskTag', 'Task', label='belongs to')
    dot.edge('TaskTag', 'Tag', label='refers to')
    
    # Notification relationships
    dot.edge('Notification', 'NotificationType', label='has')
    dot.edge('Notification', 'User', label='targets', constraint='false')
    dot.edge('Notification', 'User', label='sent by', constraint='false')
    dot.edge('Notification', 'Task', label='relates to', constraint='false')
    dot.edge('Notification', 'Project', label='relates to', constraint='false')
    
    # Service dependencies
    dot.edge('UserService', 'User', label='manages', style='dashed')
    dot.edge('ProjectService', 'Project', label='manages', style='dashed')
    dot.edge('ProjectService', 'ProjectMember', label='manages', style='dashed')
    dot.edge('TaskService', 'Task', label='manages', style='dashed')
    dot.edge('TaskService', 'TaskMember', label='manages', style='dashed')
    dot.edge('CommentService', 'Comment', label='manages', style='dashed')
    dot.edge('TagService', 'Tag', label='manages', style='dashed')
    dot.edge('TagService', 'TaskTag', label='manages', style='dashed')
    dot.edge('NotificationService', 'Notification', label='manages', style='dashed')
    dot.edge('FileService', 'TaskAttachment', label='manages', style='dashed')
    dot.edge('FileService', 'ProjectAttachment', label='manages', style='dashed')
    
    # Add title
    dot.attr(label='\nTask Management System - Logical Design Class Diagram\n', fontsize='24')
    
    # 生成高分辨率SVG矢量图和PNG位图
    output_file_png = os.path.join(OUTPUT_DIR, 'design_class_diagram')
    
    try:
        # 渲染PNG (300 DPI)
        dot.render(output_file_png, cleanup=True)
        print(f"Design class diagram generated successfully: {output_file_png}.png")
        
        # 生成SVG版本
        output_file_svg = os.path.join(OUTPUT_DIR, 'design_class_diagram_vector')
        dot_svg = graphviz.Digraph('TaskManagementSystem', comment='Logical Design Class Diagram', format='svg')
        # 复制属性
        for key, value in dot.graph_attr.items():
            dot_svg.graph_attr[key] = value
        for key, value in dot.node_attr.items():
            dot_svg.node_attr[key] = value
        for key, value in dot.edge_attr.items():
            dot_svg.edge_attr[key] = value
        # 复制内容
        dot_svg.body = dot.body.copy()
        dot_svg.render(output_file_svg, cleanup=True)
        print(f"Vector design class diagram generated: {output_file_svg}.svg")
        
        # 生成高清版本
        output_file_hd = os.path.join(OUTPUT_DIR, 'design_class_diagram_hd')
        dot_hd = graphviz.Digraph('TaskManagementSystem', comment='Logical Design Class Diagram', format='png')
        # 复制属性
        for key, value in dot.graph_attr.items():
            dot_hd.graph_attr[key] = value
        for key, value in dot.node_attr.items():
            dot_hd.node_attr[key] = value
        for key, value in dot.edge_attr.items():
            dot_hd.edge_attr[key] = value
        # 设置更高的DPI
        dot_hd.graph_attr['dpi'] = '600'
        # 复制内容
        dot_hd.body = dot.body.copy()
        dot_hd.render(output_file_hd, cleanup=True)
        print(f"High resolution design class diagram generated: {output_file_hd}.png")
        
    except Exception as e:
        print(f"Error generating design class diagram: {e}")
        with open(f"{output_file_png}.dot", "w", encoding="utf-8") as f:
            f.write(dot.source)
        print(f"DOT file saved for debugging: {output_file_png}.dot")

if __name__ == "__main__":
    print("Generating design class diagram...")
    generate_design_class_diagram()
    print("Done!") 