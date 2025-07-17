package com.cncmes.test;

import com.cncmes.utils.RabbitmqUtil;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * *Zhong
 * *
 */
public class YamlTest {
    @Test
    public void test(){
//        Yaml yaml = new Yaml();
//        InputStream inputStream =  this.getClass().getClassLoader().getResourceAsStream("config.yaml");
//        // 将YAML内容解析为Map
//        Map<String, Object> data = yaml.load(inputStream);
//        System.out.println(data);

        try {
//            RabbitmqUtil.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
