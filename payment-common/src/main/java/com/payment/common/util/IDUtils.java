package com.payment.common.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

import java.net.InetAddress;

/**
 * 随机ID工具类生成
 *
 * @author qwl
 * @version V1.0
 * @date: 2020年06月09日 14时51分
 */
public class IDUtils {

    private static Snowflake getSnowflake(Integer datacenterId) {
        return IdUtil.createSnowflake(getWorkerIdByIP(), datacenterId);
    }

    public static String snowFlakeIdGenerator() {
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        return date + getSnowflake(1).nextId();
    }

    private static int getWorkerIdByIP() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            return ip.hashCode() & 1023; // 取低 10 位，最多支持 1024 台
        } catch (Exception e) {
            throw new RuntimeException("无法获取本机 IP", e);
        }
    }

}

