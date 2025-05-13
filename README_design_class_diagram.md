# Design Class Diagram Generator

This Python script generates a logical design class diagram for the Task Management System. Unlike the class diagram generated from existing code, this diagram represents the conceptual design of the system and the logical relationships between classes, making it suitable for the early stages of system design.

## Features

The generated design class diagram includes:

1. **Domain Entities** - Core business objects such as User, Project, Task, Comment, etc.
2. **Enumerations** - Value types like TaskStatus, Priority, UserRole, etc.
3. **Relationships** - Logical connections between entities with proper cardinality
4. **Service Layer** - Service classes that manage the domain entities
5. **Methods** - Key operations supported by each class

## Prerequisites

1. Python 3.6+
2. Graphviz installed on your system (for diagram rendering)

### Installing Graphviz

- **macOS**: `brew install graphviz`
- **Ubuntu/Debian**: `sudo apt-get install graphviz`
- **Windows**: Download from [Graphviz official website](https://graphviz.org/download/)

## Dependencies

```bash
pip install graphviz
```

## Usage

1. Ensure you're in the project root directory
2. Run the script:

```bash
python generate_design_class_diagram.py
```

3. The script will generate a design class diagram in the `@diagrams` directory:
   - `design_class_diagram.png` - The visual class diagram
   - `design_class_diagram.dot` - The source DOT file (if there's an error generating the PNG)

## Design Structure

The diagram is organized into several logical groups:

### User Management
- User
- Role
- UserStatus
- UserService

### Project Management
- Project
- ProjectStatus
- ProjectMember
- ProjectRole
- ProjectAttachment
- ProjectService

### Task Management
- Task
- TaskStatus
- Priority
- TaskMember
- TaskRole
- TaskAttachment
- TaskService

### Auxiliary Features
- Comment
- Tag
- TaskTag
- Notification
- NotificationType
- CommentService
- TagService
- NotificationService
- FileService

## Relationships

The diagram shows several types of relationships:
- **Association** - Simple references between classes
- **Composition** - Strong ownership relationships
- **Dependency** - Service classes depending on domain entities
- **Enumeration** - Value type relationships

## Customization

You can customize the class diagram by modifying the script:

1. Add or modify classes by adding new `dot.node()` calls
2. Change attributes or methods in the class definitions
3. Add or modify relationships with `dot.edge()` calls
4. Adjust visual properties through `dot.attr()` calls

## Notes

- This diagram represents a logical design and may differ from the actual implementation
- The focus is on showing relationships between classes rather than implementation details
- The diagram aims to be comprehensive while remaining readable 