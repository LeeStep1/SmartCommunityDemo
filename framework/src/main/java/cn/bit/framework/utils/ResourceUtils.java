package cn.bit.framework.utils;

import org.apache.commons.lang3.LocaleUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 资源文件工具类
 * @author healy
 *
 */
public class ResourceUtils {

	private ResourceBundle resourceBundle;
	
	private ResourceUtils(String resource) {
		resourceBundle = ResourceBundle.getBundle(resource);
	}

	private ResourceUtils(String resource,Locale locale) {
		resourceBundle = ResourceBundle.getBundle(resource,locale);
	}
	
	/**
	 * 获取资源
	 * @param resource 资源
	 * @return 解析
	 */
	public static ResourceUtils getResource(String resource) {
		return new ResourceUtils(resource);
	}

	/**
	 * 获取资源
	 * @param resource 资源
	 * @return 解析
	 */
	public static ResourceUtils getResource(String resource,String localeName) {
		return new ResourceUtils(resource,LocaleUtils.toLocale(localeName));
	}
	
	/**
	 * 根据key取得value
	 * @param key 键值
	 * @param args value中参数序列，参数:{0},{1}...,{n}
	 * @return
	 */
	public String getValue(String key, Object... args) {
		String temp = resourceBundle.getString(key);
		//MessageFormat.
		return MessageFormat.format(temp, args);
	}
	
	/**
	 * 获取所有资源的Map表示
	 * @return 资源Map
	 */
	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<String, String>();
		for(String key: resourceBundle.keySet()) {
			map.put(key, resourceBundle.getString(key));
		}
		return map;
	}
}
