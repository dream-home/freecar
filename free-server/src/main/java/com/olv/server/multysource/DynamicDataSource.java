package com.olv.server.multysource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		// 获取当前数据源连接
		return JdbcContextHolder.getJdbcType();
	}
}
