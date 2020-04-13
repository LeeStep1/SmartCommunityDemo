package cn.bit.framework.utils.export.csv;


import cn.bit.framework.utils.export.DataField;
import cn.bit.framework.utils.export.ExportDataSource;
import cn.bit.framework.utils.export.txt.TxtDataExportor;

import java.io.OutputStream;


/**
 * 描述: csv格式数据导出工具
 * @author Hill
 *
 */
public class CsvDataExportor<T> extends TxtDataExportor<T> {
	public CsvDataExportor(DataField[] fields, ExportDataSource<T> dataSource, OutputStream os) {
		super(fields, dataSource, os,",");
	}
}
