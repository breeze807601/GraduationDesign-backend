package com.example.backend.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.example.backend.pojo.vo.BillSMSVo;

public class SendSMSUtil {
    /**
     *
     * @param phone 电话
     * @param billSMSVo 短信中的信息
     * @param code  短信模板码，REMINDER_OF_PAID_ELECTRICITY：电费账单提醒，REMINDER_OF_PAID_WATER：水费账单提醒
     */
    public static void paidReminder(String phone, BillSMSVo billSMSVo,String code) throws Exception {
        Client client = SMSUtil.createClient();
        SendSmsRequest request = new SendSmsRequest();
        request.setSignName("小区系统提醒")
                .setTemplateCode(code)
                .setPhoneNumbers(phone)
                .setTemplateParam(billSMSVo.toString());
        try {
            client.sendSmsWithOptions(request, new RuntimeOptions());
        } catch (TeaException error) {
            Common.assertAsString(error.message);
        } catch (Exception e) {
            TeaException error = new TeaException(e.getMessage(), e);
            Common.assertAsString(error.message);
        }
    }
    /**
     *
     * @param phone 电话
     * @param name  名字
     * @param code  短信模板码，INSUFFICIENT_BALANCE：余额不足，PAYMENT_NOTICE：待支付住户缴费通知
     *              VERIFICATION_CODE：验证码
     */
    public static void sendPaymentNotice(String phone,String name,String code) throws Exception {
        Client client = SMSUtil.createClient();
        SendSmsRequest request = new SendSmsRequest();
        request.setSignName("小区系统提醒")
                .setTemplateCode(code)
                .setPhoneNumbers(phone)
                .setTemplateParam("{\"name\":\""+name+"\"}");
        try {
            client.sendSmsWithOptions(request, new RuntimeOptions());
        } catch (TeaException error) {
            Common.assertAsString(error.message);
        } catch (Exception e) {
            TeaException error = new TeaException(e.getMessage(), e);
            Common.assertAsString(error.message);
        }
    }
}
