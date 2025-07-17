package com.cncmes.utils;

import com.cncmes.drv.CncHartFordDrv;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * *Zhong
 * *
 */
public class RabbitmqUtil {
    static String host;
    static int port;
    static String username;
    static String password;
    static {
        Yaml yaml = new Yaml();
        InputStream inputStream =  CncHartFordDrv.class.getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> data = yaml.load(inputStream);
        Map<String, Object> dataMQ = (Map<String, Object>) data.get("RabbitMQ");
        host = (String) dataMQ.get("host");
        port = (int) dataMQ.get("port");
        username = (String) dataMQ.get("username");
        password = String.valueOf(dataMQ.get("password"));
    }
    public static Connection getConnection() throws IOException, TimeoutException {
//创建连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //主机地址;默认为 localhost
        connectionFactory.setHost(host);
        //连接端口;默认为 5672
        connectionFactory.setPort(port);
        //虚拟主机名称;默认为 /
//        connectionFactory.setVirtualHost("sto");
        //连接用户名；默认为guest
        connectionFactory.setUsername(username);
        //连接密码；默认为guest
        connectionFactory.setPassword(password);
        //创建连接
        return connectionFactory.newConnection();
    }
}
