package com.taskmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskmanagement.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
} 