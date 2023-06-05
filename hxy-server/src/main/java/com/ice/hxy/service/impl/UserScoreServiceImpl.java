package com.ice.hxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.UserScoreMapper;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.UserScore;
import com.ice.hxy.service.ScoreService.IUserScoreService;
import com.ice.hxy.util.UserUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 积分表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
@Service
public class UserScoreServiceImpl extends ServiceImpl<UserScoreMapper, UserScore> implements IUserScoreService {

    @Override
    public Double getScore() {
        User loginUser = UserUtils.getLoginUser();
        BigDecimal userScore = baseMapper.selectScoreByUser(loginUser.getId());
        if (userScore == null) {
            return 0d;
        }
        return userScore.doubleValue();
    }

}
