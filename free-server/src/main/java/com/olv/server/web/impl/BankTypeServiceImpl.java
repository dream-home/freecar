package com.olv.server.web.impl;

import java.util.List;

import javax.annotation.Resource;

import com.olv.server.web.BankTypeService;
import org.springframework.stereotype.Service;

import com.olv.common.po.AppBankTypePo;
import com.olv.server.mapper.AppBankTypeMapper;

/**
 * 银行卡层实现类
 * @author yyr
 * @version v1.0
 * @date 2017年6月14日
 */
@Service
public class BankTypeServiceImpl implements BankTypeService {
	@Resource
	public AppBankTypeMapper appBankTypeMapper;

	@Override
	public List<AppBankTypePo> getall() {
		return appBankTypeMapper.getall();
	}
	
}
