package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid=#{openid};")
    User getByOpenid(String openid);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    @Select("select * from user where id=#{id};")
    User getById(Long id);

    /**
     * 新增用户
     * @param user
     */
    void insertUser(User user);

    /**
     * 根据时间查询用户数量
     * @param endTime
     * @return
     */

    Integer totalUserNumber(LocalDateTime endTime);

    /**
     * 根据时间查询新增用户数量
     * @param beginTime
     * @return
     */
    Integer newUserNumber(LocalDateTime beginTime);

    /**
     * 根据用户名查询用户
     */
    @Select("select * from tb_user where username = #{username}")
    User getByUsername(String username);

    /**
     * 根据用户名查询用户
     */
    @Select("select * from tb_user where username = #{username}")
    User getByUsernameAndRole(String username, Integer role);
}
