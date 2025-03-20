package com.taskManagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskManagement.dto.TagDTO;
import com.taskManagement.entity.Tag;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.result.Result;
import com.taskManagement.service.TagService;
import com.taskManagement.service.TaskTagService;
import com.taskManagement.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签控制器
 */
@Slf4j
@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;
    
    @Autowired
    private TaskTagService taskTagService;

    /**
     * 创建标签
     * @param tag 标签信息
     * @return 创建的标签DTO
     */
    @PostMapping
    public Result<TagDTO> createTag(@RequestBody Tag tag) {
        log.info("创建标签: {}", tag);
        Tag createdTag = tagService.createTag(tag);
        return Result.success(TagDTO.fromTag(createdTag));
    }

    /**
     * 更新标签
     * @param id 标签ID
     * @param tag 标签信息
     * @return 更新后的标签DTO
     */
    @PutMapping("/{id}")
    public Result<TagDTO> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        log.info("更新标签: {}, {}", id, tag);
        tag.setId(id);
        Tag updatedTag = tagService.updateTag(tag);
        return Result.success(TagDTO.fromTag(updatedTag));
    }

    /**
     * 删除标签
     * @param id 标签ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTag(@PathVariable Long id) {
        log.info("删除标签: {}", id);
        boolean result = tagService.deleteTag(id);
        return Result.success(result);
    }

    /**
     * 根据ID获取标签
     * @param id 标签ID
     * @return 标签DTO信息
     */
    @GetMapping("/{id}")
    public Result<TagDTO> getTagById(@PathVariable Long id) {
        log.info("根据ID获取标签: {}", id);
        Tag tag = tagService.getTagById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return Result.success(TagDTO.fromTag(tag));
    }

    /**
     * 分页查询标签列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 关键词
     * @param projectId 项目ID（可选，用于间接过滤与项目关联的任务的标签）
     * @return 分页标签DTO列表
     */
    @GetMapping
    public Result<PageResult<TagDTO>> getTagList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId) {
        log.info("分页查询标签列表, 页码: {}, 每页大小: {}, 关键词: {}, 项目ID: {}", page, pageSize, keyword, projectId);
        
        Page<Tag> pageParam = new Page<>(page, pageSize);
        Page<Tag> tagPage = tagService.getTagList(pageParam, keyword, projectId);
        
        // 转换为TagDTO列表
        List<TagDTO> tagDTOs = tagPage.getRecords().stream()
                .map(TagDTO::fromTag)
                .collect(Collectors.toList());
        
        PageResult<TagDTO> pageResult = new PageResult<>(tagDTOs, tagPage.getTotal());
        return Result.success(pageResult);
    }

    /**
     * 根据项目ID获取标签列表
     * 注意：Tag与Project没有直接关系，此方法通过查询项目下的Task间接获取关联的Tag
     * @param projectId 项目ID
     * @return 标签DTO列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<TagDTO>> getTagsByProjectId(@PathVariable Long projectId) {
        log.info("根据项目ID获取标签列表: {}", projectId);
        List<Tag> tags = tagService.getTagsByProjectId(projectId);
        
        // 转换为TagDTO列表
        List<TagDTO> tagDTOs = tags.stream()
                .map(TagDTO::fromTag)
                .collect(Collectors.toList());
        
        return Result.success(tagDTOs);
    }

    /**
     * 根据关键词和项目ID搜索标签
     * 注意：Tag与Project没有直接关系，此方法通过查询项目下的Task间接获取关联的Tag
     * @param keyword 关键词
     * @param projectId 项目ID（可选，用于间接过滤与项目关联的任务的标签）
     * @return 标签DTO列表
     */
    @GetMapping("/search")
    public Result<List<TagDTO>> searchTags(
            @RequestParam String keyword,
            @RequestParam(required = false) Long projectId) {
        log.info("根据关键词和项目ID搜索标签, 关键词: {}, 项目ID: {}", keyword, projectId);
        List<Tag> tags = tagService.searchTags(keyword, projectId);
        
        // 转换为TagDTO列表
        List<TagDTO> tagDTOs = tags.stream()
                .map(TagDTO::fromTag)
                .collect(Collectors.toList());
        
        return Result.success(tagDTOs);
    }
    
    /**
     * 根据任务ID获取标签列表
     * @param taskId 任务ID
     * @return 标签DTO列表
     */
    @GetMapping("/task/{taskId}")
    public Result<List<TagDTO>> getTagsByTaskId(@PathVariable Long taskId) {
        log.info("根据任务ID获取标签列表: {}", taskId);
        List<Tag> tags = taskTagService.getTagsByTaskId(taskId);
        
        // 转换为TagDTO列表
        List<TagDTO> tagDTOs = tags.stream()
                .map(TagDTO::fromTag)
                .collect(Collectors.toList());
        
        return Result.success(tagDTOs);
    }
    
    /**
     * 为任务添加标签
     * @param taskId 任务ID
     * @param tagId 标签ID
     * @return 操作结果
     */
    @PostMapping("/task/{taskId}/tag/{tagId}")
    public Result<Boolean> addTaskTag(
            @PathVariable Long taskId,
            @PathVariable Long tagId) {
        log.info("为任务添加标签, 任务ID: {}, 标签ID: {}", taskId, tagId);
        boolean result = taskTagService.addTaskTag(taskId, tagId);
        return Result.success(result);
    }
    
    /**
     * 移除任务的标签
     * @param taskId 任务ID
     * @param tagId 标签ID
     * @return 操作结果
     */
    @DeleteMapping("/task/{taskId}/tag/{tagId}")
    public Result<Boolean> removeTaskTag(
            @PathVariable Long taskId,
            @PathVariable Long tagId) {
        log.info("移除任务的标签, 任务ID: {}, 标签ID: {}", taskId, tagId);
        boolean result = taskTagService.removeTaskTag(taskId, tagId);
        return Result.success(result);
    }
} 