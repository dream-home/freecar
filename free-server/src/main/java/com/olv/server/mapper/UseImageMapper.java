package com.olv.server.mapper;

import com.olv.common.po.UseImagePo;
import com.olv.server.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 图片表DAO接口
 * @author lxl
 * @version v1.0
 * @date 2017年6月14日
 */
@Repository
public interface UseImageMapper extends BaseMapper<UseImagePo> {
	/**
     * 指定id集查询图片
     * @param orderType 类型
     * @return
     * @throws Exception
     */
	List<UseImagePo> getByIds(@Param("ids") List<String> ids);
}
