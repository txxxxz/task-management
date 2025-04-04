package com.taskManagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskManagement.TestApplication;
import com.taskManagement.config.TestConfig;
import com.taskManagement.context.BaseContext;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.dto.TaskAttachmentDTO;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.service.CommentService;
import com.taskManagement.service.FileService;
import com.taskManagement.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TaskController 控制器层测试类
 */
@SpringBootTest(classes = TestApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TaskService taskService;
    
    @MockBean
    private CommentService commentService;
    
    @MockBean
    private FileService fileService;
    
    private MockMvc mockMvc;
    
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TOKEN = "test_token";
    private MockedStatic<BaseContext> mockedBaseContext;
    
    @BeforeEach
    public void setup() {
        // 模拟当前用户ID
        mockedBaseContext = Mockito.mockStatic(BaseContext.class);
        mockedBaseContext.when(BaseContext::getCurrentId).thenReturn(TEST_USER_ID);

        // 构建MockMvc
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }
    
    @AfterEach
    public void tearDown() {
        if (mockedBaseContext != null) {
            mockedBaseContext.close();
        }
    }
    
    /**
     * 为请求添加认证信息
     */
    private MockHttpServletRequestBuilder addAuthHeader(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", TEST_TOKEN);
    }

    /**
     * 创建测试任务DTO
     */
    private TaskDTO createTestTaskDTO() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName("测试任务");
        taskDTO.setDescription("测试任务描述");
        taskDTO.setProjectId(1L);
        taskDTO.setStatus(0); // 0-待处理
        taskDTO.setPriority(2); // 2-中
        taskDTO.setStartTime(LocalDateTime.now());
        taskDTO.setDeadline(LocalDateTime.now().plusDays(7));
        taskDTO.setTagIds(Arrays.asList(1L, 2L));
        taskDTO.setMemberIds(Arrays.asList(1L, 2L));
        return taskDTO;
    }

    @Test
    @DisplayName("测试获取任务列表")
    public void testGetTaskList() throws Exception {
        // 准备测试数据
        String keyword = "测试";
        Integer status = 0;
        Integer priority = 2;
        Long projectId = 1L;
        Integer page = 1;
        Integer pageSize = 10;
        
        Map<String, Object> responseMap = new HashMap<>();
        List<TaskDTO> tasks = new ArrayList<>();
        tasks.add(createTestTaskDTO());
        responseMap.put("items", tasks);
        responseMap.put("total", 1);
        
        // 模拟Service行为
        when(taskService.getTaskList(anyString(), anyInt(), anyInt(), anyLong(), anyInt(), anyInt()))
                .thenReturn(responseMap);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/tasks")
                .param("keyword", keyword)
                .param("status", String.valueOf(status))
                .param("priority", String.valueOf(priority))
                .param("projectId", String.valueOf(projectId))
                .param("page", String.valueOf(page))
                .param("pageSize", String.valueOf(pageSize))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
        
        // 验证service方法被正确调用
        verify(taskService).getTaskList(keyword, status, priority, projectId, page, pageSize);
    }
    
    @Test
    @DisplayName("测试获取项目任务列表")
    public void testGetProjectTasks() throws Exception {
        // 准备测试数据
        Long projectId = 1L;
        String keyword = "测试";
        Integer status = 0;
        Integer priority = 2;
        Integer page = 1;
        Integer pageSize = 10;
        
        Map<String, Object> responseMap = new HashMap<>();
        List<TaskDTO> tasks = new ArrayList<>();
        tasks.add(createTestTaskDTO());
        responseMap.put("items", tasks);
        responseMap.put("total", 1);
        
        // 模拟Service行为
        when(taskService.getProjectTasks(anyLong(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(responseMap);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/tasks/project/{projectId}", projectId)
                .param("keyword", keyword)
                .param("status", String.valueOf(status))
                .param("priority", String.valueOf(priority))
                .param("page", String.valueOf(page))
                .param("pageSize", String.valueOf(pageSize))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
        
        // 验证service方法被正确调用
        verify(taskService).getProjectTasks(projectId, keyword, status, priority, page, pageSize);
    }
    
    @Test
    @DisplayName("测试创建任务")
    public void testCreateTask() throws Exception {
        // 准备测试数据
        TaskDTO taskDTO = createTestTaskDTO();
        
        // 模拟Service行为
        when(taskService.createTask(any(TaskDTO.class))).thenReturn(taskDTO);
        
        // 执行测试
        MvcResult result = mockMvc.perform(addAuthHeader(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.name").value(taskDTO.getName()))
                .andReturn();
        
        // 验证service方法被正确调用
        verify(taskService).createTask(any(TaskDTO.class));
    }
    
    @Test
    @DisplayName("测试创建任务-项目不存在")
    public void testCreateTaskWithNonExistingProject() throws Exception {
        // 准备测试数据
        TaskDTO taskDTO = createTestTaskDTO();
        
        // 模拟Service行为 - 抛出项目不存在异常
        when(taskService.createTask(any(TaskDTO.class)))
                .thenThrow(new BusinessException("项目不存在"));
        
        // 执行测试
        mockMvc.perform(addAuthHeader(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("项目不存在"));
        
        // 验证service方法被正确调用
        verify(taskService).createTask(any(TaskDTO.class));
    }
    
    @Test
    @DisplayName("测试获取任务详情")
    public void testGetTaskDetail() throws Exception {
        // 准备测试数据
        Long taskId = 1L;
        TaskDTO taskDTO = createTestTaskDTO();
        taskDTO.setId(taskId);
        
        // 模拟Service行为
        when(taskService.getTaskDetail(anyLong())).thenReturn(taskDTO);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/tasks/{id}", taskId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.name").value(taskDTO.getName()));
        
        // 验证service方法被正确调用
        verify(taskService).getTaskDetail(taskId);
    }
    
    @Test
    @DisplayName("测试获取不存在的任务详情")
    public void testGetNonExistingTaskDetail() throws Exception {
        // 准备测试数据
        Long taskId = 999L;
        
        // 模拟Service行为 - 抛出任务不存在异常
        when(taskService.getTaskDetail(anyLong()))
                .thenThrow(new BusinessException("任务不存在"));
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/tasks/{id}", taskId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("任务不存在"));
        
        // 验证service方法被正确调用
        verify(taskService).getTaskDetail(taskId);
    }
    
    @Test
    @DisplayName("测试更新任务")
    public void testUpdateTask() throws Exception {
        // 准备测试数据
        Long taskId = 1L;
        TaskDTO taskDTO = createTestTaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setName("更新后的任务名称");
        
        // 模拟Service行为
        when(taskService.updateTask(anyLong(), any(TaskDTO.class))).thenReturn(taskDTO);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(put("/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.name").value("更新后的任务名称"));
        
        // 验证service方法被正确调用
        verify(taskService).updateTask(eq(taskId), any(TaskDTO.class));
    }
    
    @Test
    @DisplayName("测试更新不存在的任务")
    public void testUpdateNonExistingTask() throws Exception {
        // 准备测试数据
        Long taskId = 999L;
        TaskDTO taskDTO = createTestTaskDTO();
        taskDTO.setId(taskId);
        
        // 模拟Service行为 - 抛出任务不存在异常
        when(taskService.updateTask(anyLong(), any(TaskDTO.class)))
                .thenThrow(new BusinessException("任务不存在"));
        
        // 执行测试
        mockMvc.perform(addAuthHeader(put("/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("任务不存在"));
        
        // 验证service方法被正确调用
        verify(taskService).updateTask(eq(taskId), any(TaskDTO.class));
    }
    
    @Test
    @DisplayName("测试删除任务")
    public void testDeleteTask() throws Exception {
        // 准备测试数据
        Long taskId = 1L;
        
        // 模拟Service行为 - 不抛出异常表示删除成功
        doNothing().when(taskService).deleteTask(anyLong());
        
        // 执行测试
        mockMvc.perform(addAuthHeader(delete("/tasks/{id}", taskId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("删除成功"));
        
        // 验证service方法被正确调用
        verify(taskService).deleteTask(taskId);
    }
    
    @Test
    @DisplayName("测试删除不存在的任务")
    public void testDeleteNonExistingTask() throws Exception {
        // 准备测试数据
        Long taskId = 999L;
        
        // 模拟Service行为 - 抛出任务不存在异常
        doThrow(new BusinessException("任务不存在")).when(taskService).deleteTask(anyLong());
        
        // 执行测试
        mockMvc.perform(addAuthHeader(delete("/tasks/{id}", taskId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("任务不存在"));
        
        // 验证service方法被正确调用
        verify(taskService).deleteTask(taskId);
    }
    
    @Test
    @DisplayName("测试参数校验-任务名称为空")
    public void testCreateTaskValidationNameEmpty() throws Exception {
        // 准备测试数据 - 任务名称为空
        TaskDTO taskDTO = createTestTaskDTO();
        taskDTO.setName(null);
        
        // 执行测试并验证请求参数校验
        mockMvc.perform(addAuthHeader(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO))))
                .andExpect(status().isBadRequest()); // 预期400状态码
    }
    
    @Test
    @DisplayName("测试参数校验-项目ID为空")
    public void testCreateTaskValidationProjectIdEmpty() throws Exception {
        // 准备测试数据 - 项目ID为空
        TaskDTO taskDTO = createTestTaskDTO();
        taskDTO.setProjectId(null);
        
        // 执行测试并验证请求参数校验
        mockMvc.perform(addAuthHeader(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO))))
                .andExpect(status().isBadRequest()); // 预期400状态码
    }
    
    @Test
    @DisplayName("测试任务统计")
    public void testGetTaskStats() throws Exception {
        // 准备测试数据
        Long projectId = 1L;
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("pending", 5);
        statsMap.put("inProgress", 3);
        statsMap.put("completed", 2);
        
        // 模拟Service行为
        when(taskService.getTaskStats(anyLong())).thenReturn(statsMap);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/tasks/stats")
                .param("projectId", String.valueOf(projectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.pending").value(5))
                .andExpect(jsonPath("$.data.inProgress").value(3))
                .andExpect(jsonPath("$.data.completed").value(2));
        
        // 验证service方法被正确调用
        verify(taskService).getTaskStats(projectId);
    }
} 