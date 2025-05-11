package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskManagement.context.BaseContext;
import com.taskManagement.entity.Tag;
import com.taskManagement.entity.Task;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.mapper.TagMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.service.TagService;
import com.taskManagement.service.TaskTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private TaskTagService taskTagService;
    
    @Autowired
    private TaskMapper taskMapper;


    @Override
    @Transactional
    public Tag createTag(Tag tag) {
        log.info("创建标签: {}", tag);
        
        // 设置创建者和更新者
        Long userId = BaseContext.getCurrentId();
        log.info("当前用户ID: {}", userId);
        
        // 如果 userId 为 null，使用默认值
        if (userId == null) {
            log.warn("获取当前用户ID为空，使用默认值1");
            userId = 1L; // 默认使用 ID 为 1 的用户（通常是管理员）
        }
        
        tag.setCreateUser(userId);
        tag.setUpdateUser(userId);
        
        // 如果颜色为空，设置默认颜色
        if (tag.getColor() == null || tag.getColor().isEmpty()) {
            tag.setColor("#409EFF"); // 默认蓝色
        }
        
        // 保存标签
        save(tag);
        
        // 处理任务关联
        if (tag.getTaskIds() != null && !tag.getTaskIds().isEmpty()) {
            taskTagService.batchAddTaskTags(tag.getId(), tag.getTaskIds());
        }
        
        return tag;
    }

    @Override
    @Transactional
    public Tag updateTag(Tag tag) {
        log.info("更新标签: {}", tag);
        
        // 检查标签是否存在
        Tag existingTag = getById(tag.getId());
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 设置更新者
        Long userId = BaseContext.getCurrentId();
        tag.setUpdateUser(userId);
        
        // 更新标签
        updateById(tag);
        
        // 处理任务关联
        if (tag.getTaskIds() != null) {
            taskTagService.updateTaskTags(tag.getId(), tag.getTaskIds());
        }
        
        return tag;
    }

    @Override
    @Transactional
    public boolean deleteTag(Long id) {
        log.info("删除标签: {}", id);
        
        // 检查标签是否存在
        Tag existingTag = getById(id);
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 删除标签和任务的关联关系
        taskTagService.removeTagTasks(id);
        
        // 删除标签
        return removeById(id);
    }

    @Override
    public Tag getTagById(Long id) {
        log.info("根据ID获取标签: {}", id);
        Tag tag = getById(id);
        
        if (tag != null) {
            // 查询与该标签关联的任务ID列表
            List<Long> taskIds = taskTagService.getTaskIdsByTagId(id);
            tag.setTaskIds(taskIds);
        }
        
        return tag;
    }

    @Override
    public List<Tag> getTagsByTaskId(Long taskId) {
        log.info("根据任务ID获取标签列表: {}", taskId);
        
        // 直接从关联表中获取标签ID列表
        List<Long> tagIds = taskTagService.getTagIdsByTaskId(taskId);
        
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据标签ID查询标签信息
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Tag::getId, tagIds);
        
        List<Tag> tags = list(queryWrapper);
        
        // 为每个标签添加关联的任务ID
        for (Tag tag : tags) {
            List<Long> taskIds = taskTagService.getTaskIdsByTagId(tag.getId());
            tag.setTaskIds(taskIds);
        }
        
        return tags;
    }

    @Override
    public Page<Tag> getTagList(Page<Tag> page, String keyword, Long projectId, LocalDate startTime, LocalDate endTime) {
        log.info("分页查询标签列表, keyword={}, projectId={}, startTime={}, endTime={}", keyword, projectId, startTime, endTime);
        
        // 如果有项目ID，先检查是否存在此项目下的标签
        if (projectId != null) {
            // 直接从项目获取标签（通过任务关联）
            List<Tag> projectTags = getTagsByProjectId(projectId);
            // 如果有关键字，进行筛选
            if (StringUtils.isNotBlank(keyword)) {
                projectTags = projectTags.stream()
                        .filter(tag -> tag.getName().contains(keyword))
                        .collect(Collectors.toList());
            }
            
            // 如果有时间参数，进行筛选
            if (startTime != null) {
                projectTags = projectTags.stream()
                        .filter(tag -> {
                            LocalDate createDate = tag.getCreateTime().toLocalDate();
                            return createDate.isEqual(startTime) || createDate.isAfter(startTime);
                        })
                        .collect(Collectors.toList());
            }
            
            if (endTime != null) {
                projectTags = projectTags.stream()
                        .filter(tag -> {
                            LocalDate createDate = tag.getCreateTime().toLocalDate();
                            return createDate.isEqual(endTime) || createDate.isBefore(endTime);
                        })
                        .collect(Collectors.toList());
            }
            
            // 手动分页
            int total = projectTags.size();
            int start = (int) ((page.getCurrent() - 1) * page.getSize());
            int end = Math.min(start + (int) page.getSize(), total);
            
            // 确保起始索引合法
            if (start < total) {
                List<Tag> pageData = projectTags.subList(start, end);
                page.setRecords(pageData);
                page.setTotal(total);
            } else {
                page.setRecords(new ArrayList<>());
                page.setTotal(0);
            }
            
            return page;
        }
        
        // 否则使用通用查询
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加关键字查询条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Tag::getName, keyword);
        }
        
        // 添加时间范围条件
        if (startTime != null) {
            queryWrapper.ge(Tag::getCreateTime, startTime.atStartOfDay());
        }
        
        if (endTime != null) {
            queryWrapper.le(Tag::getCreateTime, endTime.atTime(23, 59, 59));
        }
        
        // 排序
        queryWrapper.orderByDesc(Tag::getUpdateTime);
        
        // 执行查询
        return page(page, queryWrapper);
    }

    @Override
    public List<Tag> getTagsByProjectId(Long projectId) {
        log.info("根据项目ID获取标签列表: {}", projectId);
        
        // 注意：Tag与Project没有直接关系，通过Task间接关联
        // 获取项目下的所有任务
        LambdaQueryWrapper<Task> taskQueryWrapper = new LambdaQueryWrapper<>();
        taskQueryWrapper.eq(Task::getProjectId, projectId);
        taskQueryWrapper.select(Task::getId);
        List<Task> projectTasks = taskMapper.selectList(taskQueryWrapper);
        
        // 提取任务ID列表
        List<Long> taskIds = projectTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        
        if (taskIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查找与这些任务关联的所有标签ID
        Set<Long> tagIdSet = new HashSet<>();
        for (Long taskId : taskIds) {
            List<Long> tagIds = taskTagService.getTagIdsByTaskId(taskId);
            tagIdSet.addAll(tagIds);
        }
        
        if (tagIdSet.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些标签
        LambdaQueryWrapper<Tag> tagQueryWrapper = new LambdaQueryWrapper<>();
        tagQueryWrapper.in(Tag::getId, tagIdSet);
        List<Tag> tags = list(tagQueryWrapper);
        
        // 为每个标签添加关联的任务ID
        for (Tag tag : tags) {
            List<Long> tagTaskIds = taskTagService.getTaskIdsByTagId(tag.getId());
            tag.setTaskIds(tagTaskIds);
        }
        
        return tags;
    }

    @Override
    public List<Tag> searchTags(String keyword, Long projectId) {
        log.info("根据关键词和项目ID查询标签, 关键词: {}, 项目ID: {}", keyword, projectId);
        
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加关键词查询条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Tag::getName, keyword);
        }
        
        List<Tag> tags;
        // 如果指定了项目ID，需要过滤该项目相关的标签
        // 注意：Tag与Project没有直接关系，通过Task间接关联
        if (projectId != null) {
            // 获取项目下的所有任务
            LambdaQueryWrapper<Task> taskQueryWrapper = new LambdaQueryWrapper<>();
            taskQueryWrapper.eq(Task::getProjectId, projectId);
            taskQueryWrapper.select(Task::getId);
            List<Task> projectTasks = taskMapper.selectList(taskQueryWrapper);
            
            // 提取任务ID
            List<Long> taskIds = projectTasks.stream()
                    .map(Task::getId)
                    .collect(Collectors.toList());
            
            if (taskIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 查找与这些任务关联的所有标签ID
            Set<Long> tagIdSet = new HashSet<>();
            for (Long taskId : taskIds) {
                List<Long> tagIds = taskTagService.getTagIdsByTaskId(taskId);
                tagIdSet.addAll(tagIds);
            }
            
            if (tagIdSet.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 添加标签ID过滤条件
            queryWrapper.in(Tag::getId, tagIdSet);
        }
        
        tags = list(queryWrapper);
        
        // 为每个标签添加关联的任务ID
        for (Tag tag : tags) {
            List<Long> taskIds = taskTagService.getTaskIdsByTagId(tag.getId());
            tag.setTaskIds(taskIds);
        }
        
        return tags;
    }

    @Override
    public List<Tag> getAllTags() {
        log.info("获取所有标签");
        
        // 获取所有标签
        List<Tag> tags = list();
        
        // 为每个标签添加关联的任务ID
        for (Tag tag : tags) {
            List<Long> taskIds = taskTagService.getTaskIdsByTagId(tag.getId());
            tag.setTaskIds(taskIds);
        }
        
        log.info("成功获取所有标签，共 {} 个", tags.size());
        return tags;
    }
} 