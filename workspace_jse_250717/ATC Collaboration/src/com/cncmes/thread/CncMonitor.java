package com.cncmes.thread;

import com.cncmes.base.CNC;
import com.cncmes.base.CncOperation;
import com.cncmes.base.DeviceState;
import com.cncmes.base.RabbitmqConfig;
import com.cncmes.drv.CncHartFordDrv;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.RabbitmqUtil;
import com.cncmes.utils.RunningMsgUtil;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

/**
 * *Zhong
 * *
 */
public class CncMonitor implements Runnable{
    private String cncIP;
    private int cncPort;
    private String atcIP;   //inform atc that cnc have open/close gas
    private int atcPort;
    private double toolRegister;    //record CNC's tool num
    CNC cncCtrl = CncHartFordDrv.getInstance();

    public CncMonitor(String ip, int port) {
        this.cncIP = ip;
        this.cncPort = port;
    }

    public CncMonitor(String cncip, int cncport, String atcIP, int atcPort) {
        this.cncIP = cncip;
        this.cncPort = cncport;
        this.atcIP = atcIP;
        this.atcPort = atcPort;
    }

    public CncMonitor() {
    }

    public double getToolRegister() {
        return toolRegister;
    }

    public void setToolRegister(double toolRegister) {
        this.toolRegister = toolRegister;
    }

    public Runnable setParams(CNC cnc, String cncIP, int cncPort) {
        cncCtrl = cnc;
        cncIP = cncIP;
        cncPort = cncPort;
        return this;
    }


    @Override
    public void run() {
        //monitor CNC status
        while (!ThreadController.isStopCncMonitorThread())
        {
            DeviceState realTimeState = cncCtrl.getMachineState(cncIP,cncPort);
            if(DeviceState.ALARMING == realTimeState)
            {
                LogUtils.atcAndCncLog(cncIP,"CNC_"+cncIP+" run error,system has stop.");
                RunningMsgUtil.appendMsgArea("CNC_"+cncIP + " has been stopped,because status is alarming.");
                return;
            }
        }


//        double toolID = 1;
//        toolRegister = 0;
//        while (!ThreadController.isStopCncMonitorThread())
//        {
//            try {
////                double toolID = cncCtrl.getMacro(ip,port,0);//get cnc signal for change tool
//                if(toolID == 0)
//                {
//                    Thread.sleep(5000);
//                }else {
//                    if(toolRegister != toolID) //cnc need to change tool
//                    {
//                        if(!cncCtrl.openSideDoor(cncIP,cncPort))    //open side door
//                        {
//                            RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" open side door error");
//                            LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" open side door error");
//                            return;
//                        }else {
//                            RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" open side door successfully");
//                            LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" open side door successfully");
//                        }
//
//
//                        if(produceDirectMessage(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(),String.valueOf(toolID),RabbitmqConfig.RK_CNC_TO_ATC.getName()+cncIP)) //send change tool message to atc
//                        {
//                            consumeMessage(RabbitmqConfig.RK_ATC_TO_CNC.getName()+cncIP);
//                        }
//                        else {
//                            //stop machine.
//                            RunningMsgUtil.appendMsgArea("CNC Monitor produce message error.");
//                            LogUtils.atcAndCncLog(cncIP,"Thread : "+Thread.currentThread().getName()+" produce message error.");
//                        }
//                        Thread.sleep(3000);
//                        toolRegister = toolID;
//                        toolID++;
//                    }
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        if(ThreadController.isStopCncMonitorThread()) LogUtils.atcAndCncLog(cncIP,"CNC monitor program stop");
    }

