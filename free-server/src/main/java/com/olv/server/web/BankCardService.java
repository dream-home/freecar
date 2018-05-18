package com.olv.server.web;

import java.util.List;

import com.olv.common.resp.Paging;
import com.olv.common.vo.app.BankCardVo;
import com.olv.common.vo.pc.AppBankCardVo;

/**
 * 银行卡业务层接口
 * @author yyr
 * @version v1.0
 * @date 2017年6月14日
 */
public interface BankCardService {

	List<AppBankCardVo> getPoList(AppBankCardVo vo,Paging paging);

	void update(BankCardVo bankCardVo) throws Exception;

}
