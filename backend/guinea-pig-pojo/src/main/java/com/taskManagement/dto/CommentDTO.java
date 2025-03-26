package com.taskManagement.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论数据传输对象
 */
@Data
@ApiModel("评论DTO")
public class CommentDTO {

    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("任务ID")
    private Long taskId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("父评论ID，如果是顶级评论则为null")
    private Long parentId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("创建用户ID")
    private Long createUser;

    // 用于展示的额外信息
    @ApiModelProperty("创建用户名")
    private String createUserName;

    @ApiModelProperty("创建用户头像")
    private String createUserAvatar;
    
    // 子评论列表，用于构建评论树
    @ApiModelProperty("子评论列表")
    private java.util.List<CommentDTO> children;
}