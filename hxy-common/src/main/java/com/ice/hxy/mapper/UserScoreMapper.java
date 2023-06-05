package com.ice.hxy.mapper;

import com.ice.hxy.mode.entity.UserScore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 积分表 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
@Mapper
public interface UserScoreMapper extends BaseMapper<UserScore> {

    BigDecimal selectScoreByUser(@Param("id") Long id);
}
