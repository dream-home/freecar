package com.olv.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统通用工具操作类
 *
 * @author qsy
 * @version v1.0
 * @date 2016年11月26日
 */
public class ToolUtils {
    public static ObjectMapper objectMapper;

    /**
     * 产生UUID串
     *
     * @return 32位字符
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 随机产生6位数
     *
     * @return 验证码
     */
    public static String getCode() {
        return String.valueOf(((int) ((Math.random() * 9 + 1) * 100000)));
    }

    /**
     * 获取订单号
     *
     * @return 订单号
     */
    public static String getOrderNo() {
        return new Date().getTime() + String.valueOf(((int) ((Math.random() * 9 + 1) * 1000)));
    }

    /**
     * 对象序列化成json字符串
     *
     * @param obj 目标对象
     * @return json字符串
     */
    public static String toJson(Object obj) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            LogUtils.error("序列化json对象失败", ex);
        }
        return "";
    }

    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
     * (1)转换为普通JavaBean：readValue(json,Student.class)
     * (2)转换为List,如List<Student>,将第二个参数传递为Student
     * [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
     *
     * @param jsonStr
     * @param valueType
     * @return
     */
    public static <T> T toObject(String jsonStr, Class<T> valueType) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        try {
            return objectMapper.readValue(jsonStr, valueType);
        } catch (Exception ex) {
            LogUtils.error("字符串转json对象失败", ex);
        }

        return null;
    }

    /**
     * json数组转List
     *
     * @param jsonStr
     * @param valueTypeRef
     * @return
     */
    public static <T> T toObject(String jsonStr, TypeReference<T> valueTypeRef) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        try {
            return objectMapper.readValue(jsonStr, valueTypeRef);
        } catch (Exception ex) {
            LogUtils.error("字符串转json集合对象失败", ex);
        }
        return null;
    }

    /**
     * 验证手机号是否正确
     *
     * @param mobile 手机号
     * @return true|false
     */
    public static boolean isMobile(String mobile) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

    /**
     * 验证文字是否含有emoj表情
     */
    public static boolean containsEmoji(String source) {
        if (StringUtils.isEmpty(source)) {
            return false;
        }
        int len = source.length();
        boolean isEmoji = false;
        for (int i = 0; i < len; i++) {
            char hs = source.charAt(i);
            if (0xd800 <= hs && hs <= 0xdbff) {
                if (source.length() > 1) {
                    char ls = source.charAt(i + 1);
                    int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
                    if (0x1d000 <= uc && uc <= 0x1f77f) {
                        return true;
                    }
                }
            } else {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
                    return true;
                } else if (0x2B05 <= hs && hs <= 0x2b07) {
                    return true;
                } else if (0x2934 <= hs && hs <= 0x2935) {
                    return true;
                } else if (0x3297 <= hs && hs <= 0x3299) {
                    return true;
                } else if (hs == 0xa9 || hs == 0xae || hs == 0x303d
                        || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c
                        || hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
                    return true;
                }
                if (!isEmoji && source.length() > 1 && i < source.length() - 1) {
                    char ls = source.charAt(i + 1);
                    if (ls == 0x20e3) {
                        return true;
                    }
                }
            }
        }
        return isEmoji;
    }

    /**
     * String类型转Integer
     *
     * @param sValue
     * @param iDefault
     * @return
     */
    public static int parseInt(String sValue, int iDefault) {
        if (sValue == null) return iDefault;
        int _iValue = iDefault;
        try {
            _iValue = Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            ;
        }
        return _iValue;
    }


    /**
     * String类型转double 默认为0.
     *
     * @param sValue
     * @return
     */
    public static double parseDouble(String sValue) {
        return parseDouble(sValue, 0.0);
    }

    /**
     * String类型转double.
     *
     * @param sValue
     * @param dDefault
     * @return
     */
    public static double parseDouble(String sValue, double dDefault) {
        if (sValue == null) return dDefault;
        double _dValue = dDefault;
        try {
            _dValue = Double.parseDouble(sValue);
        } catch (NumberFormatException e) {
            return _dValue;
        }
        return _dValue;
    }

    /**
     * 获取两个数字之前的随机数
     *
     * @param min
     * @param max
     * @return
     */
    public static int randomInt(int min, int max) {
        Random rand = new Random();
        if (min == max)
            return min;
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return rand.nextInt(max - min) + min;
    }


    /**
     * 获取两个数字之前的随机数
     *
     * @param min
     * @param max
     * @return
     */
    public static Double randomDouble(Double min, Double max) {
        if (max==0d){
            return  0d;
        }
        Random random = new Random();
        Integer i = Double.valueOf(min * 1000000).intValue();
        Integer j = Double.valueOf(max * 1000000).intValue();
        Integer h = random.nextInt(j) % (j - i + 1) + i;
        return Double.valueOf(h) / 1000000;
    }


    public static BigDecimal matchingPrice(BigDecimal basePrice, BigDecimal currentPrice, BigDecimal changePerTransaction, BigDecimal transactionNum) {
        BigDecimal newCurrentPrice = new BigDecimal("0.0");
        if (currentPrice.compareTo(basePrice) == -1) {
            newCurrentPrice = currentPrice.add(changePerTransaction.multiply(transactionNum)).setScale(6, RoundingMode.HALF_EVEN);
            if (newCurrentPrice.compareTo(basePrice) >= 0) {
                newCurrentPrice = basePrice;
            }
        } else if (currentPrice.compareTo(basePrice) == 1) {
            newCurrentPrice = currentPrice.subtract(changePerTransaction.multiply(transactionNum)).setScale(6, RoundingMode.HALF_EVEN);
            if (basePrice.compareTo(newCurrentPrice) >= 0) {
                newCurrentPrice = basePrice;
            }
        }
        return newCurrentPrice;
    }

    public static Boolean   is100Mutiple(Double number) {
        if (number==null || number<0){
            return false;
        }
        if (number%100==0){
            return true;
        }
        return  false;
    }
}
