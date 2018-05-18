package com.olv.server.web;

import java.util.List;

import com.olv.common.resp.Paging;
import com.olv.common.vo.pc.AppPerformanceRecordVo;

public interface AppPerformanceRecordService {
    public List<AppPerformanceRecordVo> findAll(AppPerformanceRecordVo vo,Paging paging);
	
	public Integer findCount(AppPerformanceRecordVo vo);

}
