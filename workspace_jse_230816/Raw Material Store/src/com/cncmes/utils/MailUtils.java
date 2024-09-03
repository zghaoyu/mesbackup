package com.cncmes.utils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.cncmes.data.SystemConfig;
import com.sun.mail.util.MailSSLSocketFactory;

/**
 * 
 * @author W000586 Hui Zhi 2022/1/27
 *
 */
public class MailUtils {

	private static String USERNAME = null;
	private static String PASSWORD = null;
	private static String SMTPSERVER = null;
	private static String TRANSPORTTYPE = "smtp";
	private static String PORT = null;

	private static void getCommonSettings() {
		try {
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
			SMTPSERVER = (String) config.get("smtpServer");
			PORT = (String) config.get("port");
			USERNAME = (String) config.get("username");
			PASSWORD = (String) config.get("password");
			PASSWORD = DesUtils.decrypt(PASSWORD);
		} catch (Exception e) {

		}
	}

	/**
	 * 
	 * @param to
	 * @param cc
	 *            when send mail to multiple recipients, separate the
	 *            addresses with ','.
	 * @param subject
	 * @param content
	 * @return true if send mail successfully
	 */
	public static boolean sendMail(String to, String cc, String subject, String content) {
		getCommonSettings();
		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.user", USERNAME);
			props.put("mail.password", PASSWORD);
			props.put("mail.smtp.host", SMTPSERVER);
			props.put("mail.smtp.port", PORT);
			props.put("mail.smtp.socketFactory.port", PORT);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.transport.protocol", TRANSPORTTYPE);
			props.put("mail.smtp.ssl.trust", SMTPSERVER);
			MailSSLSocketFactory sf = new MailSSLSocketFactory();
			props.put("mail.smtp.ssl.enable", "true");
			props.put("mail.smtp.socketFactory.class", sf);

			Session session = Session.getInstance(props, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(USERNAME, PASSWORD);
				}
			});

			// session.setDebug(true);
			Message message = prepareMessage(session, to, cc, subject, content);
			Transport transport = session.getTransport(TRANSPORTTYPE);
			transport.connect(SMTPSERVER, USERNAME, PASSWORD);
			transport.sendMessage(message, message.getAllRecipients());

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static Message prepareMessage(Session session, String to, String cc, String subject, String content) {
		Message message = new MimeMessage(session);
		try {
			message.setSubject(subject);
			message.setText(content);
			message.setSentDate(new Date());
			message.setFrom(new InternetAddress(USERNAME));

			if (null != to && !to.equals("")) {
				new InternetAddress();
				InternetAddress[] iaToList = InternetAddress.parse(to);
				message.setRecipients(Message.RecipientType.TO, iaToList);
			}

			if (null != cc && !cc.equals("")) {
				new InternetAddress();
				InternetAddress[] iaCcList = InternetAddress.parse(cc);
				message.setRecipients(Message.RecipientType.CC, iaCcList);
			}
			
			message.setContent(content, "text/html;charset=utf-8");
			message.saveChanges();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}
}
