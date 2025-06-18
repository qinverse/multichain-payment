package com.payment.common.util;

import java.util.UUID;

/**
 * 随机ID工具类生成
 * @author qwl
 * @version V1.0

 * @date: 2020年06月09日 14时51分

 */
public class IDUtils{

    public static String UUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }

}

