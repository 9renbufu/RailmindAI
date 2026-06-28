package com.railmind.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.user.domain.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByPhone(@Param("phone") String phone);

    User selectByUsername(@Param("username") String username);

    User selectByPhoneAndDeleted(@Param("phone") String phone, @Param("deleted") Integer deleted);

    User selectByUsernameAndDeleted(@Param("username") String username, @Param("deleted") Integer deleted);

    int countByPhone(@Param("phone") String phone);

    int countByUsername(@Param("username") String username);
}
