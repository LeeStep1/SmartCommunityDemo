package cn.bit.framework.utils;

import io.netty.util.NetUtil;
import io.netty.util.internal.MacAddressUtil;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.Map.Entry;

/**
 * 通用工具类(提供一些常用而不好归类的方法)
 *
 * @author healy
 */
public class CommonUtils {
    public static final String SYSPROP_RUNMODE = "runMode";
    public static final String SYSPROP_RUNMODE_TEST = "test";
    public static final String SYSPROP_TESTMODE = "testMode";
    public static final String SYSPROP_TESTMODE_ON = "on";

    /**
     * 获取16个字符的唯一号
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 把properties文件加载到map里面
     *
     * @param uri 文件URI(暂时只支持classpath路径)
     * @return 加载后用来存放参数的map
     */
    public static Map<String, String> loadProps(String uri) {
        OrderedProperties props = new OrderedProperties();
        Map result = new LinkedHashMap();
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(uri);
        try {
            props.loadMap(is, result);
        } catch (Exception e) {
            throw new RuntimeException("load resource fail, uri:" + uri
                    + " errorMsg:" + e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    /**
     * 把properties文件加载到map里面
     *
     * @param uri 文件URI(暂时只支持classpath路径)
     * @param map 用来存放参数的map
     */
    @SuppressWarnings("unchecked")
    public static void loadProps(String uri, Map map) {
        CheckUtils.notNull(map, "map");
        map.putAll(loadProps(uri));
    }

    public static List<Entry<String, String>> loadList(String uri) {
        OrderedProperties props = new OrderedProperties();
        List<Entry<String, String>> list = new ArrayList<Entry<String, String>>();
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(uri);
        try {
            props.loadList(is, list);
        } catch (Exception e) {
            throw new RuntimeException("load resource fail, uri:" + uri
                    + " errorMsg:" + e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return list;
    }

    public static Object newInstance(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("new instance fail : " + e.getMessage(),
                    e);
        }
    }

    public boolean isTestMode() {
        return SYSPROP_RUNMODE_TEST.equals(System.getProperty(SYSPROP_RUNMODE));
    }

    public boolean isTestMode(String funcName) {
        if (isTestMode()) {
            return true;
        }
        return SYSPROP_TESTMODE_ON.equals(System.getProperty(SYSPROP_TESTMODE
                + "." + funcName));
    }

    public static <T> T getEnum(Class<T> clazz, int index) {
        T[] c = clazz.getEnumConstants();
        return c[index];
    }

    public static String getMACAddress() throws Exception {

        InetAddress ia = InetAddress.getLocalHost();
        System.err.println(">>>>>>>>>>>>>" + ia.getHostAddress());
        //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        if (mac == null)
            return null;
        //NetworkInterface.getNetworkInterfaces().
        //下面代码是把mac地址拼装成String
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }

        //把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toUpperCase();
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            System.err.println(UUID.randomUUID().toString().replaceAll("-",""));
        }
        String s  = "7b0a7632054a407e89945dec79b73a8b-86-8".replaceAll("^([0-9a-zA-Z]+)-([0-9]+)-([0-9]+)$", "$3");
        System.err.println(">>>>"+s);
    }
}
