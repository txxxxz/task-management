package com.taskManagement.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图对象
 */
@Data
@ApiModel("评论视图对象")
public class CommentVO {
    @ApiModelProperty("评论ID")
    private Long id;
    
    @ApiModelProperty("评论内容")
    private String content;
    
    @ApiModelProperty("任务ID")
    private Long taskId;
    
    @ApiModelProperty("父评论ID，如果是顶级评论则为null")
    private Long parentId;
    
    @ApiModelProperty("创建用户信息")
    private UserVO creator;
    
    @ApiModelProperty("子评论列表")
    private List<CommentVO> children;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
} 