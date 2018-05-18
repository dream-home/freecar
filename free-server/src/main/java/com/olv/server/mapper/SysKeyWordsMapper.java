package com.olv.server.mapper;


import com.olv.common.po.SysKeyWordsPo;
import com.olv.server.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 关键词汇数据操作类
 *
 * @author jay
 * @version v1.0
 * @date 2017年12月5日
 */
@Repository
public interface SysKeyWordsMapper extends BaseMapper<SysKeyWordsPo> {

    @Select("select count(1) from sys_key_words where name like concat(concat('%',#{keyWords}),'%')  or instr(#{keyWords},name) >0")
    public int findKeyWords(@Param("keyWords") String keyWords);


}
