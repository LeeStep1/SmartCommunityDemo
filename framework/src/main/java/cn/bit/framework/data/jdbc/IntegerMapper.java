package cn.bit.framework.data.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerMapper implements RowMapper<Integer> {
	
	public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
		Integer  v = rs.getInt(1);
		return v;
	}

}
