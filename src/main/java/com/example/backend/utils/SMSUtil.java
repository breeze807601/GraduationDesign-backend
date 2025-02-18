package com.example.backend.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;

public class SMSUtil {
    public static final String ACCESS_KEY_ID = "LTAI5tFV8nBAovAhg6pjmJGZ";
    public static final String ACCESS_KEY_SECRET = "KfE5SeUMvGoLi3gdsUuqlJIHlUM3Kt";
    public static Client createClient() throws Exception {
        Config config = new Config();
        config.setAccessKeyId(ACCESS_KEY_ID)
                .setAccessKeySecret(ACCESS_KEY_SECRET);
        config.endpoint = "dysmsapi.aliyuncs.com";

        return new Client(config);
    }
}
