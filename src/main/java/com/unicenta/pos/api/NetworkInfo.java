package com.unicenta.pos.api;

import java.util.logging.Logger;
import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.System.out;

public class NetworkInfo {
    private static final Logger logger = Logger.getLogger("com.openbravo.pos.api.NetworkInfo");

    public static void getAllAddresses() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                displayInterfaceInformation(netint);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            if (inetAddress.isLoopbackAddress()) {
                return;
            }
            out.printf("InetAddress: %s\n", inetAddress);
        }
        out.printf("\n");
    }
}
