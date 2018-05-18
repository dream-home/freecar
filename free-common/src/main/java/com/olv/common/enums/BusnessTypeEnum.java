package com.olv.common.enums;

/**
 * 流水业务类型
 * Created by Administrator on 2017/8/17.
 */
public enum BusnessTypeEnum {

    EP_RECHARGE(11,"EP充值","recharge"),
    EP_TRANSFER_IN(12,"EP转入","transferIn"),
    EP_TRANSFER_OUT(13,"EP转出","transferOut"),
    EP_EXCHANGE(14,"EP兑换","exchange"),
    EP_WITHDRAWALS(15,"EP提现","withdrawals"),
    E_ASSET_ACTIVE(16,"资产激活","activate"),
    E_REWARD(17,"资产赏金","reward"),
    E_REWARD_SHADOWSCORE(26,"资产赏金之影子积分","shadowscore"),
    E_RELEASE(18,"资产释放","release"),
    BIRDSCORE_TRANSFER_IN(19,"TFC转入","transferIn"),
    BIRDSCORE_TRANSFER_OUT(20,"TFC转出","transferOut"),
    BIRDSCORE_TRANSFER_OUT_ADDRESS(24,"TFC转出外部地址","transferOutAddress"),
    ACCOUNT_ACTIVE(21,"激活账号","active"),
    EP_FREE_FORZEN(22,"EP解除冻结","freeForzen"),
    ACCOUNT_ACTIVE_CHARGE(23,"充值激活次数","activeCharge"),
    COIN_SYNCHRO(25,"同步TFC","coinSynchro");



    private Integer code;
    private String name;
    private String egName;

    private BusnessTypeEnum(Integer code, String name, String egName) {
        this.name = name;
        this.code = code;
        this.egName = egName;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getEgName() {
        return egName;
    }

    public static String getName(Integer code){
        if (code == null) {
            return "";
        }
        for(BusnessTypeEnum enums : BusnessTypeEnum.values()){
            if (enums.getCode().equals(code)) {
                return enums.getName();
            }
        }
        return String.valueOf(code);
    }
    public static String getEgName(String code){
        if (code == null) {
            return "";
        }
        for(BusnessTypeEnum enums : BusnessTypeEnum.values()){
            if (enums.getCode().toString().equals(code)) {
                return enums.getEgName();
            }
        }
        return code;
    }

}
