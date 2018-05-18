package com.olv.common.vo.app;

import java.math.BigDecimal;

/**
 * 数字资产用户信息
 *
 * @author qsy
 * @version V1.0
 * @date 2017年8月19日
 */
public class CoinUserVo {
    /**
     * 帐号
     */
    private String accountName;
    /**
     * 汇率
     */
    private BigDecimal exRate;
    /**
     * 数通宝数量
     */
    private BigDecimal balance;
    /**
     * 钱包地址
     */
    private String coinAddress;
    /**
     * 支付密码
     */
    private String payPwd;
    /**
     * 瑞波币的币种id
     */
    private String currencyId;

    public String getPayPwd() {
        return payPwd;
    }

    public void setPayPwd(String payPwd) {
        this.payPwd = payPwd;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getExRate() {
        return exRate;
    }

    public void setExRate(BigDecimal exRate) {
        this.exRate = exRate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCoinAddress() {
        return coinAddress;
    }

    public void setCoinAddress(String coinAddress) {
        this.coinAddress = coinAddress;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
}
