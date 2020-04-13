package cn.bit.framework.data.jdbc.impl;

import cn.bit.framework.config.AppConfig;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


/**
 * 覆写jdbctemlate ，使用LowerCaseColumnMapRowMapper
 *
 * @author kingapex
 *         2010-6-13上午11:05:32
 */
public class LowerCaseJdbcTemplate extends JdbcTemplate {
    @Override
    protected RowMapper getColumnMapRowMapper() {
        if ("2".equals(AppConfig.getInstance().getDbType())) {
            return new OracleColumnMapRowMapper();
        } else if ("1".equals(AppConfig.getInstance().getDbType())) {
            return new MySqlColumnMapRowMapper();
        } else {
            return new ColumnMapRowMapper();
        }
    }

}
