package com.cncmes.utils;

import com.cncmes.dto.wechatvo.SendWeChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * *Zhong
 * *
 */
public class WorkWechatUtils {
    public static void sendAlarmNotification(){
        SendWeChatMessage weChat = new SendWeChatMessage();
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String date = dateFormat.format(today);
        String time = timeFormat.format(today);
        String content = "##  MES Inform  \\n>**Details are as follows.**  " +
                "\\n>Content: <font color=\\\"warning\\\">MES RUN ERROR!</font> " +
                "\\n>Principal: Eddie | Paris  \\n>  " +
                "\\n>Address: <font color=\\\"info\\\">DG TS office</font>  " +
                "\\n>Date: <font color=\\\"comment\\\">"+date+"</font>  " +
                "\\n>Time: <font color=\\\"comment\\\">"+time+"</font>  " +
                "\\n>  " +
                "\\n>Please solve the problem as soon as possible.  \\n> " +
                " \\n>More information please check the `email`.";
        weChat.sendWeChatMessage("W000650|W000014", "141", "", content,"0");
    }
}
