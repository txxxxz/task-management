package com.taskManagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taskManagement.entity.Tag;
import com.taskManagement.entity.TaskTag;

import java.util.List;

/**
 * 任务标签关联服务接口
 */
public interface TaskTagService extends IService<TaskTag> {
    
    /**
     * 添加任务标签关联
     * @param taskId 任务ID
     * @param tagId 标签ID
     * @return 是否添加成功
     */
    boolean addTaskTag(Long taskId, Long tagId);
    
    /**
     * 批量添加任务标签关联
     * @param taskId 任务ID
     * @param tagIds 标签ID列表
     * @return 添加的数量
     */
    int batchAddTaskTags(Long taskId, List<Long> tagIds);
    
    /**
     * 删除任务标签关联
     * @param taskId 任务ID
     * @param tagId 标签ID
     * @return 是否删除成功
     */
    boolean removeTaskTag(Long taskId, Long tagId);
    
    /**
     * 删除任务的所有标签关联
     * @param taskId 任务ID
     * @return 删除的数量
     */
    int removeTaskTags(Long taskId);
    
    /**
     * 删除标签的所有任务关联
     * @param tagId 标签ID
     * @return 删除的数量
     */
    int removeTagTasks(Long tagId);
    
    /**
     * 获取任务的所有标签
     * @param taskId 任务ID
     * @return 标签列表
     */
    List<Tag> getTagsByTaskId(Long taskId);
    
    /**
     * 获取任务的所有标签ID
     * @param taskId 任务ID
     * @return 标签ID列表
     */
    List<Long> getTagIdsByTaskId(Long taskId);
    
    /**
     * 获取标签关联的所有任务ID
     * @param tagId 标签ID
     * @return 任务ID列表
     */
    List<Long> getTaskIdsByTagId(Long tagId);
    
    /**
     * 更新任务的标签关联
     * @param taskId 任务ID
     * @param tagIds 新的标签ID列表
     * @return 是否更新成功
     */
    boolean updateTaskTags(Long taskId, List<Long> tagIds);
} 