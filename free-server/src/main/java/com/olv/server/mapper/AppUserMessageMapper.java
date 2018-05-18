package com.olv.server.mapper;


import com.olv.common.po.AppUserMessagePo;
import com.olv.common.vo.pc.AppUserMessageVo;
import com.olv.server.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 个人消息DAO
 *
 * @version v1.0
 * @date 2017年10月10日
 */

public interface AppUserMessageMapper extends BaseMapper<AppUserMessagePo> {


    /**
     * @param rwoBounds
     * @return
     */
    @Select("select * from app_user_message order by createTime desc")
    public List<AppUserMessagePo> findAll(RowBounds rwoBounds);

    /**
     * @return
     */
    @Select("select count(1) from app_user_message")
    public long findCount();

    /**
     * 根据条件查询指定时间段数据
     *
     * @return
     * @throws Exception
     */
    public List<AppUserMessageVo> getInfoByTime(@Param("model") AppUserMessageVo appUserMessageVo, @Param("startRow") int startRow, @Param("pageSize") int pageSize);


    public Integer getInfoByTimeCount(@Param("model") AppUserMessageVo appUserMessageVo);

    @Select("select * from app_user_message where userId=#{model.userId} order by createTime desc")
    List<AppUserMessagePo> msgList(@Param("model") AppUserMessagePo model, RowBounds rowBounds);
}
