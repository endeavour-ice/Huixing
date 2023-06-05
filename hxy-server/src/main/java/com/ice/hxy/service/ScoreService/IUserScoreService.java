package com.ice.hxy.service.ScoreService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.UserScore;

/**
 * <p>
 * 积分表 服务类
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
public interface IUserScoreService extends IService<UserScore> {

    Double getScore();

}
