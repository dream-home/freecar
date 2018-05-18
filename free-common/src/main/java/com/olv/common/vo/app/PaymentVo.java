package com.olv.common.vo.app;

import java.io.Serializable;

/**
 * 支付vo类
 * Created by Administrator on 2017/8/21.
 */
public class PaymentVo implements Serializable {

    private static final long serialVersionUID = 1435L;

    /**
     * 转账金额
     */
    private String amount;
    /**
     * 支付密码
     */
    private String payPwd;

    /**
     * 收款人手机号
     */
    private String mobile;
    /**
     * 收款人id
     */
    private String payeeId;

    /**
     * 收款人uid
     * @return
     */
    private String receivedId;

    /**
     * 转账类型 （10：转账类型 20：扫码支付）
     */
    private Integer transactionType;
    /**
     * 关键词
     */
    private String keyWord;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPayPwd() {
        return payPwd;
    }

    public void setPayPwd(String payPwd) {
        this.payPwd = payPwd;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public String getReceivedId() {
        return receivedId;
    }

    public void setReceivedId(String receivedId) {
        this.receivedId = receivedId;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }
}
