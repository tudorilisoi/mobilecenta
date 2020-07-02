package com.unicenta.pos.api;

import java.util.logging.Logger;
import java.net.*;
import java.util.*;

import static java.lang.System.out;

public class NetworkInfo {
    private static final Logger logger = Logger.getLogger("com.openbravo.pos.api.NetworkInfo");

    private static String SEPARATOR = " > ";

    //TODO  switch from strings to hashmap (ifName, addressesArray)
    public static ArrayList<String> getAllAddresses() {
        ArrayList<String> addresses = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                ArrayList<String> addrs = getInterfaceInformation(netint);
                addresses.addAll(addrs);
            }
            return addresses;
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseAddress(String addressInfo) {
        String[] parts = addressInfo.split(SEPARATOR);
        return parts[1].replace("/", "");
    }

    static ArrayList<String> getInterfaceInformation(NetworkInterface netint) throws SocketException {
        String ifName = netint.getDisplayName();
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        ArrayList<String> addresses = new ArrayList<String>();

        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            if (inetAddress.isLoopbackAddress()) {
                break;
            }
            out.printf("InetAddress: %s\n", inetAddress);
            addresses.add(ifName + SEPARATOR + inetAddress.toString());
        }
        out.printf("\n");
        return addresses;
    }
}
