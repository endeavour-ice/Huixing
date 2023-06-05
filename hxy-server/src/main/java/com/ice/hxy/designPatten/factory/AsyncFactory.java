package com.ice.hxy.designPatten.factory;

import com.ice.hxy.mode.entity.OpLog;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.service.IOpLogService;
import com.ice.hxy.service.UserService.UserTeamService;
import com.ice.hxy.util.IpUtils;
import com.ice.hxy.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

/**
 * @Author ice
 * @Date 2023/5/8 12:05
 * @Description: 异步工厂
 */
@Slf4j
public class AsyncFactory {
    private static final IOpLogService iOpLogService = SpringUtil.getBean(IOpLogService.class);
    private static final UserTeamService userTeamService = SpringUtil.getBean(UserTeamService.class);
    private static final RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
    /**
     * 操作日志记录
     *
     * @param opLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOp(final OpLog opLog) {
        return new TimerTask() {
            @Override
            public void run() {
                // 远程查询操作地点
                opLog.setOpLocation(IpUtils.getRealAddressByIP(opLog.getOpIp()));
                iOpLogService.save(opLog);
            }
        };
    }

    public static TimerTask upTimeUNReadNum(Long uid,Long tid,final String key) {
        return new TimerTask() {
            @Override
            public void run() {
                if (!redisCache.hasKey(key)) {
                    return;
                }
                Integer num = redisCache.getCacheObject(key);
                if (num == null || num <= 0) {
                    return;
                }
                try {
                    userTeamService.update().eq("user_id", uid).eq("team_id",tid)
                            .ge("unread_num", 0)
                            .setSql("unread_num=unread_num+"+num).update();
                    redisCache.deleteObject(key);
                } catch (Exception e) {
                    log.error("upTimeUNReadNum error:{}",e.getMessage());
                }
            }
        };
    }

}
