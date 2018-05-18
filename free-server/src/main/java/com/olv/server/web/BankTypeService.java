package com.olv.server.web;

import java.util.List;

import com.olv.common.po.AppBankTypePo;

/**
 * 银行卡业务层接口
 * @author yyr
 * @version v1.0
 * @date 2017年6月14日
 */
public interface BankTypeService {
	List<AppBankTypePo> getall();
}
