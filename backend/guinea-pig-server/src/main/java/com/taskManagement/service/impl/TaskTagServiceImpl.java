package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskManagement.entity.Tag;
import com.taskManagement.entity.TaskTag;
import com.taskManagement.mapper.TagMapper;
import com.taskManagement.mapper.TaskTagRelMapper;
import com.taskManagement.service.TaskTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务标签关联服务实现类
 */
@Slf4j
@Service
public class TaskTagServiceImpl extends ServiceImpl<TaskTagRelMapper, TaskTag> implements TaskTagService {

    @Autowired
    private TaskTagRelMapper taskTagRelMapper;
    
    @Autowired
    private TagMapper tagMapper;

    @Override
    @Transactional
    public boolean addTaskTag(Long taskId, Long tagId) {
        log.info("添加任务标签关联: taskId={}, tagId={}", taskId, tagId);
        
        // 1. 检查参数
        if (taskId == null || tagId == null) {
            log.error("任务ID或标签ID不能为空");
            return false;
        }
        
        // 2. 检查是否已存在关联
        if (isTaskTagExist(taskId, tagId)) {
            log.info("任务标签关联已存在: taskId={}, tagId={}", taskId, tagId);
            return true;
        }
        
        // 3. 创建并保存关联
        try {
            TaskTag taskTag = new TaskTag();
            taskTag.setTaskId(taskId);
            taskTag.setTagId(tagId);
            taskTag.setCreateTime(LocalDateTime.now());
            
            boolean success = save(taskTag);
            if (success) {
                log.info("成功添加任务标签关联: taskId={}, tagId={}", taskId, tagId);
            } else {
                log.error("添加任务标签关联失败: taskId={}, tagId={}", taskId, tagId);
            }
            return success;
        } catch (Exception e) {
            log.error("添加任务标签关联异常: taskId={}, tagId={}, 错误: {}", taskId, tagId, e.getMessage(), e);
            throw e; // 继续抛出异常，由事务管理
        }
    }

    @Override
    @Transactional
    public int batchAddTaskTags(Long taskId, List<Long> tagIds) {
        log.info("批量添加任务标签关联: taskId={}, tagIds={}", taskId, tagIds);
        
        // 参数校验
        if (taskId == null) {
            log.error("任务ID不能为空");
            return 0;
        }
        
        if (tagIds == null || tagIds.isEmpty()) {
            log.info("标签ID列表为空，无需添加关联");
            return 0;
        }
        
        try {
            // 过滤掉已存在的关联
            List<Long> existingTagIds = getExistingTagIds(taskId);
            List<Long> newTagIds = tagIds.stream()
                    .filter(tagId -> tagId != null && !existingTagIds.contains(tagId))
                    .collect(Collectors.toList());
            
            if (newTagIds.isEmpty()) {
                log.info("没有新的任务标签关联需要添加");
                return 0;
            }
            
            // 批量添加新的关联
            int affectedRows = taskTagRelMapper.batchInsert(taskId, newTagIds);
            log.info("成功批量添加 {} 个任务标签关联", affectedRows);
            return affectedRows;
        } catch (Exception e) {
            log.error("批量添加任务标签关联失败: taskId={}, tagIds={}, 错误: {}", taskId, tagIds, e.getMessage(), e);
            throw e; // 继续抛出异常，由事务管理
        }
    }

    @Override
    @Transactional
    public boolean removeTaskTag(Long taskId, Long tagId) {
        log.info("删除任务标签关联: taskId={}, tagId={}", taskId, tagId);
        
        LambdaQueryWrapper<TaskTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskTag::getTaskId, taskId)
                  .eq(TaskTag::getTagId, tagId);
        
        return remove(queryWrapper);
    }

    @Override
    @Transactional
    public int removeTaskTags(Long taskId) {
        log.info("删除任务的所有标签关联: taskId={}", taskId);
        return taskTagRelMapper.deleteByTaskId(taskId);
    }

    @Override
    @Transactional
    public int removeTagTasks(Long tagId) {
        log.info("删除标签的所有任务关联: tagId={}", tagId);
        return taskTagRelMapper.deleteByTagId(tagId);
    }

    @Override
    public List<Tag> getTagsByTaskId(Long taskId) {
        log.info("获取任务的所有标签: taskId={}", taskId);
        return tagMapper.selectByTaskId(taskId);
    }

    @Override
    public List<Long> getTagIdsByTaskId(Long taskId) {
        log.info("获取任务的所有标签ID: taskId={}", taskId);
        
        LambdaQueryWrapper<TaskTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskTag::getTaskId, taskId)
                  .select(TaskTag::getTagId);
        
        List<TaskTag> taskTags = list(queryWrapper);
        
        return taskTags.stream()
                .map(TaskTag::getTagId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getTaskIdsByTagId(Long tagId) {
        log.info("获取标签关联的所有任务ID: tagId={}", tagId);
        
        LambdaQueryWrapper<TaskTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskTag::getTagId, tagId)
                  .select(TaskTag::getTaskId);
        
        List<TaskTag> taskTags = list(queryWrapper);
        
        return taskTags.stream()
                .map(TaskTag::getTaskId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getTaskIdsByTagIds(List<Long> tagIds) {
        log.info("根据多个标签ID获取关联的所有任务ID: tagIds={}", tagIds);
        
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<TaskTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TaskTag::getTagId, tagIds)
                  .select(TaskTag::getTaskId);
        
        List<TaskTag> taskTags = list(queryWrapper);
        
        // 去重
        return taskTags.stream()
                .map(TaskTag::getTaskId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateTaskTags(Long taskId, List<Long> tagIds) {
        log.info("更新任务的标签关联: taskId={}, tagIds={}", taskId, tagIds);
        
        // 删除任务的所有现有标签关联
        removeTaskTags(taskId);
        
        // 如果有新的标签关联，添加它们
        if (tagIds != null && !tagIds.isEmpty()) {
            batchAddTaskTags(taskId, tagIds);
        }
        
        return true;
    }
    
    /**
     * 检查任务标签关联是否已存在
     * @param taskId 任务ID
     * @param tagId 标签ID
     * @return 是否已存在
     */
    private boolean isTaskTagExist(Long taskId, Long tagId) {
        LambdaQueryWrapper<TaskTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskTag::getTaskId, taskId)
                  .eq(TaskTag::getTagId, tagId);
        
        return count(queryWrapper) > 0;
    }
    
    /**
     * 获取任务已关联的标签ID列表
     * @param taskId 任务ID
     * @return 标签ID列表
     */
    private List<Long> getExistingTagIds(Long taskId) {
        LambdaQueryWrapper<TaskTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskTag::getTaskId, taskId)
                  .select(TaskTag::getTagId);
        
        List<TaskTag> taskTags = list(queryWrapper);
        
        return taskTags.stream()
                .map(TaskTag::getTagId)
                .collect(Collectors.toList());
    }
} 