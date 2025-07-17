package com.cncmes.drv;

import com.alibaba.fastjson.JSON;
import com.cncmes.base.CNC;

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
    public boolean startMachining(String ip, String programID) {
        return false;
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
                    System.out.println("getMacro : " + address + " ---------> " + macro);
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
        return false;
    }


}
