package com.tandiantong.framework.ip.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** IP 地址基础工具。 */
public final class IpAddressUtils {

    private IpAddressUtils() {
    }

    public static String normalize(String ip) {
        if (ip == null) {
            return null;
        }
        String value = ip.trim();
        if (value.isEmpty() || "unknown".equalsIgnoreCase(value)) {
            return null;
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public static boolean isValid(String ip) {
        String value = normalize(ip);
        if (value == null || value.contains(" ")) {
            return false;
        }
        try {
            InetAddress.getByName(value);
            return true;
        } catch (UnknownHostException exception) {
            return false;
        }
    }

    public static boolean isInternal(String ip) {
        String value = normalize(ip);
        if (!isValid(value)) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(value);
            return address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress();
        } catch (UnknownHostException exception) {
            return false;
        }
    }
}
