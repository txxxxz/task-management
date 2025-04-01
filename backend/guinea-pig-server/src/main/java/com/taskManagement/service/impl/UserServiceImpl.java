package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskManagement.exception.UserBusinessException;
import com.taskManagement.utils.JwtUtil;
import com.taskManagement.utils.PasswordUtil;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserUpdateDTO;
import com.taskManagement.dto.PasswordChangeDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.entity.User;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.service.UserService;
import com.taskManagement.service.FileService;
import com.taskManagement.properties.JwtProperties;
import com.taskManagement.constant.JwtClaimsConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskManagement.vo.PageResult;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final FileService fileService;
    
    @Override
    public LoginVO login(UserLoginDTO userLoginDTO) {
        try {
            // 1. 根据用户名查询用户
            User user = userMapper.getByUsername(userLoginDTO.getUsername());
            if (user == null) {
                throw new UserBusinessException("登录失败：用户名不存在");
            }
            
            // 2. 校验密码
            if (!PasswordUtil.matches(userLoginDTO.getPassword(), user.getPassword())) {
                throw new UserBusinessException("登录失败：密码错误");
            }
            
            // 3. 校验用户状态
            if (user.getStatus() == 0) {
                throw new UserBusinessException("登录失败：账号已被禁用");
            }

            // 4. 校验用户角色
            if (userLoginDTO.getRole() != null && !userLoginDTO.getRole().equals(user.getRole())) {
                String roleDesc = "";
                switch(user.getRole()) {
                    case 0: roleDesc = "普通成员"; break;
                    case 1: roleDesc = "项目负责人"; break;
                    case 2: roleDesc = "管理员"; break;
                    default: roleDesc = "未知角色"; break;
                }
                throw new UserBusinessException(String.format("登录失败：用户角色不匹配。该账号的角色是：%s", roleDesc));
            }
            
            // 5. 生成token
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put("role", user.getRole());
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
            
            // 6. 转换并返回数据
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            // 设置创建时间
            if (user.getCreateTime() != null) {
                userVO.setCreateTime(user.getCreateTime().toString());
            }
            
            return LoginVO.builder()
                    .token(token)
                    .user(userVO)
                    .build();
        } catch (Exception e) {
            if (e instanceof UserBusinessException) {
                throw e;
            }
            throw new UserBusinessException("登录失败：" + e.getMessage());
        }
    }
    
    @Override
    public UserVO register(UserRegisterDTO registerDTO) {
        // 1. 校验用户名是否已存在
        if (this.checkUsernameExist(registerDTO.getUsername())) {
            throw new UserBusinessException("用户名已存在");
        }
        
        // 2. 创建用户实体并保存
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        // 设置默认值
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        user.setStatus(1);  // 正常状态
        user.setRole(0);    // 设置默认角色为普通成员
        
        this.save(user);
        
        // 3. 转换并返回数据
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 设置创建时间
        if (user.getCreateTime() != null) {
            userVO.setCreateTime(user.getCreateTime().toString());
        }
        return userVO;
    }
    
    @Override
    public UserVO getUserById(Long id) {
        // 1. 查询用户信息
        User user = this.getById(id);
        if (user == null) {
            throw new UserBusinessException("用户不存在");
        }
        
        // 2. 转换为VO对象
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 设置创建时间
        if (user.getCreateTime() != null) {
            userVO.setCreateTime(user.getCreateTime().toString());
        }
        return userVO;
    }
    
    @Override
    public boolean checkUsernameExist(String username) {
        User user = userMapper.getByUsername(username);
        return user != null;
    }

    @Override
    public UserVO updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        // 1. 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new UserBusinessException("用户不存在");
        }

        // 2. 如果修改用户名，检查是否已存在
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(user.getUsername())) {
            if (this.checkUsernameExist(updateDTO.getUsername())) {
                throw new UserBusinessException("用户名已存在");
            }
        }

        // 3. 更新用户信息
        BeanUtils.copyProperties(updateDTO, user);
        this.updateById(user);

        // 4. 转换并返回更新后的用户信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 设置创建时间
        if (user.getCreateTime() != null) {
            userVO.setCreateTime(user.getCreateTime().toString());
        }
        return userVO;
    }

    @Override
    public void changePassword(Long userId, PasswordChangeDTO passwordDTO) {
        // 1. 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new UserBusinessException("用户不存在");
        }

        // 2. 验证原密码
        if (!PasswordUtil.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new UserBusinessException("原密码错误");
        }

        // 3. 更新密码
        user.setPassword(PasswordUtil.encode(passwordDTO.getNewPassword()));
        this.updateById(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new UserBusinessException("用户不存在");
        }

        try {
            // 2. 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png"))) {
                throw new UserBusinessException("只支持JPG或PNG格式的图片");
            }

            // 3. 检查文件大小
            if (file.getSize() > 2 * 1024 * 1024) {
                throw new UserBusinessException("图片大小不能超过2MB");
            }

            // 4. 使用 FileService 上传头像
            String avatarUrl = fileService.uploadAvatar(file, userId);
            
            return avatarUrl;
        } catch (Exception e) {
            throw new UserBusinessException("头像上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public PageResult<UserVO> getUserList(Map<String, Object> params) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件：用户名模糊查询
        if (params.containsKey("username") && params.get("username") != null) {
            queryWrapper.like(User::getUsername, params.get("username"));
        }
        
        // 添加查询条件：邮箱模糊查询
        if (params.containsKey("email") && params.get("email") != null) {
            queryWrapper.like(User::getEmail, params.get("email"));
        }
        
        // 添加查询条件：角色精确查询
        if (params.containsKey("role") && params.get("role") != null) {
            queryWrapper.eq(User::getRole, params.get("role"));
        }
        
        // 添加查询条件：状态精确查询
        if (params.containsKey("status") && params.get("status") != null) {
            queryWrapper.eq(User::getStatus, params.get("status"));
        }
        
        // 获取分页参数，默认第1页，每页10条数据
        long page = params.containsKey("page") ? Long.parseLong(params.get("page").toString()) : 1L;
        long pageSize = params.containsKey("pageSize") ? Long.parseLong(params.get("pageSize").toString()) : 10L;
        
        // 执行分页查询
        Page<User> pageResult = page(new Page<>(page, pageSize), queryWrapper);
        
        // 转换为VO列表
        List<UserVO> userVOList = new ArrayList<>();
        for (User user : pageResult.getRecords()) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            // 设置创建时间
            if (user.getCreateTime() != null) {
                userVO.setCreateTime(user.getCreateTime().toString());
            }
            userVOList.add(userVO);
        }
        
        // 构建分页结果对象
        return new PageResult<>(userVOList, pageResult.getTotal());
    }
    
    @Override
    public void deleteUser(Long id) {
        // 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new UserBusinessException("删除失败：用户不存在");
        }
        
        // 不允许删除管理员账号
        if (user.getRole() == 2) {
            throw new UserBusinessException("删除失败：不能删除管理员账号");
        }
        
        // 执行删除操作
        removeById(id);
    }
    
    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        // 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new UserBusinessException("更新状态失败：用户不存在");
        }
        
        // 不允许禁用管理员账号
        if (user.getRole() == 2 && status == 0) {
            throw new UserBusinessException("更新状态失败：不能禁用管理员账号");
        }
        
        // 更新状态
        user.setStatus(status);
        
        // 执行更新操作
        return this.updateById(user);
    }
}
