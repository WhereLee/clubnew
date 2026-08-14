package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.domain.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    @Update("UPDATE activity SET applied_count = applied_count + 1, version = version + 1 " +
            "WHERE id = #{id} AND applied_count < quota AND status IN ('PUBLISHED','ONGOING')")
    int applyActivity(@Param("id") Long id);

    @Update("UPDATE activity SET applied_count = GREATEST(applied_count - 1, 0) WHERE id = #{id}")
    int cancelApply(@Param("id") Long id);
}
