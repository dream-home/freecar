package com.olv.common.enums;

/**
 * 转账订单类型
 * Created by Administrator on 2017/8/17.
 */
public enum TransactionTypeEnum {

    TRANSACTION_PAY(10, "转账"),
    SCAN_PAY(20, "二维码扫码");

    private Integer code;
    private String name;

    private TransactionTypeEnum(Integer code, String name) {
        this.name = name;
        this.code = code;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        if (code == null) {
            return "";
        }
        for (TransactionTypeEnum enums : TransactionTypeEnum.values()) {
            if (enums.getCode().equals(code)) {
                return enums.getName();
            }
        }
        return code.toString();
    }

    public static TransactionTypeEnum getTypeFromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransactionTypeEnum enums : TransactionTypeEnum.values()) {
            if (enums.getCode().equals(code)) {
                return enums;
            }
        }
        return  null;
    }
}
