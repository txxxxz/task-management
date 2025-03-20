package com.taskManagement.dto;

import java.time.LocalDateTime;

import com.taskManagement.entity.Tag;
import lombok.Data;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * 简化版标签DTO类，仅包含必要的标签信息
 */
@Data
public class TagDTO {
    /**
     * 标签ID
     */
    @NotNull(message = "Tag ID cannot be empty")
    private Long id;
    
    /**
     * 标签名称
     */
    @NotNull(message = "Tag name cannot be empty")
    private String name;

    /**
     * 任务ID列表
     */
    private List<Long> taskIds;

    /**
     * 标签描述
     */
    private String description;
    
    /**
     * 标签颜色
     */
    private String color;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /*
     * 创建人
     */
    private Long createUser;

    /**
     * 更新人
     */
    private Long updateUser;

    /**
     * 将Tag实体转换为TagDTO对象
     * @param tag Tag实体
     * @return TagDTO对象，如果输入为null则返回null
     */
    public static TagDTO fromTag(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setName(tag.getName());
        tagDTO.setTaskIds(tag.getTaskIds());
        tagDTO.setDescription(tag.getDescription());
        tagDTO.setColor(tag.getColor());
        tagDTO.setCreateTime(tag.getCreateTime());
        tagDTO.setUpdateTime(tag.getUpdateTime());
        tagDTO.setCreateUser(tag.getCreateUser());
        tagDTO.setUpdateUser(tag.getUpdateUser());
        
        return tagDTO;
    }
}