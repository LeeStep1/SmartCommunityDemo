package cn.bit.framework.utils;

import io.netty.util.NetUtil;
import io.netty.util.internal.MacAddressUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Created by terry on 2016/7/15.
 */
public class NetworkUtils {

    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<InetAddress>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 获取外网IP，没有外网IP则返回内网IP
     * 
     * @return
     */
    public static String getNetIp() {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;// 是否找到外网IP
            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
                        netip = ip.getHostAddress();
                        finded = true;
                        break;
                    } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
                        localip = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    public static void main(String[] args) throws SocketException {

        Collection<InetAddress> colInetAddress = getAllHostAddress();

        for (InetAddress address : colInetAddress) {
            if (!address.isLoopbackAddress()) {
                byte[] b = NetworkInterface.getByInetAddress(address).getHardwareAddress();

                StringBuffer sb = new StringBuffer();

                for (int i = 0; i < b.length; i++) {
                    if (i != 0) {
                        sb.append("-");
                    }
                    // mac[i] & 0xFF 是为了把byte转化为正整数
                    String s = Integer.toHexString(b[i] & 0xFF);
                    sb.append(s.length() == 1 ? 0 + s : s);
                }

                System.err.println(sb);
            }
            // System.out.println("IP:"+address.getHostAddress());

        }
        // byte[] b = NetworkInterface.getByIndex(3).getHardwareAddress();
        // System.err.println(new String(b));
        System.err.println(getNetIp());
    }
}
