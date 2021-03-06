package com.olv.server.mapper;

import com.olv.common.po.SysParameterPo;
import com.olv.common.vo.pc.SysParameterVo;
import com.olv.server.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 参数设置DAO
 *
 * @author qsy
 * @version v1.0
 * @date 2017年6月15日
 */
public interface SysParameterMapper extends BaseMapper<SysParameterPo> {

    /**
     * @param rwoBounds
     * @return
     */
    @Select("select * from sys_parameter order by createTime desc")
    public List<SysParameterVo> findAll(RowBounds rwoBounds);
    
    
    /**
     * @param rwoBounds
     * @return
     */
    @Select("select grouptype from sys_parameter group by grouptype")
    public List<String> findGrouptype();

    /**
     * @return
     */
    @Select("select count(1) from sys_parameter")
    public long findCount();

    /**
     * @return paraValue
     */
    @Select("select paraValue from sys_parameter where paraName = #{paraName}")
    String findParameter(String paraName);
    
    /**
     * 根据条件查询指定时间段数据
     * @param orderType 类型
     * @return
     * @throws Exception
     */
	public List<SysParameterVo> getInfoByTime(@Param("model") SysParameterVo sysParameterVo, @Param("startRow") int startRow, @Param("pageSize") int pageSize);

    List<SysParameterPo> findParameters(@Param("strs") String[] paraName);
}
