package com.olv.server.mapper;

import com.olv.common.po.SysNoticePo;
import com.olv.server.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统公告表DAO
 *
 * @version v1.0
 * @date 2017年10月10日
 */
@Repository
public interface SysNoticeMapper extends BaseMapper<SysNoticePo> {
	/**
	 * @param rwoBounds
	 * @return
	 */
	@Select("select * from sys_notice order by createTime desc")
	public List<SysNoticePo> findAll(RowBounds rwoBounds);
	/**
	 * @return
	 */
	@Select("select count(1) from sys_notice")
	public long findCount();
	/**
     * 根据条件查询指定时间段数据
     * @param orderType 类型
     * @return
     * @throws Exception
     */
	public List<SysNoticePo> getInfoByTime(@Param("model") SysNoticePo sysNoticePo, @Param("startRow") int startRow, @Param("pageSize") int pageSize);

	/**
	 * @param rwoBounds
	 * @return
	 */
	@Select("select * from sys_notice where endTime > now() order by createTime desc")
	public List<SysNoticePo> noticeList(RowBounds rwoBounds);

	/**
	 * @return
	 */
	@Select("select count(1) from sys_notice where endTime > now()")
	int noticeCount();
}
