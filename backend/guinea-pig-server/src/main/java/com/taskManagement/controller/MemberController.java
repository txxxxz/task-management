package com.taskManagement.controller;

import com.taskManagement.entity.User;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.result.Result;
import com.taskManagement.vo.PageResult;
import com.taskManagement.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成员管理相关接口
 */
@RestController
@RequestMapping("/members")
@Slf4j
public class MemberController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取成员列表，可按角色过滤
     * @param keyword 关键字（用户名、邮箱）
     * @param role 角色（可选，多个角色用逗号分隔，如"0,1"）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 成员列表
     */
    @GetMapping
    public Result<PageResult<UserVO>> getMemberList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取成员列表，keyword={}, role={}, page={}, pageSize={}", keyword, role, page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        
        // 角色过滤
        if (StringUtils.isNotBlank(role)) {
            String[] roles = role.split(",");
            List<Integer> roleList = new ArrayList<>();
            for (String r : roles) {
                try {
                    roleList.add(Integer.parseInt(r.trim()));
                } catch (NumberFormatException e) {
                    log.warn("无效的角色值: {}", r);
                }
            }
            if (!roleList.isEmpty()) {
                queryWrapper.in(User::getRole, roleList);
            }
        }
        
        // 默认只查询角色为0(成员)和1(领导)的用户
        else {
            queryWrapper.in(User::getRole, 0, 1);
        }
        
        // 只查询启用状态的用户
        queryWrapper.eq(User::getStatus, 1);
        
        // 按创建时间排序
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 分页参数处理
        int pageIndex = Math.max(0, page - 1);
        Page<User> pageParam = new Page<>(pageIndex, pageSize);
        
        // 执行分页查询
        Page<User> userPage = userMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为VO列表
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    vo.setId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setEmail(user.getEmail());
                    vo.setAvatar(user.getAvatar());
                    vo.setStatus(user.getStatus());
                    vo.setRole(user.getRole());
                    vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null);
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 返回分页结果
        return Result.success(new PageResult<>(userVOList, userPage.getTotal()));
    }
} 