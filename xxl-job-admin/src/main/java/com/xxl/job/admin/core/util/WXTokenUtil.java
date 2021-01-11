package com.xxl.job.admin.core.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * @author noah_yang
 * @version 1.0
 * @date 2020-02-26 15:43
 */
public class WXTokenUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WXTokenUtil.class);
    private static String url =
        "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ww8c58505d51cea383&corpsecret=iefHAD5YKiTfzR8qKK9UHEq37t9o1-ZJySLNcfX4VqY";
    private static String token;
    /**
     * 创建时间
     */
    private static long creatingMs;

    /**
     * 获取电商token
     *
     * @return String
     */
    public static String getToken() {
        // 当队列无为空的时候调用获取token的方法
        if (StringUtils.isBlank(WXTokenUtil.token)
            || (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - creatingMs > 1200)) {
            WXTokenUtil.clientToken();
        }
        return WXTokenUtil.token;
    }

    /**
     * 获取token的实际函数
     */
    public static void clientToken() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(url);
            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String string = EntityUtils.toString(entity);
                    Map<String, Object> result = JSON.parseObject(string, Map.class);
                    LOGGER.info("token返回result:{}", result);
                    if (result != null && (int)result.get("errcode") == 0) {
                        creatingMs = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
                        token = (String)result.get("access_token");
                    }
                }
            }
        } catch (Exception e) {
            WXTokenUtil.token = null;
            LOGGER.info("预警平台获取token异常", e);
        }
    }
}
