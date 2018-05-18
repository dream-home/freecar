package com.olv.server.app.impl;


import com.olv.common.po.AppUserMessagePo;
import com.olv.common.resp.Paging;
import com.olv.common.util.ToolUtils;
import com.olv.common.vo.pc.AppUserMessageVo;
import com.olv.server.app.AppUserMessageService;
import com.olv.server.mapper.AppUserMessageMapper;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@Service("webAppUserMessageService")
public class AppUserMessageServiceImpl implements AppUserMessageService {

    @Resource
    private AppUserMessageMapper appUserMessageMapper;


    @Override
    public long findCount() {
        // TODO Auto-generated method stub
        return appUserMessageMapper.findCount();
    }

    @Override
    public List<AppUserMessagePo> findAll(Paging paging) throws Exception {
        RowBounds rwoBounds = new RowBounds(paging.getPageNumber(), paging.getPageSize());
        return appUserMessageMapper.findAll(rwoBounds);
    }

    @Override
    public List<AppUserMessageVo> getInfoByTime(AppUserMessageVo appUserMessageVo, Paging paging) {
        int startRow = 0;
        int pageSize = 0;
        if (null != paging) {
            startRow = (paging.getPageNumber() > 0) ? (paging.getPageNumber() - 1) * paging.getPageSize() : 0;
            pageSize = paging.getPageSize();
        } else {
            pageSize = Integer.MAX_VALUE;
        }
        return appUserMessageMapper.getInfoByTime(appUserMessageVo, startRow, pageSize);
    }

    @Override
    public void add(AppUserMessagePo appUserMessagePo) throws Exception {
        // TODO Auto-generated method stub
        appUserMessagePo.setId(ToolUtils.getUUID());
        appUserMessagePo.setCreateTime(new Date());
        appUserMessageMapper.insert(appUserMessagePo);


    }

    @Override
    public void delete(AppUserMessagePo appUserMessagePo) throws Exception {

        appUserMessageMapper.deleteByPrimaryKey(appUserMessagePo.getId());


    }

    @Override
    public Integer getInfoByTimeCount(AppUserMessageVo appUserMessageVo) {
        // TODO Auto-generated method stub
        return appUserMessageMapper.getInfoByTimeCount(appUserMessageVo);
    }

}
