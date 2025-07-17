package com.cncmes.thread;

import com.cncmes.base.RabbitmqConfig;
import com.cncmes.drv.AtcDrv;
import com.cncmes.utils.RabbitmqUtil;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * *Zhong
 * *
 */
public class AtcMonitor implements Runnable{
    private String ip;
    private String port;
    AtcDrv atcDrv = AtcDrv.getInstance();

    public AtcMonitor(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {

    }
    public String consumeMessage(String routingKey)
    {
         String message = "";
        try {
            Connection connection = RabbitmqUtil.getConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), BuiltinExchangeType.DIRECT);
            channel.queueDeclare(RabbitmqConfig.QUEUE_CNC_TO_ATC.getName(),true,false,false,null);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue,RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(),routingKey);
            Consumer consumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    String message = new String(body,"UTF-8");
                    if(!message.equals("0"))
                    {
                       Boolean result = atcDrv.loadToolToCNC(ip, Integer.parseInt(port), Integer.parseInt(message));
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return message;
    }
}
