package com.ice.hxy.service.commService;


import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2022/10/29 16:47
 * @PackageName:com.ice.py.utils
 * @ClassName: RedisCache
 * @Description: 缓存工具类
 * @Version 1.0
 */
@Component
@SuppressWarnings({"all"})
@Slf4j
public class RedisCache {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 缓存map 数据
     *
     * @param key
     * @param map
     * @param <T>
     */
    public <T> boolean setCacheMap(final String key, final Map<String, T> map) {
        if (map != null) {
            try {
                redisTemplate.opsForHash().putAll(key, map);
            } catch (Exception e) {
                log.error("setCacheMap缓存失败");
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    public <T> boolean setCacheMap(final String key, final Map<String, T> map, final long timeout, final TimeUnit unit) {
        if (map != null) {
            try {
                redisTemplate.opsForHash().putAll(key, map);
                return expire(key, timeout, unit);
            } catch (Exception e) {
                log.error("setCacheMap缓存失败!");
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 删除key
     *
     * @param key
     * @return
     */
    public boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean removeLikeKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        key = key + "*";
        Set keys = redisTemplate.keys(key);
        if (!CollectionUtils.isEmpty(keys)) {
            return redisTemplate.delete(keys) > 0;
        }
        return false;
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> boolean setCacheObject(final String key, final T value, final long timeout, final TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("setCacheObject缓存失败。。。。");
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public <T> boolean setCacheObject(final String key, final T value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("setCacheObject缓存失败。。。。");
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public <T> void increment(final String increment) {
        redisTemplate.opsForValue().increment(increment);
    }

    public <T> long setCacheList(final String key, final List<T> list) {
        Long count;
        try {
            count = redisTemplate.opsForList().rightPushAll(key, list);
        } catch (Exception e) {
            log.error("缓存失败。。。。");
            log.error(e.getMessage());
            return 0;
        }
        return count == null ? 0 : count;
    }

    public <T> long setCacheList(final String key, final List<T> list, final Integer timeout, final TimeUnit timeUnit) {
        Long count;
        try {
            count = redisTemplate.opsForList().rightPushAll(key, list);
            expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("List缓存失败。。");
            log.error(e.getMessage());
            return 0;
        }
        return count == null ? 0 : count;
    }

    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public long addCacheSet(final String key, final Object value, final Integer timeout, final TimeUnit timeUnit) {
        Long count = null;
        try {
            count = redisTemplate.opsForSet().add(key, value);
            expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Set缓存失败");
        }
        return count == null ? 0 : count;
    }

    public boolean isCachSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public boolean removeCachSet(String key, Object value) {
        Long remove = redisTemplate.opsForSet().remove(key, value);
        return remove != null && remove > 0;
    }

    public Set<Object> getAllCachSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public boolean setBitMap(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    public boolean signIn(Long userId) {
        try {
            LocalDate localDate = LocalDate.now();
            int day = localDate.get(ChronoField.DAY_OF_MONTH) - 1;
            String key = CacheConstants.SIGN + DateUtils.getDate() + ":" + userId;
            boolean bitMap = setBitMap(key, day, true);
            if (bitMap) {
                int mathDay = DateUtils.getMathDay();
            }
            return bitMap;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }

    }



    /**
     * 查询当天是否有签到
     *
     * @param cacheKey
     * @return
     */
    public boolean checkSign(Long userId) {
        String key = CacheConstants.SIGN + DateUtils.getDate() + ":" + userId;
        LocalDate localDate = LocalDate.now();
        return redisTemplate.opsForValue().getBit(key, localDate.getDayOfMonth() - 1);
    }

    /**
     * 获取用户签到次数
     */
    public long getSignCount(Long userId) {
        String key = CacheConstants.SIGN + DateUtils.getDate() + ":" + userId;
        Long bitCount = (Long) redisTemplate.execute((RedisCallback) cbk -> cbk.bitCount(key.getBytes()));
        return bitCount;
    }

    /**
     * 获取本月签到信息
     *
     * @param cacheKey
     * @param localDate
     * @return
     */
    public List<String> getSignInfo(Long userId) {
        LocalDate localDate = LocalDate.now();
        String key = CacheConstants.SIGN + DateUtils.getDate() + ":" + userId;
        List<String> resultList = new ArrayList<>();
        int lengthOfMonth = localDate.lengthOfMonth();
        List<Long> bitFieldList = (List<Long>) redisTemplate.execute((RedisCallback<List<Long>>) cbk
                -> cbk.bitField(key.getBytes(),
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(lengthOfMonth)).valueAt(0)));
        if (bitFieldList != null && bitFieldList.size() > 0) {
            long valueDec = bitFieldList.get(0) != null ? bitFieldList.get(0) : 0;
            for (int i = lengthOfMonth; i > 0; i--) {
                LocalDate tempDayOfMonth = LocalDate.now().withDayOfMonth(i);
                if (valueDec >> 1 << 1 != valueDec) {
                    resultList.add(tempDayOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
                valueDec >>= 1;
            }
        }
        return resultList;
    }

    /**
     * 获取当月连续签到次数
     *
     * @param cacheKey
     * @param localDate
     * @return
     */
    public long getContinuousSignCount(Long userId) {
        LocalDate localDate = LocalDate.now();
        String key = CacheConstants.SIGN + DateUtils.getDate() + ":" + userId;
        long signCount = 0;
        List<Long> list = redisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType
                .unsigned(localDate.getDayOfMonth())).valueAt(0));
        if (list != null && list.size() > 0) {
            long valueDec = list.get(0) != null ? list.get(0) : 0;
//            System.out.println("valueDec..." + valueDec);
            for (int i = 0; i < localDate.getDayOfMonth(); i++) {
                if (valueDec >> 1 << 1 == valueDec) {
                    if (i > 0) {
                        break;
                    }
                } else {
                    signCount += 1;
                }
                valueDec >>= 1;
            }
        }
        return signCount;
    }

    /**
     * 获取当月首次签到日期
     */
    public LocalDate getFirstSignDate(Long userId) {
        LocalDate localDate = LocalDate.now();
        String key = CacheConstants.SIGN + DateUtils.getDate() + ":" + userId;
        long bitPosition = (Long) redisTemplate.execute((RedisCallback) cbk -> cbk.bitPos(key.getBytes(), true));
        return bitPosition < 0 ? null : localDate.withDayOfMonth((int) bitPosition + 1);
    }

    public void deleteByPrex(String prex) {
        Set<String> keys = redisTemplate.keys(prex);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 模糊查询所有的key
     *
     * @param prex
     * @return
     */
    public Set<String> getKeyByPrex(String prex) {
        Set<String> keys = redisTemplate.keys(prex);
        if (!CollectionUtils.isEmpty(keys)) {
            return keys;
        }
        return null;
    }

    /**
     * 根据多个Key 进行查找
     *
     * @param keys
     * @param <T>
     * @return
     */
    public <T> List<T> getListByKeys(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        List<T> list = this.redisTemplate.executePipelined(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection conn = (StringRedisConnection) connection;
                for (String key : keys) {
                    conn.get(key);
                }
                return null;
            }
        });
        return list;
    }

    /**
     * 获取zset的评分
     *
     * @param key
     * @param obj
     * @return 返回空不存在
     */
    public Double getZSetScore(final String key, final Object obj) {
        return redisTemplate.opsForZSet().score(key, obj);
    }

    public Double setScore(final String key, final Object obj, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, obj, score);
    }

    public boolean addZSet(String key, Object value, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("addZSet 保存失败: {}", e.getMessage());
            return false;
        }
    }

    public <T> long addZSet(String key, Set<DefaultTypedTuple<T>> set) {
        try {
            return redisTemplate.opsForZSet().add(key, set);
        } catch (Exception e) {
            log.error("addZSet 保存失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 从大到小排序获取所有的值
     *
     * @param key
     * @return
     */
    public Set<String> getZSetDesc(String key) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, -1);
    }

    /**
     * 从大到小排序获取所有的值
     *
     * @param key
     * @return
     */
    public Set<String> getZSetDesc(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 从小到大排序获取所有的值
     *
     * @param key
     * @return
     */
    public Set<Object> getZSetAsc(String key) {
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    public Set<Object> getZSetAsc(String key, int start, int end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }


}
