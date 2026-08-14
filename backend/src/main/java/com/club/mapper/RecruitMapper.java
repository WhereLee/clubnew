package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.domain.Recruit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RecruitMapper extends BaseMapper<Recruit> {

    /**
     * 原子扣减名额（库存校验，不检查版本号以支持并发）
     * 影响行数=0 说明名额已满
     */
    @Update("UPDATE recruit SET applied_count = applied_count + 1, version = version + 1 " +
            "WHERE id = #{id} AND applied_count < quota AND status = 'IN_PROGRESS'")
    int applyRecruit(@Param("id") Long id);

    @Update("UPDATE recruit SET applied_count = GREATEST(applied_count - 1, 0) WHERE id = #{id}")
    int cancelApply(@Param("id") Long id);
}
