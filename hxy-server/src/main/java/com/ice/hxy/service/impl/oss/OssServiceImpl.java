package com.ice.hxy.service.impl.oss;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ice.hxy.service.commService.TokenService;
import com.ice.hxy.util.IpUtils;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.config.initConfig.ConstantProperties;

import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.Team;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.service.commService.RedisCache;


import com.ice.hxy.mq.RabbitService;
import com.ice.hxy.service.OssService.OssService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.chatService.TeamService;
import com.ice.hxy.util.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author ice
 * @date 2022/9/17 12:48
 */
@Service
@Slf4j
public class OssServiceImpl implements OssService {
    @Resource
    private RabbitService rabbitService;


    @Resource
    private IUserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private TokenService tokenService;
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisCache redisCache;

    /**
     * 用户头像的上传
     *
     * @param file    上传的文件
     * @param request 登录的用户
     * @return 返回url
     */
    @Override
    public B<String> upload(MultipartFile file, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser();
        // 判断用户是否上传过
        Long userId = loginUser.getId();
        RLock lock = redissonClient.getLock(CacheConstants.REDIS_FILE_AVATAR_LOCK + userId.toString().intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                if (file == null) {
                    return B.parameter();
                }
                String redisKey = CacheConstants.OSS_AVATAR_USER_REDIS_KEY + userId;
                String url = getUrl(redisKey, file);
                User user = new User();
                user.setId(userId);
                user.setAvatarUrl(url);
                boolean updateById = userService.updateById(user);
                if (!updateById) {
                    return B.parameter();
                }
                long integer = DateUtils.getRemainSecondsOneDay();
                redisCache.setCacheObject(redisKey, LocalDateTime.now(), integer, TimeUnit.SECONDS);
                if (!StringUtils.hasText(url)) {
                    return B.parameter();
                }
                loginUser.setAvatarUrl(url);
                tokenService.setTokenByUser(request, user);
                // 删除掉主页的用户
                rabbitService.sendDelRedisKey(CacheConstants.REDIS_INDEX_KEY);
                return B.ok(url);
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return B.empty();
    }

    /**
     * 队伍头像上传
     *
     * @param file   文件流
     * @param teamID 队伍的id
     * @return 返回头像url
     */
    @Override
    public B<String> upFileByTeam(MultipartFile file, Long teamID) {
        User loginUser = UserUtils.getLoginUser();
        Long userId = loginUser.getId();
        RLock lock = redissonClient.getLock(CacheConstants.REDIS_FILE_BY_TEAM_AVATAR_LOCK + userId.toString().intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {

                if (file == null || LongUtil.isEmpty(teamID)) {
                    B.parameter();
                }
                Team team = teamService.getTeamByTeamUser(teamID, userId);
                if (team == null) {
                    return B.empty();
                }
                Long teamUserId = team.getUserId();
                if (!userId.equals(teamUserId)) {
                    return B.auth();
                }
                String redisKey = CacheConstants.OSS_AVATAR_TEAM_REDIS_KEY + teamID;
                String url = getUrl(redisKey, file);
                team.setAvatarUrl(url);
                boolean teamByTeam = teamService.updateTeamByTeam(team);
                if (!teamByTeam) {
                    return B.parameter();
                }
                long integer = DateUtils.getRemainSecondsOneDay();
                redisCache.setCacheObject(redisKey, LocalDateTime.now().toString(), integer, TimeUnit.SECONDS);
                return B.ok(url);
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return B.empty();
    }

    public String getUrl(String redisKey, MultipartFile file) {
        String key = redisCache.getCacheObject(redisKey);
        if (StringUtils.hasText(key)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "今日上限...");
        }
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantProperties.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantProperties.ACCESS_KEY_ID;
        String accessKeySecret = ConstantProperties.ACCESS_KEY_SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantProperties.BUCKET_NAME;
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        // 返回客服端的原始名字
        String originalFilename = IdUtil.simpleUUID() + file.getOriginalFilename();
        String objectName = "user/" + new DateTime().toString("yyyy/MM/dd") + "/" + originalFilename;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {

            InputStream inputStream = file.getInputStream();
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } catch (Exception oe) {
            log.error(oe.getMessage());
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 忘记密码
     *
     * @param responseEmail responseEmail
     * @param request       用户信息
     * @return true
     */
    @Override
    public B<Boolean> sendForgetEMail(ResponseEmail responseEmail, HttpServletRequest request) {
        String id = request.getSession().getId();
        RLock lock = redissonClient.getLock(CacheConstants.REDIS_FILE_BY_FORGET_LOCK + id.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                // 获取真实ip
                ipEmailUtil(request);
                if (responseEmail == null) {
                    return B.parameter("请输入邮箱");
                }

                String email = responseEmail.getEmail();
                String userAccount = responseEmail.getUserAccount();
                if (!StringUtils.hasText(email)) {
                    return B.parameter("请输入邮箱");
                }
                if (!StringUtils.hasText(userAccount)) {
                    return B.parameter("请输入账号");
                }

                if (REUtils.isEmail(email)) {
                    return B.parameter("请输入正确邮箱");
                }
                // 根据邮箱查找用户
                User user = userService.forgetUserEmail(email);

                if (user == null) {
                    return B.parameter("该邮箱没有注册过");

                }
                String userUserAccount = user.getUserAccount();
                if (!userAccount.equals(userUserAccount)) {
                    return B.parameter("请输入该邮箱绑定的账号");

                }

                String code = RandomUtil.getRandomFour();
                String[] split = email.split("@");
                String name = split[0];
                boolean sendQQEmail = sendQQEmail(email, code, name);
                if (!sendQQEmail) {
                    return B.parameter("发送失败请重试");

                }
                String redisKey = CacheConstants.REDIS_FORGET_CODE + email;
                return B.ok(redisCache.setCacheObject(redisKey, code, 60, TimeUnit.SECONDS));
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return B.ok(false);
    }
    /**
     * 发送注册邮件
     *
     * @param responseEmail 接受的邮件
     * @return 返回Boolean
     */
    @Override
    public B<Boolean> sendRegisterEMail(ResponseEmail responseEmail, HttpServletRequest request) {
        String id = request.getSession().getId();
        RLock lock = redissonClient.getLock(CacheConstants.REDIS_FILE_BY_REGISTER_LOCK + id.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                // 获取真实ip
                ipEmailUtil(request);
                String email = getEmail(responseEmail);
                if (userService.seeUserEmail(email)) {
                    return B.parameter("注册邮箱重复");
                }
                email = email.toLowerCase();
                String code = getCode(email);
                if (code == null) {
                    return B.parameter("发送失败请重试");
                }
                String redisKey = CacheConstants.REDIS_REGISTER_CODE + email;
                return B.ok(redisCache.setCacheObject(redisKey, code, 60, TimeUnit.SECONDS));
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return B.ok(true);
    }

    /**
     * 发送绑定验证码
     *
     * @param responseEmail responseEmail
     * @param request       s
     * @return s
     */
    @Override
    public B<Boolean> sendBinDingEMail(ResponseEmail responseEmail, HttpServletRequest request) {
        String id = request.getSession().getId();
        RLock lock = redissonClient.getLock(CacheConstants.REDIS_FILE_BY_BING_DING_LOCK + id.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                // 获取真实ip
                if (responseEmail == null) {
                    return B.parameter();
                }

                ipEmailUtil(request);
                User user = UserUtils.getLoginUser();
                String email = getEmail(responseEmail);
                String userEmail = user.getEmail();
                if (StringUtils.hasText(userEmail)) {
                    if (!userEmail.equals(email)) {
                        return B.parameter();
                    }
                }
                if (userService.seeUserEmail(email)) {
                    return B.parameter("该邮箱已被注册");

                }
                email = email.toLowerCase();
                String code = getCode(email);
                if (code == null) {
                    return B.parameter("发送失败请重试");
                }
                String redisKey = CacheConstants.REDIS_FILE_BY_BING_DING_KEY + email;
                return B.ok(redisCache.setCacheObject(redisKey, code, 60, TimeUnit.SECONDS));
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return B.ok(true);
    }

    private String getCode(String email) {
        String code = RandomUtil.getRandomSix();
        String[] split = email.split("@");
        String name = split[0];
        boolean sendQQEmail = sendQQEmail(email, code, name);
        if (!sendQQEmail) {
            return null;
        }
        return code;
    }

    private String getEmail(ResponseEmail responseEmail) {
        if (responseEmail == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请输入邮箱");
        }
        String email = responseEmail.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请输入邮箱");
        }

        if (REUtils.isEmail(email)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请输入正确邮箱");
        }
        return email.toLowerCase();
    }

    /**
     * 发送邮件(参数自己根据自己的需求来修改，发送短信验证码)
     *
     * @param receives 接收人的邮箱
     * @param code     验证码
     * @param name     收件人的姓名
     * @return 是否成功
     */
    public boolean sendQQEmail(String receives, String code, String name) {

        SimpleDateFormat sdf = SDFUtils.getFdt();
        // 模板
        String str = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><p style='font-size: 20px;font-weight:bold;'>尊敬的：" + name + "，您好！</p>"
                + "<p style='text-indent:2em; font-size: 20px;'>欢迎注册伙伴匹配系统，您本次的验证码是 "
                + "<span style='font-size:30px;font-weight:bold;color:red'>" + code + "</span>，1分钟之内有效，请尽快使用！</p>"
                + "<p style='text-align:right; padding-right: 20px;'"
                + "<a href='http://www.hyycinfo.com' style='font-size: 18px'></a></p>"
                + "<span style='font-size: 18px; float:right; margin-right: 60px;'>" + sdf.format(LocalDateTime.now()) + "</span></body></html>";
        String them = "验证码";
        return EmailUtil.sendEmail(receives, them, str);
    }

    private void ipEmailUtil(HttpServletRequest request) {
        String ipAddress = IpUtils.getIpAddress(request);
        Integer num = redisCache.getCacheObject(ipAddress);
        if (num != null) {
            // 一天的次数过多
            if (num >= 3 && num < 5) {
                redisCache.increment(ipAddress);
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求次数过多，今天还剩余" + (5 - num - 1) + "次");
            } else if (num >= 5) {
                IpUtilSealUp.addIpList(ipAddress);
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求次数过多,请明天再试");
            } else {
                redisCache.increment(ipAddress);
            }
        } else {
            redisCache.setCacheObject(ipAddress, 1, DateUtils.getRemainSecondsOneDay(),
                    TimeUnit.SECONDS);
        }
    }


}
