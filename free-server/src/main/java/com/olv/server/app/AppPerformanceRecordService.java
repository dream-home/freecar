package com.olv.server.app;

import com.olv.common.po.AppPerformanceRecordPo;
import com.olv.common.resp.Paging;
import com.olv.common.vo.app.PerformanceRecordVo;
import com.olv.server.multysource.DataSource;
import com.olv.server.multysource.DataSourceType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业绩记录
 * Created by Administrator on 2018/1/4 0004.
 */
@Service
public interface AppPerformanceRecordService {
    public Integer insertPerformanceRecordList(List<AppPerformanceRecordPo> list);

    public List<PerformanceRecordVo> performanceList(String userId, String area, Paging paging);

    public Integer performanceTotal(String userId, String area);
}
