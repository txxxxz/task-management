package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskManagement.entity.Task;
import com.taskManagement.vo.TaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * task mapper interface
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
    /**
     * count tasks by conditions
     * @param projectId project ID (optional)
     * @param status status (optional)
     * @return task count
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_task WHERE 1=1" +
            "<if test='projectId != null'> AND project_id = #{projectId}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    Long countTasks(@Param("projectId") Long projectId, @Param("status") Integer status);
    
    /**
     * 统计项目中每个状态的任务数量
     * @param projectId 项目ID
     * @return 每个状态的任务数量
     */
    @Select("SELECT status, COUNT(*) as count FROM tb_task " +
            "WHERE project_id = #{projectId} " +
            "GROUP BY status")
    List<Map<String, Object>> countTasksByStatus(@Param("projectId") Long projectId);
    
    /**
     * 查询任务详情（包含项目信息和标签信息）
     * @param taskId 任务ID
     * @return 任务详情视图对象
     */
    TaskVO getTaskWithDetails(@Param("taskId") Long taskId);
    
    /**
     * 分页查询任务列表（包含项目信息和标签信息）
     * @param page 分页参数
     * @param projectId 项目ID（可选）
     * @param keyword 关键词（可选）
     * @param status 状态（可选）
     * @param priority 优先级（可选）
     * @return 分页任务列表
     */
    IPage<TaskVO> getTaskPageWithDetails(Page<TaskVO> page,
                                        @Param("projectId") Long projectId,
                                        @Param("keyword") String keyword,
                                        @Param("status") Integer status,
                                        @Param("priority") Integer priority);
} 