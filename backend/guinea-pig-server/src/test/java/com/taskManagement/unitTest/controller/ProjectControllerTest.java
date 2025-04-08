package com.taskManagement.unitTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskManagement.TestApplication;
import com.taskManagement.config.TestConfig;
import com.taskManagement.context.BaseContext;
import com.taskManagement.dto.ProjectDTO;
import com.taskManagement.interceptor.JwtTokenUserInterceptor;
import com.taskManagement.properties.JwtProperties;
import com.taskManagement.result.Result;
import com.taskManagement.service.ProjectService;
import com.taskManagement.service.TaskService;
import com.taskManagement.vo.PageResult;
import com.taskManagement.vo.ProjectVO;
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
import java.util.Collections;
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
 * ProjectController 控制器层测试类
 */
@SpringBootTest(classes = TestApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    
    private MockMvc mockMvc;
    
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TOKEN = "test_token";
    private MockedStatic<BaseContext> mockedBaseContext;
    
    @BeforeEach
    public void setup() throws Exception {
        // 模拟JwtTokenUserInterceptor拦截器，直接返回true，绕过认证
        when(jwtTokenUserInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        
        // 模拟当前用户ID
        mockedBaseContext = Mockito.mockStatic(BaseContext.class);
        mockedBaseContext.when(BaseContext::getCurrentId).thenReturn(TEST_USER_ID);

        // 构建MockMvc，确保注册我们模拟的拦截器
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
     * 创建测试项目DTO
     */
    private ProjectDTO createTestProjectDTO() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("测试项目_" + System.currentTimeMillis());
        projectDTO.setDescription("测试项目描述");
        projectDTO.setStatus(0); // 0-筹备中
        projectDTO.setPriority(2); // 2-中
        projectDTO.setStartTime(LocalDateTime.now());
        projectDTO.setEndTime(LocalDateTime.now().plusDays(30));
        return projectDTO;
    }

    /**
     * 创建测试项目VO
     */
    private ProjectVO createTestProjectVO(Long id) {
        ProjectVO projectVO = new ProjectVO();
        projectVO.setId(id);
        projectVO.setName("测试项目_" + System.currentTimeMillis());
        projectVO.setDescription("测试项目描述");
        projectVO.setStatus(0); // 0-筹备中
        projectVO.setPriority(2); // 2-中
        projectVO.setStartTime(LocalDateTime.now());
        projectVO.setEndTime(LocalDateTime.now().plusDays(30));
        projectVO.setCreateTime(LocalDateTime.now());
        projectVO.setUpdateTime(LocalDateTime.now());
        projectVO.setMemberCount(1);
        projectVO.setIsCreator(true);
        return projectVO;
    }

    @Test
    @DisplayName("测试获取项目列表")
    public void testGetProjectList() throws Exception {
        // 准备测试数据
        String keyword = "测试";
        Integer status = 0;
        Integer page = 1;
        Integer pageSize = 10;
        
        List<ProjectVO> projectVOList = new ArrayList<>();
        projectVOList.add(createTestProjectVO(1L));
        
        PageResult<ProjectVO> pageResult = new PageResult<>(projectVOList, 1);
        
        // 模拟Service行为
        when(projectService.getProjectList(eq(keyword), eq(status), eq(page), eq(pageSize)))
                .thenReturn(pageResult);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/projects")
                .param("keyword", keyword)
                .param("status", String.valueOf(status))
                .param("page", String.valueOf(page))
                .param("pageSize", String.valueOf(pageSize))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].name").exists())
                .andExpect(jsonPath("$.data.total").value(1));
    }
    
    @Test
    @DisplayName("测试获取项目详情")
    public void testGetProjectDetail() throws Exception {
        // 准备测试数据
        Long projectId = 1L;
        ProjectVO projectVO = createTestProjectVO(projectId);
        
        // 模拟Service行为
        when(projectService.getProjectDetail(eq(projectId))).thenReturn(projectVO);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/projects/{id}", projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(projectId))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.description").exists());
    }
    
    @Test
    @DisplayName("测试创建项目")
    public void testCreateProject() throws Exception {
        // 准备测试数据
        ProjectDTO projectDTO = createTestProjectDTO();
        ProjectVO projectVO = createTestProjectVO(1L);
        
        // 模拟Service行为
        when(projectService.createProject(any(ProjectDTO.class))).thenReturn(projectVO);
        
        // 执行测试
        MvcResult result = mockMvc.perform(addAuthHeader(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andReturn();
        
        // 验证返回的JSON
        String responseJson = result.getResponse().getContentAsString();
        Result<ProjectVO> responseResult = objectMapper.readValue(responseJson, 
                objectMapper.getTypeFactory().constructParametricType(Result.class, ProjectVO.class));
        
        assertNotNull(responseResult);
        assertEquals(Integer.valueOf(1), responseResult.getCode());
        assertNotNull(responseResult.getData());
        assertEquals(Long.valueOf(1), responseResult.getData().getId());
    }
    
    @Test
    @DisplayName("测试更新项目")
    public void testUpdateProject() throws Exception {
        // 准备测试数据
        Long projectId = 1L;
        ProjectDTO projectDTO = createTestProjectDTO();
        projectDTO.setId(projectId);
        ProjectVO projectVO = createTestProjectVO(projectId);
        
        // 模拟Service行为
        when(projectService.updateProject(any(ProjectDTO.class))).thenReturn(projectVO);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(put("/projects/{id}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(projectId))
                .andExpect(jsonPath("$.data.name").exists());
    }
    
    @Test
    @DisplayName("测试删除项目")
    public void testDeleteProject() throws Exception {
        // 准备测试数据
        Long projectId = 1L;
        
        // 执行测试
        mockMvc.perform(addAuthHeader(delete("/projects/{id}", projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
    
    @Test
    @DisplayName("测试创建项目参数校验-项目名称为空")
    public void testCreateProjectValidationNameEmpty() throws Exception {
        // 准备测试数据 - 项目名称为空
        ProjectDTO projectDTO = createTestProjectDTO();
        projectDTO.setName(null); // 使用null而不是空字符串
        
        // 执行测试并验证请求参数校验
        mockMvc.perform(addAuthHeader(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO))))
                .andExpect(status().isBadRequest()); // 预期400状态码
    }
    
    @Test
    @DisplayName("测试创建项目参数校验-项目名称过长")
    public void testCreateProjectValidationNameTooLong() throws Exception {
        // 准备测试数据 - 项目名称过长
        ProjectDTO project = createTestProjectDTO();
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            longName.append("a");
        }
        project.setName(longName.toString());
        
        // 模拟service行为，对于过长的名称，测试环境中可能不会触发校验
        when(projectService.createProject(any(ProjectDTO.class))).thenReturn(new ProjectVO());
        
        mockMvc.perform(addAuthHeader(post("/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(project))))
                .andExpect(status().isBadRequest()); // 名称过长应返回400错误
    }
    
    @Test
    @DisplayName("测试获取项目成员")
    public void testGetProjectMembers() throws Exception {
        // 准备测试数据
        Long projectId = 1L;
        List<String> members = Collections.singletonList("testuser");
        
        // 模拟Service行为
        when(projectService.getProjectMembers(eq(projectId))).thenReturn(members);
        
        // 执行测试
        mockMvc.perform(addAuthHeader(get("/projects/{id}/members", projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("testuser"));
    }
    
    @Test
    @DisplayName("测试添加项目成员")
    public void testAddProjectMember() throws Exception {
        // 准备测试数据
        Long projectId = 1L;
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "testuser");
        
        // 执行测试
        mockMvc.perform(addAuthHeader(post("/projects/{id}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
} 