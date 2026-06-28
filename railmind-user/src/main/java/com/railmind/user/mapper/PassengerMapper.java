package com.railmind.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.user.domain.model.Passenger;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PassengerMapper extends BaseMapper<Passenger> {

    List<Passenger> selectByUserIdAndDeleted(@Param("userId") Long userId, @Param("deleted") Integer deleted);

    Passenger selectByIdAndUserIdAndDeleted(@Param("id") Long id, @Param("userId") Long userId, @Param("deleted") Integer deleted);

    int countByUserIdAndDeleted(@Param("userId") Long userId, @Param("deleted") Integer deleted);
}
