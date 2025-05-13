from graphviz import Digraph

def create_class_diagram():
    dot = Digraph('Class Diagram for Task Management System', format='png')
    dot.attr(rankdir='BT')

    # Define classes
    classes = [
        ('User', ['user_id', 'email', 'password', 'role']),
        ('Admin', ['manage_users()', 'configure_settings()']),
        ('TeamLeader', ['create_project()', 'assign_tasks()', 'manage_team()']),
        ('Member', ['complete_task()', 'comment_task()']),
        ('Task', ['task_id', 'title', 'description', 'due_date', 'priority', 'status']),
        ('Project', ['project_id', 'name', 'description', 'start_date', 'end_date']),
        ('Comment', ['comment_id', 'content', 'timestamp']),
        ('Tag', ['tag_id', 'name']),
        ('Attachment', ['attachment_id', 'filename', 'upload_date']),
        ('Notification', ['notification_id', 'message', 'timestamp']),
    ]

    # Add class nodes
    for classname, attributes in classes:
        label = f'{classname}|'+ '\l'.join(attributes) + '\l'
        dot.node(classname, label=label, shape='record', style='filled', fillcolor='lightgray')

    # Define relationships
    relations = [
        ('Admin', 'User', 'inherits'),
        ('TeamLeader', 'User', 'inherits'),
        ('Member', 'User', 'inherits'),
        ('Task', 'User', 'assigned to'),
        ('Task', 'Project', 'belongs to'),
        ('Comment', 'Task', 'belongs to'),
        ('Comment', 'User', 'created by'),
        ('Attachment', 'Task', 'attached to'),
        ('Tag', 'Task', 'assigned to'),
        ('Notification', 'User', 'sent to'),
        ('Project', 'User', 'managed by'),
    ]

    # Add relationships
    for source, target, relation in relations:
        dot.edge(source, target, label=relation)

    # Render diagram
    dot.render('task_management_class_diagram', view=True)

# Generate the diagram
create_class_diagram()