    //send message to RabbitMQ
    public boolean produceDirectMessage(String exchangeName,String message,String routingKey)
    {
        boolean success = false;
        try {
            Connection connection = RabbitmqUtil.getNewConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
            channel.confirmSelect();

            RunningMsgUtil.appendMsgArea("CNC Monitor send message : "+ message +" ( routing key : "+routingKey +" )");
            LogUtils.atcAndCncLog(cncIP,"CNC Monitor send message : "+ message +" ( routing key : "+routingKey +" )");

            channel.basicPublish(exchangeName, routingKey,true,null,message.getBytes());

            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long l, boolean b) throws IOException {
                    RunningMsgUtil.appendMsgArea("CNC Monitor send message successfully : "+ message +" ( routing key : "+routingKey +" )");
                    LogUtils.atcAndCncLog(cncIP,"CNC Monitor send message successfully : "+ message +" ( routing key : "+routingKey +" )");
                }

                @Override
                public void handleNack(long l, boolean b) throws IOException {
                    RunningMsgUtil.appendMsgArea("CNC Monitor send message fail,message : "+message+" ( routing key : "+routingKey +" )");
                    LogUtils.atcAndCncLog(cncIP,"CNC Monitor send message fail,message : "+message+" ( routing key : "+routingKey +" )");
                }
            });
            success = true;

        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        } catch (TimeoutException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    //receive atc callback        routingKey is cnc's ip
    public String consumeMessage(String routingKey)
    {
        String message = "";
        try {
            Connection connection = RabbitmqUtil.getNewConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(), BuiltinExchangeType.DIRECT);
//            String queue = channel.queueDeclare().getQueue();
            String queueName = RabbitmqConfig.QUEUE_ATC_TO_CNC.getName()+cncIP;    //such as queue's name is cnc_receive_10.10.206.178
            channel.queueDeclare(queueName, false,false,false,null);
            channel.queueBind(queueName,RabbitmqConfig.DIRECT_EXCHANGE_NAME.getName(),routingKey);

            Consumer consumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    String message = new String(body,"UTF-8");
                    switch (message){
                        case "lock":
                            if(cncCtrl.lockTool(cncIP,cncPort))
                            {
                                RunningMsgUtil.appendMsgArea("CNC lock tool successfully.");
                                LogUtils.atcAndCncLog(cncIP,"CNC lock tool successfully.");
                            }
                            System.out.println("CNC lock tool");
                            break;
                        case "unlock":
                            if(cncCtrl.unlockTool(cncIP,cncPort))
                            {
                                RunningMsgUtil.appendMsgArea("CNC unlock tool successfully.");
                                LogUtils.atcAndCncLog(cncIP,"CNC unlock tool successfully.");
                            }
                            System.out.println("CNC unlock tool");
                            break;
                        case "close door":
                            if(cncCtrl.closeSideDoor(cncIP,cncPort))
                            {
                                RunningMsgUtil.appendMsgArea("CNC close side door successfully.");
                                LogUtils.atcAndCncLog(cncIP,"CNC close side door successfully.");
                            }
                            System.out.println("CNC close side door");
                            break;
                        case "open door":
                            if(cncCtrl.openSideDoor(cncIP,cncPort))
                            {
                                RunningMsgUtil.appendMsgArea("CNC open side door successfully.");
                                LogUtils.atcAndCncLog(cncIP,"CNC open side door successfully.");
                            }
                            System.out.println("CNC open side door");
                            break;
                        case "start machine":
                            if(cncCtrl.startMachining(cncIP,cncPort,""))
                            {
                                RunningMsgUtil.appendMsgArea("CNC start machine.");
                                LogUtils.atcAndCncLog(cncIP,"CNC start machine.");
                            }
                            break;
                        default:
                            RunningMsgUtil.appendMsgArea("Send invalid command to CNC.");
                            LogUtils.atcAndCncLog(cncIP,"Send invalid command to CNC.");
                            System.out.println("Send invalid command to CNC.");
                    }


//                    if(message.equals(CncOperation.LOCK_TOOL.getName()))        //atc execute result no equal 0 mean false
//                    {
//                        //lock action
//                        if(!cncCtrl.lockTool(ip))
//                        {
//                            LogUtils.atcAndCncLog(ip,"cnc had received order"+message+",but cnc lock tool failed.");
//                            channel.basicReject(envelope.getDeliveryTag(),false);
//                        }else {
//                            //ack
//
//                        }
//                    }
//                    if(message.equals(CncOperation.UNLOCK_TOOL.getName()))        //atc execute result no equal 0 mean false
//                    {
//                        //unlock action
//                        if(!cncCtrl.unlockTool(ip))
//                        {
//                            LogUtils.atcAndCncLog(ip,"cnc had received order"+message+",but cnc unlock tool failed.");
//                            channel.basicReject(envelope.getDeliveryTag(),false);
//                        }else {
//                            //ack
//                        }
//                    }
                }
            };
            channel.basicConsume(queueName,false,consumer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return message;
    }

}
