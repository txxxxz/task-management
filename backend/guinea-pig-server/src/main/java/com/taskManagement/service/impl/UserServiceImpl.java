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
import com.taskManagement.properties.JwtProperties;
import com.taskManagement.constant.JwtClaimsConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.taskManagement.utils.AliOssUtil;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final AliOssUtil aliOssUtil;
    
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

            // 4. 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "avatar/" + userId + "_" + System.currentTimeMillis() + extension;

            // 5. 上传文件到阿里云OSS
            String avatarUrl = aliOssUtil.upload(file.getBytes(), fileName);

            // 6. 更新用户头像URL
            user.setAvatar(avatarUrl);
            this.updateById(user);

            return avatarUrl;
        } catch (Exception e) {
            throw new UserBusinessException("头像上传失败：" + e.getMessage());
        }
    }
}
