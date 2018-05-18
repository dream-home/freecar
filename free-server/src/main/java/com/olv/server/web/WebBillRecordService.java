package com.olv.server.web;

import java.math.BigDecimal;
import java.util.List;

import com.olv.common.po.AppBillRecordPo;
import com.olv.common.resp.Paging;
import com.olv.common.vo.pc.AppBillRecordVo1;


public interface WebBillRecordService {
	public List<AppBillRecordVo1> findAll(AppBillRecordVo1 vo,Paging paging);
	
	public Integer findCount(AppBillRecordVo1 vo);
	
	public List<String> getCurrencyType();
	
	public List<String> getBusnessType();
	
	public void add(AppBillRecordPo appBillRecordPo) throws Exception;
	
	public  BigDecimal SUMCount(AppBillRecordVo1 vo);
	

}
