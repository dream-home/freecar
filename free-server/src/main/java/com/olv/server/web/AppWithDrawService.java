package com.olv.server.web;

import java.math.BigDecimal;
import java.util.List;

import com.olv.common.po.AppWithDrawPo;
import com.olv.common.resp.Paging;

import com.olv.common.vo.pc.AppWithDrawVo;

public interface AppWithDrawService {
	
	 public List<AppWithDrawVo> findAll(AppWithDrawVo vo,Paging paging);
		
	 public Integer findCount(AppWithDrawVo vo);
	 
	 
	 public void update(AppWithDrawPo Po );
	 
	 
	 public BigDecimal findSUM(AppWithDrawVo vo);

}
