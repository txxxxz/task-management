package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskManagement.TestApplication;
import com.taskManagement.entity.Project;
import com.taskManagement.entity.Tag;
import com.taskManagement.entity.Task;
import com.taskManagement.entity.TaskTag;
import com.taskManagement.entity.User;
import com.taskManagement.vo.TaskVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TaskMapper 数据层测试类
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
@Rollback
public class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private TaskTagRelMapper taskTagRelMapper;

    @Autowired
    private UserMapper userMapper;

    private Long projectId;
    private Long userId;
    private Long tagId1;
    private Long tagId2;

    @BeforeEach
    public void setup() {
        // 创建测试用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(1);
        user.setRole(1); // 项目负责人
        userMapper.insert(user);
        userId = user.getId();
        
        // 创建测试项目
        Project project = new Project();
        project.setName("测试项目");
        project.setDescription("测试项目描述");
        project.setStatus(0);
        project.setPriority(2);
        project.setCreateUser(userId);
        project.setUpdateUser(userId);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.insert(project);
        projectId = project.getId();
        
        // 创建测试标签
        Tag tag1 = new Tag();
        tag1.setName("标签1");
        tag1.setColor("#FF0000");
        tag1.setCreateUser(userId);
        tag1.setCreateTime(LocalDateTime.now());
        tagMapper.insert(tag1);
        tagId1 = tag1.getId();
        
        Tag tag2 = new Tag();
        tag2.setName("标签2");
        tag2.setColor("#00FF00");
        tag2.setCreateUser(userId);
        tag2.setCreateTime(LocalDateTime.now());
        tagMapper.insert(tag2);
        tagId2 = tag2.getId();
    }

    /**
     * 创建测试任务
     */
    private Task createTestTask() {
        Task task = new Task();
        task.setName("测试任务");
        task.setDescription("测试任务描述");
        task.setProjectId(projectId);
        task.setStatus(0);
        task.setPriority(2);
        task.setStartTime(LocalDateTime.now());
        task.setDeadline(LocalDateTime.now().plusDays(7));
        task.setCreateUser(userId);
        task.setUpdateUser(userId);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        task.setCommentCount(0);
        return task;
    }

    @Test
    @DisplayName("测试插入任务")
    public void testInsert() {
        // 创建测试任务
        Task task = createTestTask();
        
        // 执行插入
        int result = taskMapper.insert(task);
        
        // 验证结果
        assertEquals(1, result);
        assertNotNull(task.getId());
        
        // 关联标签
        TaskTag taskTag1 = new TaskTag();
        taskTag1.setTaskId(task.getId());
        taskTag1.setTagId(tagId1);
        taskTag1.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag1);
        
        TaskTag taskTag2 = new TaskTag();
        taskTag2.setTaskId(task.getId());
        taskTag2.setTagId(tagId2);
        taskTag2.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag2);
        
        // 验证关联结果
        List<Tag> tags = tagMapper.selectByTaskId(task.getId());
        assertEquals(2, tags.size());
    }

    @Test
    @DisplayName("测试根据ID查询任务")
    public void testSelectById() {
        // 创建测试任务并插入
        Task task = createTestTask();
        taskMapper.insert(task);
        Long taskId = task.getId();
        
        // 关联标签
        TaskTag taskTag1 = new TaskTag();
        taskTag1.setTaskId(taskId);
        taskTag1.setTagId(tagId1);
        taskTag1.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag1);
        
        TaskTag taskTag2 = new TaskTag();
        taskTag2.setTaskId(taskId);
        taskTag2.setTagId(tagId2);
        taskTag2.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag2);
        
        // 查询任务
        Task result = taskMapper.selectById(taskId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("测试任务", result.getName());
        assertEquals(projectId, result.getProjectId());
        
        // 查询关联的标签
        List<Tag> tags = tagMapper.selectByTaskId(taskId);
        assertEquals(2, tags.size());
    }

    @Test
    @DisplayName("测试更新任务")
    public void testUpdate() {
        // 创建测试任务并插入
        Task task = createTestTask();
        taskMapper.insert(task);
        Long taskId = task.getId();
        
        // 更新任务
        task.setName("更新后的任务名称");
        task.setStatus(1); // 修改为进行中
        task.setUpdateTime(LocalDateTime.now());
        
        int result = taskMapper.updateById(task);
        
        // 验证更新结果
        assertEquals(1, result);
        
        // 再次查询
        Task updatedTask = taskMapper.selectById(taskId);
        assertEquals("更新后的任务名称", updatedTask.getName());
        assertEquals(Integer.valueOf(1), updatedTask.getStatus());
    }

    @Test
    @DisplayName("测试删除任务")
    public void testDelete() {
        // 创建测试任务并插入
        Task task = createTestTask();
        taskMapper.insert(task);
        Long taskId = task.getId();
        
        // 关联标签
        TaskTag taskTag1 = new TaskTag();
        taskTag1.setTaskId(taskId);
        taskTag1.setTagId(tagId1);
        taskTag1.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag1);
        
        TaskTag taskTag2 = new TaskTag();
        taskTag2.setTaskId(taskId);
        taskTag2.setTagId(tagId2);
        taskTag2.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag2);
        
        // 删除任务
        int result = taskMapper.deleteById(taskId);
        
        // 验证删除结果
        assertEquals(1, result);
        
        // 再次查询
        Task deletedTask = taskMapper.selectById(taskId);
        assertNull(deletedTask);
        
        // 验证标签关联是否解除（如果有外键级联删除）
        List<TaskTag> taskTags = taskTagRelMapper.selectByTaskId(taskId);
        assertTrue(taskTags.isEmpty());
    }

    @Test
    @DisplayName("测试分页查询任务")
    public void testSelectPage() {
        // 创建5个测试任务并插入
        for (int i = 0; i < 5; i++) {
            Task task = createTestTask();
            task.setName("测试任务" + i);
            task.setStatus(i % 3); // 使用不同状态
            taskMapper.insert(task);
        }
        
        // 创建分页对象
        Page<Task> page = new Page<>(1, 3);
        
        // 创建查询条件：状态为0的任务
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getStatus, 0);
        queryWrapper.eq(Task::getProjectId, projectId);
        
        // 执行分页查询
        IPage<Task> taskPage = taskMapper.selectPage(page, queryWrapper);
        
        // 验证分页结果
        assertNotNull(taskPage);
        assertEquals(3, taskPage.getSize()); // 页大小
        assertEquals(2, taskPage.getRecords().size()); // 实际返回记录数（状态为0的记录数）
        assertEquals(2, taskPage.getTotal()); // 总记录数（满足条件的记录总数）
    }

    @Test
    @DisplayName("测试获取任务详情（包含标签）")
    public void testGetTaskWithDetails() {
        // 创建测试任务并插入
        Task task = createTestTask();
        taskMapper.insert(task);
        Long taskId = task.getId();
        
        // 关联标签
        TaskTag taskTag1 = new TaskTag();
        taskTag1.setTaskId(taskId);
        taskTag1.setTagId(tagId1);
        taskTag1.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag1);
        
        TaskTag taskTag2 = new TaskTag();
        taskTag2.setTaskId(taskId);
        taskTag2.setTagId(tagId2);
        taskTag2.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag2);
        
        // 执行查询
        TaskVO taskVO = taskMapper.getTaskWithDetails(taskId);
        
        // 验证结果
        assertNotNull(taskVO);
        assertEquals(taskId, taskVO.getId());
        assertEquals("测试任务", taskVO.getName());
        assertEquals(projectId, taskVO.getProjectId());
        
        // 验证标签是否正确关联
        assertNotNull(taskVO.getTags());
        assertEquals(2, taskVO.getTags().size());
    }

    @Test
    @DisplayName("测试根据项目ID查询任务")
    public void testSelectByProjectId() {
        // 创建3个测试任务并插入（属于同一项目）
        for (int i = 0; i < 3; i++) {
            Task task = createTestTask();
            task.setName("测试任务" + i);
            taskMapper.insert(task);
        }
        
        // 创建1个属于其他项目的任务
        Task otherProjectTask = createTestTask();
        otherProjectTask.setName("其他项目任务");
        
        // 创建第二个项目
        Project otherProject = new Project();
        otherProject.setName("其他项目");
        otherProject.setDescription("其他项目描述");
        otherProject.setStatus(0);
        otherProject.setPriority(2);
        otherProject.setCreateUser(userId);
        otherProject.setUpdateUser(userId);
        otherProject.setCreateTime(LocalDateTime.now());
        otherProject.setUpdateTime(LocalDateTime.now());
        projectMapper.insert(otherProject);
        
        otherProjectTask.setProjectId(otherProject.getId());
        taskMapper.insert(otherProjectTask);
        
        // 创建查询条件：按项目ID查询
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getProjectId, projectId);
        
        // 执行查询
        List<Task> tasks = taskMapper.selectList(queryWrapper);
        
        // 验证结果：应该只有3个属于测试项目的任务
        assertEquals(3, tasks.size());
        for (Task t : tasks) {
            assertEquals(projectId, t.getProjectId());
        }
    }

    @Test
    @DisplayName("测试任务与标签多对多关系")
    public void testTaskTagRelationship() {
        // 创建2个测试任务
        Task task1 = createTestTask();
        task1.setName("任务1");
        taskMapper.insert(task1);
        
        Task task2 = createTestTask();
        task2.setName("任务2");
        taskMapper.insert(task2);
        
        // 建立多对多关系：任务1关联标签1和标签2，任务2关联标签2
        TaskTag taskTag1 = new TaskTag();
        taskTag1.setTaskId(task1.getId());
        taskTag1.setTagId(tagId1);
        taskTag1.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag1);
        
        TaskTag taskTag2 = new TaskTag();
        taskTag2.setTaskId(task1.getId());
        taskTag2.setTagId(tagId2);
        taskTag2.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag2);
        
        TaskTag taskTag3 = new TaskTag();
        taskTag3.setTaskId(task2.getId());
        taskTag3.setTagId(tagId2);
        taskTag3.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag3);
        
        // 验证任务1关联的标签
        List<Tag> task1Tags = tagMapper.selectByTaskId(task1.getId());
        assertEquals(2, task1Tags.size());
        
        // 验证任务2关联的标签
        List<Tag> task2Tags = tagMapper.selectByTaskId(task2.getId());
        assertEquals(1, task2Tags.size());
        assertEquals(tagId2, task2Tags.get(0).getId());
        
        // 验证标签2关联的任务
        List<TaskTag> tag2Tasks = taskTagRelMapper.selectByTagId(tagId2);
        assertEquals(2, tag2Tasks.size());
        
        // 验证标签1关联的任务
        List<TaskTag> tag1Tasks = taskTagRelMapper.selectByTagId(tagId1);
        assertEquals(1, tag1Tasks.size());
        assertEquals(task1.getId(), tag1Tasks.get(0).getTaskId());
    }

    @Test
    @DisplayName("测试按标签ID查询任务")
    public void testSelectByTagId() {
        // 创建2个测试任务
        Task task1 = createTestTask();
        task1.setName("任务1");
        taskMapper.insert(task1);
        
        Task task2 = createTestTask();
        task2.setName("任务2");
        taskMapper.insert(task2);
        
        // 建立多对多关系
        TaskTag taskTag1 = new TaskTag();
        taskTag1.setTaskId(task1.getId());
        taskTag1.setTagId(tagId1);
        taskTag1.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag1);
        
        TaskTag taskTag2 = new TaskTag();
        taskTag2.setTaskId(task2.getId());
        taskTag2.setTagId(tagId1);
        taskTag2.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag2);
        
        TaskTag taskTag3 = new TaskTag();
        taskTag3.setTaskId(task2.getId());
        taskTag3.setTagId(tagId2);
        taskTag3.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag3);
        
        // 通过关联表查询与标签1关联的任务
        List<TaskTag> taskTags = taskTagRelMapper.selectByTagId(tagId1);
        assertEquals(2, taskTags.size());
        
        // 通过关联表查询与标签2关联的任务
        taskTags = taskTagRelMapper.selectByTagId(tagId2);
        assertEquals(1, taskTags.size());
        assertEquals(task2.getId(), taskTags.get(0).getTaskId());
    }

    @Test
    @DisplayName("测试解除任务与标签的关联")
    public void testRemoveTagFromTask() {
        // 创建测试任务
        Task task = createTestTask();
        taskMapper.insert(task);
        
        // 关联两个标签
        TaskTag taskTag1 = new TaskTag();
        taskTag1.setTaskId(task.getId());
        taskTag1.setTagId(tagId1);
        taskTag1.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag1);
        
        TaskTag taskTag2 = new TaskTag();
        taskTag2.setTaskId(task.getId());
        taskTag2.setTagId(tagId2);
        taskTag2.setCreateTime(LocalDateTime.now());
        taskTagRelMapper.insert(taskTag2);
        
        // 验证关联成功
        List<Tag> tags = tagMapper.selectByTaskId(task.getId());
        assertEquals(2, tags.size());
        
        // 删除一个标签关联
        LambdaQueryWrapper<TaskTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskTag::getTaskId, task.getId());
        queryWrapper.eq(TaskTag::getTagId, tagId1);
        taskTagRelMapper.delete(queryWrapper);
        
        // 验证关联解除
        tags = tagMapper.selectByTaskId(task.getId());
        assertEquals(1, tags.size());
        assertEquals(tagId2, tags.get(0).getId());
    }
} 