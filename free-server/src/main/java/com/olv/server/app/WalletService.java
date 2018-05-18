package com.olv.server.app;

import com.olv.common.enums.TransactionTypeEnum;
import com.olv.common.po.AppUserPo;

import java.math.BigDecimal;

/**
 * 转账业务接口
 * Created by Administrator on 2017/8/22.
 */
public interface WalletService {

    /**
     * ep转账
     * @param payUser
     * @param payeeUser
     * @param amount
     * @throws Exception
     */
    public void payment(AppUserPo payUser, AppUserPo payeeUser, String amount) throws Exception;

    /**
     * EP兑换
     * @param userId
     * @param amount
     * @return
     * @throws Exception
     */
    public Boolean epExchange(String userId,BigDecimal amount) throws Exception;

    /**
     * TFC转账
     * @param payUser
     * @param payeeUser
     * @param amount
     * @throws Exception
     */
    public void birdtransfer(AppUserPo payUser, AppUserPo payeeUser, String amount, TransactionTypeEnum transactionTypeEnum) throws Exception;


    /**
     * TFC转账
     * @param payUser
     * @param amount
     * @throws Exception
     */
    public void birdtransferToOutAddress(AppUserPo payUser, String outAddress, BigDecimal amount, TransactionTypeEnum transactionTypeEnum) throws Exception;

    /**
     * 保存用户签到信息
     * */
    void openRedPacket(BigDecimal releaseAmount,BigDecimal epAmount,BigDecimal birdScore,AppUserPo appUserPo,BigDecimal shadowAmount) throws Exception;

}
