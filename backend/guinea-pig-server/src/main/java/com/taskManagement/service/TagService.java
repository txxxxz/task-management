package com.taskManagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taskManagement.entity.Tag;

import java.time.LocalDate;
import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService extends IService<Tag> {
    
    /**
     * 创建标签
     * @param tag 标签信息
     * @return 创建的标签
     */
    Tag createTag(Tag tag);
    
    /**
     * 更新标签
     * @param tag 标签信息
     * @return 更新后的标签
     */
    Tag updateTag(Tag tag);
    
    /**
     * 删除标签
     * @param id 标签ID
     * @return 是否删除成功
     */
    boolean deleteTag(Long id);
    
    /**
     * 根据ID获取标签
     * @param id 标签ID
     * @return 标签信息
     */
    Tag getTagById(Long id);
    
    /**
     * 分页查询标签列表
     * @param page 分页参数
     * @param keyword 关键词
     * @param projectId 项目ID（可选，用于间接过滤与项目关联的任务的标签）
     * @param startTime 创建开始时间
     * @param endTime 创建结束时间
     * @return 分页标签列表
     */
    Page<Tag> getTagList(Page<Tag> page, String keyword, Long projectId, LocalDate startTime, LocalDate endTime);
    
    /**
     * 根据项目ID获取标签列表
     * 注意：Tag与Project没有直接关系，此方法通过查询项目下的Task间接获取关联的Tag
     * @param projectId 项目ID
     * @return 标签列表
     */
    List<Tag> getTagsByProjectId(Long projectId);
    
    /**
     * 根据关键词和项目ID查询标签
     * 注意：Tag与Project没有直接关系，此方法通过查询项目下的Task间接获取关联的Tag
     * @param keyword 关键词
     * @param projectId 项目ID（可选，用于间接过滤与项目关联的任务的标签）
     * @return 标签列表
     */
    List<Tag> searchTags(String keyword, Long projectId);
    
    /**
     * 根据任务ID获取标签列表
     * @param taskId 任务ID
     * @return 标签列表
     */
    List<Tag> getTagsByTaskId(Long taskId);
    
    /**
     * 获取所有标签
     * @return 所有标签列表
     */
    List<Tag> getAllTags();
} 