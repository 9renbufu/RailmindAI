package com.railmind.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.order.domain.model.Outbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OutboxMapper extends BaseMapper<Outbox> {

    @Select("SELECT * FROM t_outbox WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT #{limit}")
    List<Outbox> selectPending(@Param("limit") int limit);

    @Update("UPDATE t_outbox SET status = 'SENT', updated_at = NOW() WHERE id = #{id}")
    int markSent(@Param("id") Long id);

    @Update("UPDATE t_outbox SET status = 'FAILED', error_message = #{errorMessage}, retry_count = retry_count + 1, updated_at = NOW() WHERE id = #{id}")
    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);

    @Select("SELECT * FROM t_outbox WHERE aggregate_type = #{aggregateType} AND aggregate_id = #{aggregateId} AND event_type = #{eventType}")
    Outbox selectByEvent(@Param("aggregateType") String aggregateType,
                         @Param("aggregateId") String aggregateId,
                         @Param("eventType") String eventType);
}
