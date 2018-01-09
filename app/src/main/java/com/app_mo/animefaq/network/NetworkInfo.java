package com.app_mo.animefaq.network;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * Created by hp on 8/9/2017.
 */

public class NetworkInfo {
    public static final String HOST_URL = "http://138.68.105.89/work/public/";

    public static String getDeviceMACAddress() {
        String interfaceName = "wlan0";

        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface networkInterface : networkInterfaces) {
                if (!networkInterface.getName().equalsIgnoreCase(interfaceName))
                    continue;
                byte[] MAC = networkInterface.getHardwareAddress();

                if (MAC == null)
                    return "";

                StringBuilder builder = new StringBuilder();
                for (byte aMAC : MAC) {
                    builder.append(String.format("%02X", aMAC));
                }

                for (int i = 2; i < builder.length() - 1; i=i+3) {
                    builder.insert(i, ':');
                }

                return builder.toString();
            }

        } catch (SocketException e) {
            e.printStackTrace();
            e.getMessage();
        }

        return "";
    }
}
