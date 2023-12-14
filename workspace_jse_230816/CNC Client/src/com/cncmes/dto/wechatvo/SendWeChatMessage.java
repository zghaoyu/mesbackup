package com.cncmes.dto.wechatvo;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * *Zhong
 * *
 */
public class SendWeChatMessage {
    //发送消息的类型
    final static String MSGTYPE = "markdown";
    //将消息发送给所有成员
    final static String TOPARTY = "@all";
    //获取企业微信的企业号，根据不同企业更改
    final static String CORPID = "ww46122f9dd9ed0da2";
//    final static String CORPID = "ww2820c7620c6f87e2";
    //获取企业应用的密钥，根据不同应用更改
    final static String CORPSECRET = "w0ktb8YxFQ5OssU-l0dgEUB-7LHw0-oa9P7KeESQd_A";
//    final static String CORPSECRET = "oDiXdbwtz_HRBYijOf19zezIo-oWQXZpf73dp5pp-aY";
//    final static String CORPSECRET = "wEY4GInvOzYv0nDVAfNeG3qCmp9KIHul5ISm01s1gBU"; //mine
    //获取访问权限码URL
    final static String ACCESS_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    //创建会话请求URL
    final static String CREATE_SESSION_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";
    //根据手机号得到userid
    final static String GET_USERID_BY_MOBILE_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserid?access_token=";
    /**
     * 企业接口向下属关注用户发送给微信消息
     *
     * @param toUser 成员ID列表
     * @param toParty 部门ID列表
     * @param toTag 标签ID列表
     * @param content 消息内容
     * @param safe 是否保密
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public void sendWeChatMessage(String toUser, String toParty, String toTag, String content,String safe) {
        //从对象中提取凭证
        AccessToken accessToken = getAccessToken();
//        System.out.println("access_token:"+accessToken.getAccess_token());
        String ACCESS_TOKEN = accessToken.getAccess_token();
        String url = CREATE_SESSION_URL + ACCESS_TOKEN;
        //请求串
//        String url = CREATE_SESSION_URL + ACCESS_TOKEN;

        //封装发送消息请求JSON
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        stringBuffer.append("\"touser\":" + "\"" + toUser + "\",");
        stringBuffer.append("\"toparty\":" + "\"" + toParty + "\",");
        stringBuffer.append("\"totag\":" + "\"" + toTag + "\",");
        stringBuffer.append("\"msgtype\":" + "\"" + MSGTYPE + "\",");
        stringBuffer.append("\"markdown\":" + "{");
        stringBuffer.append("\"content\":" + "\"" + content + "\"");
        stringBuffer.append("}");
        stringBuffer.append(",\"safe\":" + "\"" + safe + "\",");
        stringBuffer.append("\"agentid\":" + "\"" + "1000005" + "\",");
        stringBuffer.append("\"debug\":" + "\"" + "1" + "\"");
        stringBuffer.append("}");
        String json = stringBuffer.toString();
//        System.out.println(json);

        try {
            URL postUrl = new URL(url);
            HttpURLConnection http = (HttpURLConnection) postUrl.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            http.setDoOutput(true);
            http.setDoInput(true);
            // 连接超时30秒
            System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
            // 读取超时30秒
            System.setProperty("sun.net.client.defaultReadTimeout", "30000");
            http.connect();

            //写入内容
            OutputStream outputStream = http.getOutputStream();
            outputStream.write(json.getBytes("UTF-8"));
            InputStream inputStream = http.getInputStream();
            int size = inputStream.available();
            byte[] jsonBytes = new byte[size];
            inputStream.read(jsonBytes);
            String result = new String(jsonBytes, "UTF-8");
            System.out.println("请求返回结果:" + result);

            //清空输出流
            outputStream.flush();
            //关闭输出通道
            outputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //获取access_token
    public static AccessToken getAccessToken() {

        AccessToken token = new AccessToken();

        //访问微信服务器
        String url = SendWeChatMessage.ACCESS_TOKEN_URL + "?corpid=" + SendWeChatMessage.CORPID + "&corpsecret=" + SendWeChatMessage.CORPSECRET+"&debug=1";
        try {
            URL getUrl = new URL(url);
            //开启连接，并返回一个URLConnection对象
            HttpURLConnection http = (HttpURLConnection)getUrl.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
            //将URL连接用于输入和输出，一般输入默认为true，输出默认为false
            http.setDoOutput(true);
            http.setDoInput(true);
            //进行连接，不返回对象
            http.connect();

            //获得输入内容,并将其存储在缓存区
            InputStream inputStream = http.getInputStream();
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            //将内容转化为JSON代码
            String message = new String(buffer,"UTF-8");
            JSONObject json = JSONObject.parseObject(message);
            //提取内容，放入对象
            token.setAccess_token(json.getString("access_token"));
            token.setExpires_in(new Integer(json.getString("expires_in")));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //返回access_token码
        return token;
    }

}
