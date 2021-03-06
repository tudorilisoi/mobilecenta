//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2017 uniCenta
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.AltEncrypter;
import com.unicenta.pos.api.AES256Cryptor;
import com.unicenta.pos.api.AesCbcWithIntegrity;
import com.unicenta.pos.api.NetworkInfo;
import com.unicenta.pos.api.QRCodeGenerator;

import javax.swing.*;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author JG uniCenta
 */
public class JPanelConfigMobilecenta extends javax.swing.JPanel implements PanelConfig {

    private final DirtyManager dirty = new DirtyManager();
    private String port;
    private String aesPassword;
    private String secretKeys;
    private String serverIPAddress;
    private static final Logger logger = Logger.getLogger("com.openbravo.pos.config.JPanelConfigMobilecenta");

    /**
     *
     */
    public JPanelConfigMobilecenta() {

        initComponents();

        jtxtServerPort.getDocument().addDocumentListener(dirty);
        jtxtAESKey.getDocument().addDocumentListener(dirty);


    }

    /**
     * @return
     */
    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }

    /**
     * @return
     */
    @Override
    public Component getConfigComponent() {
        return this;
    }

    private String loadCryptedProp(AppConfig config, String prop) {
        String val = config.getProperty(prop);
        if (val != null && val.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cipherkey");
            val = cypher.decrypt(val.substring(6));
            return val;
        }
        return val;
    }

    /**
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {
        boolean _dirty = false;

        port = config.getProperty("mobilecenta.server_port");
        if (port == null) {
            port = "7777";
            _dirty = true;
        }
        jtxtServerPort.setText(port);

        aesPassword = loadCryptedProp(config, "mobilecenta.aes_password");
        if (aesPassword == null) {
            aesPassword = generateAESKey();
            _dirty = true;
        }
        jtxtAESKey.setText(aesPassword);

        secretKeys = loadCryptedProp(config, "mobilecenta.aes_secret_keys");


        serverIPAddress = config.getProperty("mobilecenta.server_ip_address");
        ArrayList<String> addresses = NetworkInfo.getAllAddresses();

        jComboBoxNetworkIPs.removeAllItems();
        for (int i = 0; i < addresses.size(); i++) {
            String addr = addresses.get(i);
            jComboBoxNetworkIPs.addItem(addr);
            String parsed = NetworkInfo.parseAddress(addr);
            if (parsed.equals(serverIPAddress)) {
                jComboBoxNetworkIPs.setSelectedItem(addr);
            }
        }

        setQRCode(getQRJSONString());

        dirty.setDirty(_dirty);

    }

    /**
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {

        port = jtxtServerPort.getText();
        if (port == null) {
            port = "7777";
            jtxtServerPort.setText(port);
        }
        config.setProperty("mobilecenta.server_port", port);

        aesPassword = jtxtAESKey.getText();
        AltEncrypter cypher = new AltEncrypter("cipherkey");
        String prevPass = loadCryptedProp(config, "mobilecenta.aes_password");

        if (prevPass == null || !prevPass.equals(aesPassword)) {

            config.setProperty("mobilecenta.aes_password", "crypt:" +
                    cypher.encrypt(aesPassword));

            secretKeys = AES256Cryptor.generateKeys(aesPassword);
            config.setProperty("mobilecenta.aes_secret_keys", "crypt:" +
                    cypher.encrypt(secretKeys));

            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        serverIPAddress = (String) jComboBoxNetworkIPs.getSelectedItem();
        serverIPAddress = NetworkInfo.parseAddress(serverIPAddress);
        config.setProperty("mobilecenta.server_ip_address", serverIPAddress);

        setQRCode(getQRJSONString());
        dirty.setDirty(false);
    }


    private String generateAESKey() {
        byte[] array = new byte[10]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = Base64.getEncoder().encodeToString(array);
        //cut the == padding
        return generatedString.substring(0, generatedString.length() - 2);
    }

    private String getQRJSONString() {
        HashMap d = new HashMap();


        d.put("secretKeys", secretKeys);
        d.put("baseURL", "http://" + serverIPAddress + ":" + port);
//        d.put("port", port);
//        d.put("serverIPAddress", serverIPAddress);

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting().create();
        String ret = gson.toJson(d);
        logger.info(ret);
        logger.info(secretKeys);
        return ret;
    }

    private void setQRCode(String str) {
        try {
            byte[] imgBytes = QRCodeGenerator.getQRCodeImage(str, 200, 200);
            lblQRImage.setIcon(new ImageIcon(imgBytes));
            jTextAreaQR.setText(str);
        } catch (Exception e) {
            //TODO set an informative message
            lblQRImage.setText("ERROR: Cannot generate QR code");
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTxtServerPort = new javax.swing.JLabel();
        jtxtServerPort = new javax.swing.JTextField();
        lblTxtAESkey = new javax.swing.JLabel();
        jtxtAESKey = new javax.swing.JTextField();
        lblQRImage = new javax.swing.JLabel();
        lblTxtNetworkIface = new javax.swing.JLabel();
        jComboBoxNetworkIPs = new javax.swing.JComboBox<>();
        lblTxtNetworkIface1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaQR = new javax.swing.JTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setPreferredSize(new java.awt.Dimension(700, 500));

        lblTxtServerPort.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTxtServerPort.setText(AppLocal.getIntString("label.mobilecenta.serverportnumber")); // NOI18N
        lblTxtServerPort.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTxtServerPort.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTxtServerPort.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtServerPort.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtServerPort.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtServerPort.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtServerPort.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTxtAESkey.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTxtAESkey.setText(AppLocal.getIntString("label.mobilecenta.aeskey")); // NOI18N
        lblTxtAESkey.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTxtAESkey.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTxtAESkey.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtAESKey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtAESKey.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtAESKey.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtAESKey.setPreferredSize(new java.awt.Dimension(300, 30));

        lblQRImage.setText("QR Code");

        lblTxtNetworkIface.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTxtNetworkIface.setText(AppLocal.getIntString("label.mobilecenta.serveripaddress")); // NOI18N
        lblTxtNetworkIface.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTxtNetworkIface.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTxtNetworkIface.setPreferredSize(new java.awt.Dimension(150, 30));

        lblTxtNetworkIface1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTxtNetworkIface1.setText(AppLocal.getIntString("label.mobilecenta.qrcode")); // NOI18N
        lblTxtNetworkIface1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTxtNetworkIface1.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTxtNetworkIface1.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTxtNetworkIface1.setPreferredSize(new java.awt.Dimension(150, 30));
        lblTxtNetworkIface1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jTextAreaQR.setColumns(20);
        jTextAreaQR.setRows(5);
        jScrollPane1.setViewportView(jTextAreaQR);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(lblTxtAESkey, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblTxtServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtxtAESKey, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jtxtServerPort, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblQRImage, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblTxtNetworkIface, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBoxNetworkIPs, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(lblTxtNetworkIface1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(247, 247, 247))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxtServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTxtServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTxtAESkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtAESKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTxtNetworkIface, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxNetworkIPs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lblTxtNetworkIface1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblQRImage, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBoxNetworkIPs;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaQR;
    private javax.swing.JTextField jtxtAESKey;
    private javax.swing.JTextField jtxtServerPort;
    private javax.swing.JLabel lblQRImage;
    private javax.swing.JLabel lblTxtAESkey;
    private javax.swing.JLabel lblTxtNetworkIface;
    private javax.swing.JLabel lblTxtNetworkIface1;
    private javax.swing.JLabel lblTxtServerPort;
    // End of variables declaration//GEN-END:variables

}
