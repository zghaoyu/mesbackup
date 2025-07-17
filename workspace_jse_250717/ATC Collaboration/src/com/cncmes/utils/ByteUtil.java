package com.cncmes.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * *Zhong
 * *
 */
public class ByteUtil {
    /**
     * 将byte数组转换为浮点数
     */
    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    /**
     * 将byte数组转换为浮点数-并保留最后两位小数点
     */
    public static double toDouble00(byte[] bytes) {
        double f = ByteBuffer.wrap(bytes).getFloat();
        return toDouble00(f);
    }

    /**
     * double 保留最后两位小数点
     */
    public static double toDouble00(double f) {
        BigDecimal b = new BigDecimal(f);
        // 是小数点后只有两位的双精度类型数据
        double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }

    /**
     * 将16进制转10进制-s去掉前缀0x
     * 也可以手动改后面16参数，改自己要的字节
     */
    public static int get16to10(String s) {
        return Integer.parseInt(s, 16);
    }

    /**
     * 将byte[]转为各种进制的字符串
     *
     * @param bytes byte[]
     * @param radix 基数可以转换进制的范围，从Character.MIN_RADIX到Character.MAX_RADIX，超出范围后变为10进制
     * @return 转换后的字符串
     * <p>
     * 类型 占用 bit（位）
     * byte（字节） 8
     * short（短整型） 16
     * int（整型） 32
     * long（长整型） 64
     * float（单精度浮点型） 32
     * double（双精度浮点型） 64
     * char（字符） 16
     * boolean（布尔型） 1
     */
    public static String binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }

    /**
     * 将byte数组转换为整数
     * 转换为bit后，最左边的那位表示，符号位（有符号/无符号）
     */
    public static int bytesToInt(byte[] bs) {
        int a = 0;
        for (int i = bs.length - 1; i >= 0; i--) {
            a += bs[i] * Math.pow(255, bs.length - i - 1);
        }
        return a;
    }

    /**
     * 将byte数组转换为short
     */
    public static short bytesToshort(byte[] b) {
        short l = 0;
        for (int i = 0; i < 2; i++) {
            l <<= 8; //<<=和我们的 +=是一样的，意思就是 l = l << 8
            l |= (b[i] & 0xff); //和上面也是一样的  l = l | (b[i]&0xff)
        }
        return l;
    }

}
