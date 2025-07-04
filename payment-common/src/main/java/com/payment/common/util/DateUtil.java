package com.payment.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qinverse
 * @date 2025/6/19 17:13
 * @description DateUtil 类描述
 */
public class DateUtil {
    /**
     * 获取当前时间格式化字符串
     * @param pattern 日期格式，如 yyyyMMdd、yyyyMMddHHmmss
     * @return 格式化后的字符串
     */
    public static String now(String pattern) {
        return format(new Date(), pattern);
    }

    /**
     * 将日期格式化为字符串
     * @param date 日期
     * @param pattern 格式，如 yyyy-MM-dd HH:mm:ss
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null || pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("日期或格式不能为空");
        }
        return new SimpleDateFormat(pattern).format(date);
    }
}
