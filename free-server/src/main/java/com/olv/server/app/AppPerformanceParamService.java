package com.olv.server.app;

import com.olv.common.po.AppPerformanceParamPo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 业绩相关参数
 * Created by Administrator on 2018/1/4 0004.
 */
@Service
public interface AppPerformanceParamService {

    public AppPerformanceParamPo findOneAppPerformanceParamPo(BigDecimal performance ,Integer type );


}
