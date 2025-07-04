package com.payment.common.base;

/**
 * @author qinverse
 * @date 2025/6/19 17:39
 * @description DelayLevelEnum 类描述
 */
public enum DelayLevelEnum {
    LEVEL_1(1, "1s"),      // 1秒
    LEVEL_2(2, "5s"),      // 5秒
    LEVEL_3(3, "10s"),     // 10秒
    LEVEL_4(4, "30s"),     // 30秒
    LEVEL_5(5, "1m"),      // 1分钟
    LEVEL_6(6, "2m"),      // 2分钟
    LEVEL_7(7, "3m"),      // 3分钟
    LEVEL_8(8, "4m"),      // 4分钟
    LEVEL_9(9, "5m"),      // 5分钟
    LEVEL_10(10, "6m"),    // 6分钟
    LEVEL_11(11, "7m"),    // 7分钟
    LEVEL_12(12, "8m"),    // 8分钟
    LEVEL_13(13, "9m"),    // 9分钟
    LEVEL_14(14, "10m"),   // 10分钟
    LEVEL_15(15, "20m"),   // 20分钟
    LEVEL_16(16, "30m"),   // 30分钟
    LEVEL_17(17, "1h"),    // 1小时
    LEVEL_18(18, "2h");    // 2小时

    private final int level;
    private final String delay;

    DelayLevelEnum(int level, String delay) {
        this.level = level;
        this.delay = delay;
    }

    public int getLevel() {
        return level;
    }

    public String getDelay() {
        return delay;
    }

    /**
     * 把字符串格式的延迟时间转成毫秒
     */
    public long getDelayMillis() {
        return parseDelayToMillis(delay);
    }

    private long parseDelayToMillis(String delayStr) {
        if (delayStr == null || delayStr.length() < 2) {
            throw new IllegalArgumentException("无效的延迟时间格式: " + delayStr);
        }
        long number = Long.parseLong(delayStr.substring(0, delayStr.length() - 1));
        char unit = delayStr.charAt(delayStr.length() - 1);

        switch (unit) {
            case 's': return number * 1000L;
            case 'm': return number * 60 * 1000L;
            case 'h': return number * 60 * 60 * 1000L;
            default:
                throw new IllegalArgumentException("不支持的时间单位: " + unit);
        }
    }

    public static DelayLevelEnum fromLevel(int level) {
        for (DelayLevelEnum e : values()) {
            if (e.level == level) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知的delay level: " + level);
    }
}
