package com.ice.hxy.controller.UserController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.UserScore;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.service.ScoreService.IUserScoreService;
import com.ice.hxy.util.SnowFlake;
import com.ice.hxy.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/4/3 10:18
 * @Description: 签到
 */
@RestController
public class UserSignController {

    @Resource
    private RedisCache redisCache;
    @Autowired
    private IUserScoreService scoreService;


    /**
     * 用户签到
     *
     * @return
     */
    @GetMapping("/sign")
    @Transactional(rollbackFor = Exception.class)
    public B<Boolean> doSign() {

        User loginUser = UserUtils.getLoginUser();
        Long loginUserId = loginUser.getId();
        boolean checkSign = redisCache.checkSign(loginUserId);
        if (checkSign) {
            return B.error("以签到");
        }
        QueryWrapper<UserScore> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", loginUserId);
        UserScore us = scoreService.getOne(wrapper);
        boolean is;
        if (us != null) {
            BigDecimal score = us.getScore();
            BigDecimal bigDecimal = BigDecimal.valueOf(1);
            BigDecimal decimal = score.add(bigDecimal);
            us.setScore(decimal);
            is = scoreService.updateById(us);
        } else {
            UserScore userScore = new UserScore();
            userScore.setScoreId(SnowFlake.getSnowLong());
            userScore.setUserId(loginUserId);
            BigDecimal bigDecimal = BigDecimal.valueOf(1);
            userScore.setScore(bigDecimal);
           is= scoreService.save(userScore);
        }
        if (!is) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "error");
        }
         redisCache.signIn(loginUserId);
        return B.ok();

    }

    /**
     * 查询当月签到情况
     *
     * @return
     */
    @GetMapping("/mathSign")
    public B<List<String>> mathSign() {
        User loginUser = UserUtils.getLoginUser();
        Long loginUserId = loginUser.getId();
        List<String> signInfo = redisCache.getSignInfo(loginUserId);
        return B.ok(signInfo);
    }

    /**
     * 查询当月签到情况
     *
     * @return
     */
    @GetMapping("/checkSign")
    public B<Boolean> checkSign() {
        User loginUser = UserUtils.getLoginUser();
        Long loginUserId = loginUser.getId();
        Boolean checkSign = redisCache.checkSign(loginUserId);
        return B.ok(checkSign);
    }
}
