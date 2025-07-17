package com.cncmes.drv;

import com.alibaba.fastjson.JSON;
import com.cncmes.base.CNC;

import com.cncmes.base.DeviceState;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.handler.impl.HartfordCncDataHandler;
import com.cncmes.utils.HttpRequestUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * *Zhong
 * *
 */
public class CncHartFordDrv implements CNC {
    static Map<String, String> hartFordConfig;
    static String commandURL;
    static String brand;
    static String model;
    static {
        Yaml yaml = new Yaml();
        InputStream inputStream =  CncHartFordDrv.class.getClassLoader().getResourceAsStream("config.yaml");
        // 将YAML内容解析为Map
        Map<String, Object> data = yaml.load(inputStream);
        Map<String, Map> dataCNC = (Map<String, Map>) data.get("CNC");
        hartFordConfig = dataCNC.get("HartFord");
        commandURL = hartFordConfig.get("commandURL");
        brand = hartFordConfig.get("brand");
        model = hartFordConfig.get("model");
    }
    public static CncHartFordDrv cncHartFordDrv = new CncHartFordDrv();
    public static CncHartFordDrv getInstance() {
        return cncHartFordDrv;
    }

    @Override
    public boolean sendCommand(String ip, int port, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> rtnData) {
        boolean success = false;
        String url = commandURL+operationName;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("brand",brand);
        jsonObject.put("model",model);
        jsonObject.put("ip",ip);
        jsonObject.put("port", port);
        JSONObject data = new JSONObject();
        if(inParas != null && inParas.size()>0)
        {

            for(String key : inParas.keySet())
            {
                if(key == "brand" || key == "model" || key == "ip" || key == "port")
                    continue;
                else{
                    data.put(key,inParas.get(key));
                }
            }
            String s = StringEscapeUtils.escapeJava(data.toString());
            jsonObject.put("data",s);
        }

        String parasJsonStr = jsonObject.toString().replace("\\\\", "").replace("#r#n", "\\\\r\\\\n");
        System.out.println(parasJsonStr);
        String rtnMsg = "OK";
        LinkedHashMap<String, Object> outParas = new LinkedHashMap<>();
        outParas.put("code",0);
        outParas.put("data","?");
        JSONObject resp = JSONObject.fromObject(HttpRequestUtils.httpPost(url,parasJsonStr,"utf-8",outParas));

        for (String para : outParas.keySet()) {
            if (outParas.get(para).getClass().equals(Integer.class)) {
                if (resp.getInt(para) != (int) outParas.get(para)) {
                    rtnMsg = "ERR";
                    success = false;
                    break;
                }
            }
        }
        if ("OK".equals(rtnMsg)) success = true;
        for (String para : outParas.keySet()) {
            if (outParas.get(para).getClass().equals(Integer.class)) {
                rtnMsg += "," + para + "=" + resp.getInt(para);
            } else {
                rtnMsg += "," + para + "=" + resp.getString(para);
                if (null != rtnData) rtnData.put(para, resp.getString(para));
            }
        }
        return success;
    }

    @Override
    public boolean startMachining(String ip,int port ,String programID) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) paras.put("program", programID);
        return sendCommand(ip, port,"start", paras, null);
    }

    @Override
    public double getMacro(String cncIp,int cncPort, double address) {
        double macro = -1;
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        paras.put("name", String.valueOf(address));
        if (sendCommand(cncIp, cncPort,"macro_get", paras, rtnData)) {
            HartfordCncDataHandler rsltHandler = HartfordCncDataHandler.getInstance();
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    macro = rsltHandler.machineGetMacroHandler(rtnData.get(key));
//                    System.out.println("getMacro : " + address + " ---------> " + macro);
                    break;
                }
            }
        }
        return macro;
    }

    @Override
    public boolean setMacro(String cncIP, int cncPort, String address, String value, String dec) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        paras.put("name", address);
        paras.put("value", value);
        paras.put("dec", dec);
        if (sendCommand(cncIP, cncPort,"macro_set", paras, rtnData)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean openSideDoor(String cncIp, int cncPort) {
        startMachining(cncIp,cncPort,"");       //M code control
        //check sensor
        for(int i = 0 ;i <= 20 ; i++)
        {
            if(checkSensor(cncIp,"","1"))
            {
                return true;
            }else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public boolean closeSideDoor(String cncIP, int cncPort) {
        startMachining(cncIP,cncPort,"");       //M code control
        //check sensor
        for(int i = 0 ;i <= 20 ; i++)
        {
            if(checkSensor(cncIP,"","1"))
            {
                return true;
            }else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public Boolean checkSensor(String cncip, String sensorAddress, String value)
    {
        Boolean result = false;
        String inParasStr = "{\"brand\":\"hartford\",\"model\":\"MVP-8\",\"ip\":\""+cncip+"\",\"port\":8193,\"data\":\"{\\\"address\\\":\\\""+sensorAddress+"\\\"}\"}";
        LinkedHashMap<String, Object> outParas = new LinkedHashMap<>();
        outParas.put("code",0);
        outParas.put("data","?");
        JSONObject resp = JSONObject.fromObject(HttpRequestUtils.httpPost("http://10.10.206.5:6300/api/v3/hartford/io_get", inParasStr, "utf-8", outParas));
        String rtnMsg = "OK";
        for (String para : outParas.keySet()) {
            if (outParas.get(para).getClass().equals(Integer.class)) {
                if (resp.getInt(para) != (int) outParas.get(para)) {
                    rtnMsg = "ERR";
                    result = false;
                    break;
                }
            }
        }
        if ("OK".equals(rtnMsg)) {
            String data = resp.getString("data");
            JSONObject sensorValue = JSONObject.fromObject(data);
            if(sensorValue.getString("value").equals(value))
            {
                result = true;
            }
        }
        return result;
    }

    @Override
    public Boolean lockTool(String cncip,int port) {
        return startMachining(cncip,port,"");
    }

    @Override
    public Boolean unlockTool(String cncip,int port) {
        return startMachining(cncip,port,"");
    }

    @Override
    public DeviceState getMachineState(String ip, int port) {
        DeviceState state = DeviceState.SHUTDOWN;
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, port,"status", null, rtnData)) {
            CncDataHandler rsltHandler = HartfordCncDataHandler.getInstance();
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    state = rsltHandler.machineStateHandler(rtnData.get(key));
                    break;
                }
            } else {
                state = DeviceState.ALARMING;
            }
        }
        return state;
    }


}
