package com.railmind.train.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.train.domain.model.Station;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StationMapper extends BaseMapper<Station> {

    /**
     * 模糊搜索站点（支持中文名、城市名、编码）
     */
    @Select("SELECT * FROM t_station WHERE status = 1 AND " +
            "(name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "city LIKE CONCAT('%', #{keyword}, '%') OR " +
            "code LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY name LIMIT 20")
    List<Station> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据编码查询站点
     */
    @Select("SELECT * FROM t_station WHERE code = #{code} AND status = 1")
    Station selectByCode(@Param("code") String code);

    /**
     * 根据城市查询站点列表
     */
    @Select("SELECT * FROM t_station WHERE city = #{city} AND status = 1 ORDER BY name")
    List<Station> selectByCity(@Param("city") String city);
}
