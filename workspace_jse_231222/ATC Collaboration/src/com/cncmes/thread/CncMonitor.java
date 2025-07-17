package com.cncmes.thread;

import com.cncmes.base.CNC;
import com.cncmes.base.RabbitmqConfig;
import com.cncmes.drv.CncHartFordDrv;
import com.cncmes.utils.RabbitmqUtil;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * *Zhong
 * *
 */
public class CncMonitor implements Runnable{
    private String ip;
    private int port;
    CNC cncCtrl = CncHartFordDrv.getInstance();

    public CncMonitor(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public CncMonitor() {
    }

    //    public Runnable setParams(CNC cnc, String cncIP, int cncPort) {
//        cncCtrl = cnc;
//        ip = cncIP;
//        port = cncPort;
//        return this;
//    }
    @Override
    public void run() {
        while (!ThreadController.isStopCncMonitorThread())
        {
            try {
                double toolID = cncCtrl.getMacro(ip,port,0);
                if(toolID == 0)
                {
                    Thread.sleep(5000);
                }else {
                    //cnc open side door

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //send message to RabbitMq
    public boolean produceDirectMessage(String message,String routingKey)
    {
        boolean success = false;
        try {
            Connection connection = RabbitmqUtil.getConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), BuiltinExchangeType.DIRECT);
            channel.basicPublish(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), routingKey,null,message.getBytes());
            success = true;
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return success;
    }
    //prepare for lock/unlock
    public String consumeMessage(String routingKey)
    {
        String message = "";
        try {
            Connection connection = RabbitmqUtil.getConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), BuiltinExchangeType.DIRECT);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue,RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(),routingKey);
            Consumer consumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    String message = new String(body,"UTF-8");
                    if(!message.equals("0"))
                    {
                        //lock/unlock action
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
