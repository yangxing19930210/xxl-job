package com.xxl.job.admin.core.alarm.impl;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.mail.internet.MimeMessage;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.core.util.WXTokenUtil;
import com.xxl.job.core.biz.model.ReturnT;

/**
 * job alarm by email
 *
 * @author xuxueli 2020-01-19
 */
@Component
public class EmailJobAlarm implements JobAlarm {
    private static Logger logger = LoggerFactory.getLogger(EmailJobAlarm.class);
    private static String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";

    /**
     * load email job alarm template
     *
     * @return
     */
    private static final String loadEmailJobAlarmTemplate() {
        String mailBodyTemplate = "<h5>" + I18nUtil.getString("jobconf_monitor_detail") + "：</span>"
            + "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n"
            + "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" + "      <tr>\n"
            + "         <td width=\"20%\" >" + I18nUtil.getString("jobinfo_field_jobgroup") + "</td>\n"
            + "         <td width=\"10%\" >" + I18nUtil.getString("jobinfo_field_id") + "</td>\n"
            + "         <td width=\"20%\" >" + I18nUtil.getString("jobinfo_field_jobdesc") + "</td>\n"
            + "         <td width=\"10%\" >" + I18nUtil.getString("jobconf_monitor_alarm_title") + "</td>\n"
            + "         <td width=\"40%\" >" + I18nUtil.getString("jobconf_monitor_alarm_content") + "</td>\n"
            + "      </tr>\n" + "   </thead>\n" + "   <tbody>\n" + "      <tr>\n" + "         <td>{0}</td>\n"
            + "         <td>{1}</td>\n" + "         <td>{2}</td>\n" + "         <td>"
            + I18nUtil.getString("jobconf_monitor_alarm_type") + "</td>\n" + "         <td>{3}</td>\n" + "      </tr>\n"
            + "   </tbody>\n" + "</table>";

        return mailBodyTemplate;
    }

    /**
     * fail alarm
     *
     * @param jobLog
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        boolean alarmResult = true;

        // send monitor email
        if (info != null && info.getAlarmEmail() != null && info.getAlarmEmail().trim().length() > 0) {

            // 开始发送邮件
            // alarmContent
            String alarmContent = "Alarm Job LogId=" + jobLog.getId();
            if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += "<br>TriggerMsg=<br>" + jobLog.getTriggerMsg();
            }
            if (jobLog.getHandleCode() > 0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += "<br>HandleCode=" + jobLog.getHandleMsg();
            }

            // email info
            XxlJobGroup group =
                XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(Integer.valueOf(info.getJobGroup()));
            String personal = I18nUtil.getString("admin_name_full");
            String title = I18nUtil.getString("jobconf_monitor");
            String content = MessageFormat.format(loadEmailJobAlarmTemplate(),
                group != null ? group.getTitle() : "null", info.getId(), info.getJobDesc(), alarmContent);

            Set<String> emailSet = new HashSet<String>(Arrays.asList(info.getAlarmEmail().split(",")));
            for (String email : emailSet) {

                // make mail
                try {
                    MimeMessage mimeMessage = XxlJobAdminConfig.getAdminConfig().getMailSender().createMimeMessage();

                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                    helper.setFrom(XxlJobAdminConfig.getAdminConfig().getEmailUserName().concat("@sekorm.com"),
                        personal);
                    helper.setTo(email);
                    helper.setSubject(title);
                    helper.setText(content, true);

                    XxlJobAdminConfig.getAdminConfig().getMailSender().send(mimeMessage);
                } catch (Exception e) {
                    logger.error(">>>>>>>>>>> xxl-job, job fail alarm email send error, JobLogId:{}", jobLog.getId(),
                        e);

                    alarmResult = false;
                }

            }
            // 邮件发送结束

            // 开始发送企业微信
            String wxUsers =
                emailSet.stream().map(s -> s.trim().replace("@sekorm.com", "")).collect(Collectors.joining("|"));
            // 组装文本信息
            Map<String, String> stringStringMap = new HashMap<>();
            stringStringMap.put("title", group != null ? group.getTitle() : "null");
            stringStringMap.put("url", "http://172.16.1.94/xxl-job-admin/");
            stringStringMap.put("btntxt", "调度平台");
            StringBuilder sb = new StringBuilder();
            sb.append("<div>提示：详细信息请查xxl-job或本地日志</div>\n");
            sb.append("日志信息：");
            sb.append(alarmContent);
            stringStringMap.put("description", sb.toString());
            // 组装接收人信息
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("touser", wxUsers);
            objectMap.put("msgtype", "textcard");
            objectMap.put("agentid", "1000024");
            objectMap.put("textcard", stringStringMap);
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url.concat(WXTokenUtil.getToken()));
                // 拼装参数，设置编码格式
                httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
                StringEntity stringEntity = new StringEntity(JSON.toJSONString(objectMap), Charset.forName("UTF-8"));
                stringEntity.setContentEncoding("UTF-8");
                stringEntity.setContentType("application/json");
                httpPost.setEntity(stringEntity);
                try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                    HttpEntity entity = response.getEntity();
                    String msg = EntityUtils.toString(entity);
                    // 如果errcode为0则发生成功,否则是不
                    if (!(msg.indexOf("\"errcode\":0") != -1)) {
                        alarmResult = false;
                        logger.error("预警发送失败！{}", msg);
                    }
                }
            } catch (Exception e) {
                alarmResult = false;
                logger.error("预警发送失败！", e);
            }
        }
        // 企业微信发送结束

        return alarmResult;
    }

}
