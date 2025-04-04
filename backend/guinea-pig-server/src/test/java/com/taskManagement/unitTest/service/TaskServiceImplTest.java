package com.taskManagement.unitTest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskManagement.context.BaseContext;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.entity.Project;
import com.taskManagement.entity.Task;
import com.taskManagement.entity.User;
import com.taskManagement.entity.Tag;
import com.taskManagement.entity.TaskMember;
import com.taskManagement.mapper.ProjectMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.mapper.TaskMemberMapper;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.TaskTagService;
import com.taskManagement.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

/**
 * TaskService 服务层测试类
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskServiceImplTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ProjectMapper projectMapper;
    
    @Mock
    private TaskTagService taskTagService;
    
    @Mock
    private TaskMemberMapper taskMemberMapper;

    @Mock
    private UserMapper userMapper;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_PROJECT_ID = 1L;
    private static final Long TEST_TASK_ID = 1L;

    private MockedStatic<BaseContext> mockedBaseContext;

    @BeforeEach
    public void setup() {
        // 模拟当前用户ID
        mockedBaseContext = Mockito.mockStatic(BaseContext.class);
        mockedBaseContext.when(BaseContext::getCurrentId).thenReturn(TEST_USER_ID);
    }

    @AfterEach
    public void tearDown() {
        if (mockedBaseContext != null) {
            mockedBaseContext.close();
        }
    }

    /**
     * 创建测试任务DTO
     */
    private TaskDTO createTestTaskDTO() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName("测试任务");
        taskDTO.setDescription("测试任务描述");
        taskDTO.setProjectId(TEST_PROJECT_ID);
        taskDTO.setStatus(0); // 0-待处理
        taskDTO.setPriority(2); // 2-中
        taskDTO.setStartTime(LocalDateTime.now());
        taskDTO.setDeadline(LocalDateTime.now().plusDays(7));
        return taskDTO;
    }

    /**
     * 创建测试任务实体
     */
    private Task createTestTask() {
        Task task = new Task();
        task.setId(TEST_TASK_ID);
        task.setName("测试任务");
        task.setDescription("测试任务描述");
        task.setProjectId(TEST_PROJECT_ID);
        task.setStatus(0);
        task.setPriority(2);
        task.setStartTime(LocalDateTime.now());
        task.setDeadline(LocalDateTime.now().plusDays(7));
        task.setCreateUser(TEST_USER_ID);
        task.setUpdateUser(TEST_USER_ID);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        task.setCommentCount(0);
        return task;
    }

    /**
     * 创建测试项目实体
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setId(TEST_PROJECT_ID);
        project.setName("测试项目");
        project.setDescription("测试项目描述");
        project.setStatus(0);
        project.setPriority(2);
        project.setStartTime(LocalDateTime.now());
        project.setEndTime(LocalDateTime.now().plusDays(30));
        project.setCreateUser(TEST_USER_ID);
        project.setUpdateUser(TEST_USER_ID);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        return project;
    }

    @Test
    @DisplayName("测试获取任务列表")
    public void testGetTaskList() {
        // 准备测试数据
        String keyword = "测试";
        Integer status = 0;
        Integer priority = 2;
        Long projectId = TEST_PROJECT_ID;
        Integer page = 1;
        Integer pageSize = 10;
        
        List<Task> taskList = new ArrayList<>();
        taskList.add(createTestTask());
        Page<Task> taskPage = new Page<>(page, pageSize);
        taskPage.setRecords(taskList);
        taskPage.setTotal(1);
        
        // 为TaskTagService模拟行为
        when(taskTagService.getTagIdsByTaskId(any())).thenReturn(Collections.emptyList());
        // 为TaskMemberMapper模拟行为
        when(taskMemberMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // 模拟行为
        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(taskPage);
        
        // 执行测试
        Map<String, Object> result = taskService.getTaskList(keyword, status, priority, projectId, page, pageSize);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("items")).size());
        assertEquals(1L, result.get("total"));
        
        // 验证方法调用
        verify(taskMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    public void testCreateTask() {
        // 准备测试数据
        TaskDTO taskDTO = createTestTaskDTO();
        Task task = createTestTask();
        Project project = createTestProject();
        
        // 模拟依赖行为
        when(projectMapper.selectById(anyLong())).thenReturn(project);
        when(taskMapper.insert(any(Task.class))).thenReturn(1);
        
        // 执行方法
        TaskDTO result = taskService.createTask(taskDTO);
        
        // 验证结果
        assertNotNull(result);
        verify(taskMapper).insert(any(Task.class));
    }

    @Test
    public void testGetTaskDetail() {
        // 准备测试数据
        Long taskId = 1L;
        Task task = createTestTask();
        List<Long> tagIds = new ArrayList<>();
        
        // 模拟依赖行为
        when(taskMapper.selectById(taskId)).thenReturn(task);
        // 只保留必要的模拟
        when(taskTagService.getTagIdsByTaskId(taskId)).thenReturn(tagIds);
        
        // 执行方法
        TaskDTO result = taskService.getTaskDetail(taskId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        assertEquals(task.getName(), result.getName());
        verify(taskMapper).selectById(taskId);
        verify(taskTagService).getTagIdsByTaskId(taskId);
    }

    @Test
    public void testUpdateTask() {
        // 准备测试数据
        Long taskId = 1L;
        TaskDTO taskDTO = createTestTaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setName("更新后的标题");
        Task task = createTestTask();
        
        // 模拟依赖行为
        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);
        when(taskTagService.getTagIdsByTaskId(taskId)).thenReturn(new ArrayList<>());
        
        // 执行方法
        taskService.updateTask(taskId, taskDTO);
        
        // 验证结果
        verify(taskMapper).updateById(any(Task.class));
    }

    @Test
    public void testDeleteTask() {
        // 准备测试数据
        Long taskId = 1L;
        Task task = createTestTask();
        task.setCreateUser(TEST_USER_ID); // 确保当前用户是任务创建者
        
        // 模拟依赖行为
        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(taskMapper.deleteById(taskId)).thenReturn(1);
        when(taskTagService.removeTaskTags(taskId)).thenReturn(1);
        
        // 执行方法
        taskService.deleteTask(taskId);
        
        // 验证结果
        verify(taskMapper).deleteById(taskId);
        verify(taskTagService).removeTaskTags(taskId);
    }
} 