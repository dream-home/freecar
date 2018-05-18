package com.olv.server.app;


import ru.paradoxs.bitcoin.client.BitcoinClient;

import java.math.BigDecimal;

/**
 * 数字货币业务层接口
 *
 * @author qsy
 * @version V1.0
 * @date 2017年8月17日
 */
public interface BitcoinService {


    /**
     * 获取钱包地址，如果用户不存在就创建新钱包地址
     *
     * @param accountName 账号名
     * @return 钱包地址
     */
    public String getAccountAddress(String accountName);


    public Boolean validAddress(String address);

    /**
     * 获取当前帐号的数字资产
     *
     * @param accountName 账号
     * @return 数字资产
     */
    public BigDecimal loadBalance(String accountName);

    /**
     * 获取钱包总额
     *
     * @return 钱包总额
     */
    public BigDecimal loadWalletBalance();

    /**
     * 更新用户币种资产
     * @param userid
     * @throws Exception
     */
    void updateUserCoin(String userid) throws Exception;

    public BitcoinClient getBitCoinClient();
}
