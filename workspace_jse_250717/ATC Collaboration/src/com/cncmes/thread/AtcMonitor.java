package com.cncmes.thread;

import com.cncmes.base.CncOperation;
import com.cncmes.base.RabbitmqConfig;
import com.cncmes.drv.AtcDrv;
import com.cncmes.dto.Result;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.RabbitmqUtil;
import com.cncmes.utils.RunningMsgUtil;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * *Zhong
 * *
 */
public class AtcMonitor implements Runnable{
    private String atcIP;
    private int atcPort;
    private String cncIP;
    private int cncPort;
    private int toolRegister;    //record CNC's tool num

    public int getToolRegister() {
        return toolRegister;
    }

    public void setToolRegister(int toolRegister) {
        this.toolRegister = toolRegister;
    }
    AtcDrv atcDrv = AtcDrv.getInstance();


    public AtcMonitor(String ip, int port) {
        this.atcIP = ip;
        this.atcPort = port;
    }

    public AtcMonitor(String ip, int port, String cncIP, int cncPort) {
        this.atcIP = ip;
        this.atcPort = port;
        this.cncIP = cncIP;
        this.cncPort = cncPort;
    }

    @Override
    public void run() {
            messageHandle(RabbitmqConfig.RK_CNC_TO_ATC.getName()+cncIP);           //receive message from cnc
    }
    public String messageHandle(String routingKey)
    {
         String message = "";
        try {
            Connection connection = RabbitmqUtil.getNewConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), BuiltinExchangeType.DIRECT);
            String queueName = RabbitmqConfig.QUEUE_CNC_TO_ATC.getName()+cncIP;
            channel.queueDeclare(queueName,false,false,false,null);  //such as queue's name is cnc_receive_10.10.206.178

            channel.queueBind(queueName,RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(),routingKey);
            Consumer consumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    String message = new String(body,"UTF-8");
                    if(!message.equals("0"))
                    {
                        RunningMsgUtil.appendMsgArea("ATC Monitor consume message : "+message +" ( routing key : "+routingKey + " )");
                        LogUtils.atcAndCncLog(cncIP,"ATC Monitor consume message : "+message +" ( routing key : "+routingKey + " )");



                        //uninstall tool
                        Result unloadUpResult = atcDrv.unloadActionUp(atcIP, atcPort);
                        if (!unloadUpResult.getResult()){
                            LogUtils.atcAndCncLog(cncIP,unloadUpResult.getErrorCasuse());
                            return;
                        }
                        if(!produceDirectMessage(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), "unlock",RabbitmqConfig.RK_ATC_TO_CNC.getName()+cncIP))
                        {
                            LogUtils.atcAndCncLog(cncIP,"ATC inform CNC to unlock tool error.");
                            return;
                        }
                        try {
                            Thread.sleep(5000); //wait for cnc unlock tool.
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Result unloadDownResult = atcDrv.unloadActionDown(atcIP,atcPort,toolRegister);
                        if(!unloadDownResult.getResult()){
                            LogUtils.atcAndCncLog(cncIP,unloadDownResult.getErrorCasuse());
                            return;
                        }


                        //install tool
                        Result loadUpResult = atcDrv.loadActionUp(atcIP,atcPort, Integer.parseInt(message));
                        if(!loadUpResult.getResult()){
                            LogUtils.atcAndCncLog(cncIP,loadUpResult.getErrorCasuse());
                            return;
                        }

                        if(!produceDirectMessage(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), "lock",RabbitmqConfig.RK_ATC_TO_CNC.getName()+cncIP))
                        {
                            LogUtils.atcAndCncLog(cncIP,"ATC inform CNC to lock tool error.");
                            return;
                        }
                        try {
                            Thread.sleep(5000); //wait for cnc lock tool.
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Result loadDownResult = atcDrv.loadActionDown(atcIP,atcPort);
                        if(!loadDownResult.getResult()){
                            LogUtils.atcAndCncLog(cncIP,loadDownResult.getErrorCasuse());
                            return;
                        }
                        LogUtils.atcAndCncLog(cncIP,"ATC change cnc_"+ cncIP +" tool successfully.");
                        RunningMsgUtil.appendMsgArea("ATC change cnc_"+ cncIP +" tool successfully");
                        channel.basicAck(envelope.getDeliveryTag(),false);

                        if(!produceDirectMessage(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), "close door",RabbitmqConfig.RK_ATC_TO_CNC.getName()+cncIP))
                        {
                            LogUtils.atcAndCncLog(cncIP,"ATC inform CNC to close door error.");
                            return;
                        }

                    }
                    else {
                        RunningMsgUtil.appendMsgArea("CNC send error tool num.");
                        channel.basicReject(envelope.getDeliveryTag(),false);
                    }
                }
            };
            channel.basicConsume(queueName,false,consumer);
//            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return message;
    }
    public boolean produceDirectMessage(String exchangeName,String message,String routingKey)
    {
        boolean success = false;
        try {
            Connection connection = RabbitmqUtil.getNewConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
            channel.basicPublish(exchangeName, routingKey,true,null,message.getBytes());
            success = true;
            System.out.println("ATC Monitor send message : "+message +" ( routing key : "+routingKey + " )");
            RunningMsgUtil.appendMsgArea("ATC monitor send "+message);
//            LogUtils.atcAndCncLog(ip,"CNC Monitor send message : "+message +" ( routing key : "+routingKey + " )");
//            channel.close();
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        } catch (TimeoutException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

}
