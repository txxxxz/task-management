from graphviz import Digraph

def create_use_case_diagram():
    dot = Digraph(comment='Task Management System Use Case Diagram', format='png')

    # Actors
    dot.node('A1', 'Admin', shape='ellipse', style='filled', color='lightblue')
    dot.node('A2', 'Team Leader', shape='ellipse', style='filled', color='lightgreen')
    dot.node('A3', 'Member', shape='ellipse', style='filled', color='lightyellow')

    # Use Cases
    use_cases = [
        ('UC1', 'User Management'),
        ('UC2', 'Task Management'),
        ('UC3', 'Project Management'),
        ('UC4', 'Collaboration & Communication'),
        ('UC5', 'Visualization & Reporting'),
        ('UC6', 'File Management'),
        ('UC7', 'Authentication & Authorization'),
        ('UC8', 'System Settings'),
        ('UC9', 'Notifications'),
    ]

    for uc_id, uc_name in use_cases:
        dot.node(uc_id, uc_name, shape='oval')

    # Associations
    associations = [
        ('A1', 'UC1'), ('A1', 'UC7'), ('A1', 'UC8'), ('A1', 'UC6'),
        ('A2', 'UC2'), ('A2', 'UC3'), ('A2', 'UC4'), ('A2', 'UC5'), ('A2', 'UC9'),
        ('A3', 'UC2'), ('A3', 'UC4'), ('A3', 'UC6'), ('A3', 'UC9'),
    ]

    for actor, uc in associations:
        dot.edge(actor, uc)

    # Render diagram
    dot.render('task_management_use_case_diagram', view=True)

# Generate diagram
create_use_case_diagram()
