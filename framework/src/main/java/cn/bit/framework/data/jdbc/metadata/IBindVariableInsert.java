package cn.bit.framework.data.jdbc.metadata;


import cn.bit.framework.data.jdbc.metadata.entity.SqlBo;
import cn.bit.framework.data.jdbc.metadata.entity.Field;

import java.util.List;

/**
 * 绑定变量的insert语句解析接口
 * @author kingapex
 * @version v1.0
 * @since v6.0
 * 2016年10月20日下午9:20:29
 */
public interface IBindVariableInsert {
	
	
	/**
	 * 生成绑定变量的Insert 语句绑定接口
	 * @param tableName 表名
	 * @param fieldList 字段列表
	 * @return 插入 sql的 SqlBo对象，要携带sql语句及变量
	 * @see Field
	 */
	public SqlBo parseInsertSqlWidthVariable(String tableName, List<Field> fieldList) ;
}
