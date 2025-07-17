package com.cncmes.utils;

import com.cncmes.data.CncData;
import com.sun.mail.util.MailSSLSocketFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * *Eddie
 * *revise attachment in sendEmail function
 */
public class ErrorEMailUtil {
    private String host;
    private String port;
    private String nick;
    private String username;
    private String password;
    private String from;
    private List<String> toList=new ArrayList<>();
    CncData cncData = CncData.getInstance();
    public ErrorEMailUtil() {
    }

    public ErrorEMailUtil(String host, String port, String nick, String username, String password, String from, String recipients) {
        this.host = host;
        this.port = port;
        this.nick = nick;
        this.username = username;
        this.password = password;
        this.from = from;
        String[] split = recipients.split(";");
        for (String string : split) {
            this.toList.add(string);
        }
        CncData cncData = CncData.getInstance();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getToList() {
        return toList;
    }

    public void setToList(List<String> toList) {
        this.toList = toList;
    }
    private boolean isEmpty() {
        if (nick != null && host != null && port != null && username != null && password != null && from != null && toList != null) {
            return false;
        }
        return true;
    }
    public boolean sendEmail(String title,String txt) throws GeneralSecurityException {

        if(isEmpty()) {
            return false;
        }


        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        Properties properties = System.getProperties();
        properties.setProperty("mail.debug", "false");
        properties.setProperty("mail.transport.protocol", "smtps");
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        properties.setProperty("mail.smtp.auth", "true");
//        properties.setProperty("mail.smtp.starttls.enable", "true");// outlook邮箱需要加上

        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.setProperty("mail.smtp.socketFactory.port", Integer.toString(443));//设置ssl端口
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.ssl.socketFactory", sf);
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");


        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        Session session = Session.getDefaultInstance(properties, authenticator);

        try {
            MimeMessage message = new MimeMessage(session);
            //set nickname
            message.setFrom(new InternetAddress(nick + " <"+from +">"));

            //set message content body
            MimeMultipart msgMultipart = new MimeMultipart("mixed");
            message.setContent(msgMultipart);

            //设置消息正文
            MimeBodyPart content = new MimeBodyPart();
            msgMultipart.addBodyPart(content);

            //设置正文格式
            MimeMultipart bodyMultipart = new MimeMultipart("related");
            content.setContent(bodyMultipart);

            //设置正文内容
            MimeBodyPart htmlPart = new MimeBodyPart();
            bodyMultipart.addBodyPart(htmlPart);
            htmlPart.setContent(txt, "text/html;charset=UTF-8");


            Date today = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String date = formatter.format(today);

            if(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\RobotCMRSocketDataHandler_"+date+".log").exists())
            {
                //attachment body
                MimeBodyPart attch1 = new MimeBodyPart();
                //content add attachment
                msgMultipart.addBodyPart(attch1);
                DataSource dataSource1 = new FileDataSource(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\RobotCMRSocketDataHandler_"+date+".log"));
                DataHandler dataHandler1 = new DataHandler(dataSource1);
                attch1.setDataHandler(dataHandler1);
                //设置第一个附件的文件名
                attch1.setFileName(
                        MimeUtility.encodeText("RobotCMRSocketDataHandler.log")
                );
//                attch1.setContent("", "text/html;charset=UTF-8");
            }


            if(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Error_"+date+".log").exists())
            {
                //attachment body
                MimeBodyPart attch2 = new MimeBodyPart();
                //content add attachment
                msgMultipart.addBodyPart(attch2);
                DataSource dataSource2 = new FileDataSource(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Error_"+date+".log"));
                DataHandler dataHandler2 = new DataHandler(dataSource2);
                attch2.setDataHandler(dataHandler2);
                //设置第二个附件的文件名
                attch2.setFileName(
                        MimeUtility.encodeText("Error.log")
                );
//                attch2.setContent("", "text/html;charset=UTF-8");
            }

            Set<String> cncIPSet = cncData.getDataMap().keySet();
            for(String ip : cncIPSet)
            {
                if(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Command_"+ip+"_"+date+".log").exists())
                {
                    //attachment body
                    MimeBodyPart attch3 = new MimeBodyPart();
                    //content add attachment
                    msgMultipart.addBodyPart(attch3);
                    DataSource dataSource3 = new FileDataSource(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Command_"+ip+"_"+date+".log"));
                    DataHandler dataHandler3 = new DataHandler(dataSource3);
                    attch3.setDataHandler(dataHandler3);
                    //设置第三个附件的文件名
                    attch3.setFileName(
                            MimeUtility.encodeText("CNC_"+ip+"Command.log")
                    );
                }

                if(new File("Log/"+TimeUtils.getCurrentDate("yyyyMMdd")+"_"+ip+"_"+"HARTFORD_MVP8.log").exists())
                {
                    //attachment body
                    MimeBodyPart attch5 = new MimeBodyPart();
                    //content add attachment
                    msgMultipart.addBodyPart(attch5);
                    DataSource dataSource5 = new FileDataSource(new File("Log/"+TimeUtils.getCurrentDate("yyyyMMdd")+"_"+ip+"_"+"HARTFORD_MVP8.log"));
                    DataHandler dataHandler5 = new DataHandler(dataSource5);
                    attch5.setDataHandler(dataHandler5);
                    //设置第四个附件的文件名
                    attch5.setFileName(
                            MimeUtility.encodeText("CNC_"+ip+"_operation.log")
                    );
//                attch4.setContent("", "text/html;charset=UTF-8");
                }
            }

//            if(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Command_10.10.206.178_"+date+".log").exists())
//            {
//                //attachment body
//                MimeBodyPart attch3 = new MimeBodyPart();
//                //content add attachment
//                msgMultipart.addBodyPart(attch3);
//                DataSource dataSource3 = new FileDataSource(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Command_10.10.206.178_"+date+".log"));
//                DataHandler dataHandler3 = new DataHandler(dataSource3);
//                attch3.setDataHandler(dataHandler3);
//                //设置第三个附件的文件名
//                attch3.setFileName(
//                        MimeUtility.encodeText("CNC_79Command.log")
//                );
////                attch3.setContent("", "text/html;charset=UTF-8");
//            }
//            if(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Command_10.10.206.72_"+date+".log").exists())
//            {
//                //attachment body
//                MimeBodyPart attch4 = new MimeBodyPart();
//                //content add attachment
//                msgMultipart.addBodyPart(attch4);
//                DataSource dataSource4 = new FileDataSource(new File(System.getProperty("user.dir") + File.separator + "Log"+"\\Command_10.10.206.72_"+date+".log"));
//                DataHandler dataHandler4 = new DataHandler(dataSource4);
//                attch4.setDataHandler(dataHandler4);
//                //设置第四个附件的文件名
//                attch4.setFileName(
//                        MimeUtility.encodeText("CNC_72Command.log")
//                );
////                attch4.setContent("", "text/html;charset=UTF-8");
//            }

//            if(new File("Log/"+TimeUtils.getCurrentDate("yyyyMMdd")+"_"+"10.10.206.72"+"_"+"HARTFORD_MVP8.log").exists())
//            {
//                //attachment body
//                MimeBodyPart attch5 = new MimeBodyPart();
//                //content add attachment
//                msgMultipart.addBodyPart(attch5);
//                DataSource dataSource5 = new FileDataSource(new File("Log/"+TimeUtils.getCurrentDate("yyyyMMdd")+"_"+"10.10.206.72"+"_"+"HARTFORD_MVP8.log"));
//                DataHandler dataHandler5 = new DataHandler(dataSource5);
//                attch5.setDataHandler(dataHandler5);
//                //设置第四个附件的文件名
//                attch5.setFileName(
//                        MimeUtility.encodeText("CNC_72_operation.log")
//                );
////                attch4.setContent("", "text/html;charset=UTF-8");
//            }
//            if(new File("Log/"+TimeUtils.getCurrentDate("yyyyMMdd")+"_"+"10.10.206.178"+"_"+"HARTFORD_MVP8.log").exists())
//            {
//                //attachment body
//                MimeBodyPart attch6 = new MimeBodyPart();
//                //content add attachment
//                msgMultipart.addBodyPart(attch6);
//                DataSource dataSource6 = new FileDataSource(new File("Log/"+TimeUtils.getCurrentDate("yyyyMMdd")+"_"+"10.10.206.178"+"_"+"HARTFORD_MVP8.log"));
//                DataHandler dataHandler6 = new DataHandler(dataSource6);
//                attch6.setDataHandler(dataHandler6);
//                //设置第四个附件的文件名
//                attch6.setFileName(
//                        MimeUtility.encodeText("CNC_79_operation.log")
//                );
////                attch4.setContent("", "text/html;charset=UTF-8");
//            }




            message.saveChanges();
            /**
             *  设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
             * MimeMessage.RecipientType.TO:发送
             * MimeMessage.RecipientType.CC：抄送
             * MimeMessage.RecipientType.BCC：密送
             */
            for (String to : toList) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }

            message.setSubject(title, "UTF-8");
//            message.setContent(content, "text/html;charset=UTF-8");

            Transport.send(message,message.getAllRecipients());

            System.out.println("Sent message successfully.... title："+title);
            return true ;
        } catch (MessagingException | UnsupportedEncodingException mex) {
            mex.printStackTrace();
        }
        return false;
    }
}
