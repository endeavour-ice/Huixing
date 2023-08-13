package com.ice.hxy.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author ice
 * @date 2022/9/15 18:03
 */

public class DateUtils {
    /**
     * 距离当天还有多少秒
     * @return
     */
    public static long getRemainSecondsOneDay() {
        LocalDateTime now = LocalDateTime.now(); // 获取当前时间
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59); // 当天结束时间
        return now.until(endOfDay, ChronoUnit.SECONDS);
    }

    /**
     * 获取当月有多少天
     * @return
     */
    public static int getMathDay() {
        LocalDateTime now = LocalDateTime.now(); // 获取当前时间
        int year = now.getYear(); // 获取当前年份
        int month = now.getMonthValue(); // 获取当前月份
        YearMonth yearMonth = YearMonth.of(year, month); // 创建 YearMonth 实例
        return yearMonth.lengthOfMonth(); // 获取当月的天数
    }

    public static String getDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public static long getLocalTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0;
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault()); // 将LocalDateTime转换为ZonedDateTime
        return zonedDateTime.toInstant().toEpochMilli(); // 将ZonedDateTime转换为Instant
    }

    public static LocalDateTime getLocalTimeByTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp); // 将时间戳转换为Instant类型
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static String getLDTString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        int year = localDateTime.getYear(); // 获取年份
        int month = localDateTime.getMonthValue(); // 获取月份
        int day = localDateTime.getDayOfMonth(); // 获取日期
        return String.format("%d年%d月%d日", year, month, day);
    }
}
