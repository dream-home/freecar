package com.olv.common.po;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 银行卡管理po类
 * Created by Administrator on 2017/8/17.
 */
@Table(name = "app_bank_card")
public class AppBankCardPo {

    /**
     * 主键编号
     */
    @Id
    private String id;
    /**
     * '用户主键编号'
     */
    private String userId;
    /**
     * '持卡人姓名'
     */
    private String name;
    /**
     * '银行卡号'
     */
    private String bankCard;
    /**
     * '开户支行'
     */
    private String branch;
    /**
     * 是否删除
     */
    private String state;
    /**
     * 是否默认
     */
    private String defaultState;
    /**
     * 银行类型id
     */
    private String bankTypeId;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(String defaultState) {
        this.defaultState = defaultState;
    }

    public String getBankTypeId() {
        return bankTypeId;
    }

    public void setBankTypeId(String bankTypeId) {
        this.bankTypeId = bankTypeId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
