package com.olv.server.app;


import com.olv.common.po.AppUserMessagePo;
import com.olv.common.resp.Paging;
import com.olv.common.vo.pc.AppUserMessageVo;

import java.util.List;

public interface AppUserMessageService {
	
	/**
	 * 查找总记录数
	 * @return
	 */
	public long findCount();
	/**
	 * 查找所有数据
	 * @param paging 分页
	 * @return 集合
	 * @throws Exception 
	 */
	
	
	
	public List<AppUserMessagePo> findAll(Paging paging) throws Exception;
	
	
	public List<AppUserMessageVo> getInfoByTime(AppUserMessageVo appUserMessageVo, Paging paging);
	
	public Integer getInfoByTimeCount(AppUserMessageVo appUserMessageVo);
	
	/**
	 * 新增
	 * @param appUserMessagePo
	 * @throws Exception
	 */
	
	public void add(AppUserMessagePo appUserMessagePo) throws Exception;
	
	/**
	 *删除
	 * @throws Exception
	 */
	
	public void delete(AppUserMessagePo appUserMessagePo) throws Exception;

}
