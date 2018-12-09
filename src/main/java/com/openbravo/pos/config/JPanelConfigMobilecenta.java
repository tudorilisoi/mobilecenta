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

import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.AltEncrypter;

import java.awt.Component;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Random;

/**
 * @author JG uniCenta
 */
public class JPanelConfigMobilecenta extends javax.swing.JPanel implements PanelConfig {

    private final DirtyManager dirty = new DirtyManager();


    /**
     *
     */
    public JPanelConfigMobilecenta() {

        initComponents();

        jtxtServerPort.getDocument().addDocumentListener(dirty);
        jtxtAESKey.getDocument().addDocumentListener(dirty);
        jtxtTktHeader3.getDocument().addDocumentListener(dirty);
        jtxtTktHeader4.getDocument().addDocumentListener(dirty);
        jtxtTktHeader5.getDocument().addDocumentListener(dirty);
        jtxtTktHeader6.getDocument().addDocumentListener(dirty);

        jtxtTktFooter1.getDocument().addDocumentListener(dirty);
        jtxtTktFooter2.getDocument().addDocumentListener(dirty);
        jtxtTktFooter3.getDocument().addDocumentListener(dirty);
        jtxtTktFooter4.getDocument().addDocumentListener(dirty);
        jtxtTktFooter5.getDocument().addDocumentListener(dirty);
        jtxtTktFooter6.getDocument().addDocumentListener(dirty);

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

    /**
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {

        jtxtServerPort.setText(config.getProperty("mobilecenta.server_port"));
        String aesKey = config.getProperty("mobilecenta.aes_private_key");
        if (aesKey != null && aesKey.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey");
            aesKey = cypher.decrypt(aesKey.substring(6));
        }
        if (aesKey == null) {
            aesKey = generateAESKey();
        }
        jtxtAESKey.setText(aesKey);

        jtxtTktHeader3.setText(config.getProperty("tkt.header3"));
        jtxtTktHeader4.setText(config.getProperty("tkt.header4"));
        jtxtTktHeader5.setText(config.getProperty("tkt.header5"));
        jtxtTktHeader6.setText(config.getProperty("tkt.header6"));

        jtxtTktFooter1.setText(config.getProperty("tkt.footer1"));
        jtxtTktFooter2.setText(config.getProperty("tkt.footer2"));
        jtxtTktFooter3.setText(config.getProperty("tkt.footer3"));
        jtxtTktFooter4.setText(config.getProperty("tkt.footer4"));
        jtxtTktFooter5.setText(config.getProperty("tkt.footer5"));
        jtxtTktFooter6.setText(config.getProperty("tkt.footer6"));

        dirty.setDirty(false);

    }

    /**
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {

        config.setProperty("mobilecenta.server_port", jtxtServerPort.getText());

        String aesKey = jtxtAESKey.getText();
        AltEncrypter cypher = new AltEncrypter("cypherkey");
        config.setProperty("mobilecenta.aes_private_key", "crypt:" +
                cypher.encrypt(new String(aesKey)));

        config.setProperty("tkt.header3", jtxtTktHeader3.getText());
        config.setProperty("tkt.header4", jtxtTktHeader4.getText());
        config.setProperty("tkt.header5", jtxtTktHeader5.getText());
        config.setProperty("tkt.header6", jtxtTktHeader6.getText());

        config.setProperty("tkt.footer1", jtxtTktFooter1.getText());
        config.setProperty("tkt.footer2", jtxtTktFooter2.getText());
        config.setProperty("tkt.footer3", jtxtTktFooter3.getText());
        config.setProperty("tkt.footer4", jtxtTktFooter4.getText());
        config.setProperty("tkt.footer5", jtxtTktFooter5.getText());
        config.setProperty("tkt.footer6", jtxtTktFooter6.getText());

        dirty.setDirty(false);
    }


    private String generateAESKey() {
        byte[] array = new byte[10]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = Base64.getEncoder().encodeToString(array);
        //cut the == padding
        return generatedString.substring(0, generatedString.length()-2);
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
        lblTktHeader3 = new javax.swing.JLabel();
        jtxtTktHeader3 = new javax.swing.JTextField();
        lblTktHeader4 = new javax.swing.JLabel();
        jtxtTktHeader4 = new javax.swing.JTextField();
        lblTktHeader5 = new javax.swing.JLabel();
        jtxtTktHeader5 = new javax.swing.JTextField();
        lblTktHeader6 = new javax.swing.JLabel();
        jtxtTktHeader6 = new javax.swing.JTextField();
        lblTktFooter1 = new javax.swing.JLabel();
        jtxtTktFooter1 = new javax.swing.JTextField();
        lblTktFooter2 = new javax.swing.JLabel();
        jtxtTktFooter2 = new javax.swing.JTextField();
        lblTktFooter3 = new javax.swing.JLabel();
        jtxtTktFooter3 = new javax.swing.JTextField();
        lblTktFooter4 = new javax.swing.JLabel();
        jtxtTktFooter4 = new javax.swing.JTextField();
        lblTktFooter5 = new javax.swing.JLabel();
        jtxtTktFooter5 = new javax.swing.JTextField();
        lblTktFooter6 = new javax.swing.JLabel();
        jtxtTktFooter6 = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setPreferredSize(new java.awt.Dimension(700, 500));

        lblTxtServerPort.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTxtServerPort.setText(AppLocal.getIntString("label.tktheader1")); // NOI18N
        lblTxtServerPort.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTxtServerPort.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTxtServerPort.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtServerPort.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtServerPort.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtServerPort.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtServerPort.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTxtAESkey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTxtAESkey.setText(AppLocal.getIntString("label.tktheader2")); // NOI18N
        lblTxtAESkey.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTxtAESkey.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTxtAESkey.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtAESKey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtAESKey.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtAESKey.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtAESKey.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktHeader3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktHeader3.setText(AppLocal.getIntString("label.tktheader3")); // NOI18N
        lblTktHeader3.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktHeader3.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktHeader3.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktHeader3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader3.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader3.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader3.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktHeader4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktHeader4.setText(AppLocal.getIntString("label.tktheader4")); // NOI18N
        lblTktHeader4.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktHeader4.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktHeader4.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktHeader4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader4.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader4.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader4.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktHeader5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktHeader5.setText(AppLocal.getIntString("label.tktheader5")); // NOI18N
        lblTktHeader5.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktHeader5.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktHeader5.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktHeader5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader5.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader5.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader5.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktHeader6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktHeader6.setText(AppLocal.getIntString("label.tktheader6")); // NOI18N
        lblTktHeader6.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktHeader6.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktHeader6.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktHeader6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader6.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader6.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader6.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktFooter1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTktFooter1.setText(AppLocal.getIntString("label.tktfooter1")); // NOI18N
        lblTktFooter1.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktFooter1.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktFooter1.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktFooter1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter1.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter1.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter1.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktFooter2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktFooter2.setText(AppLocal.getIntString("label.tktfooter2")); // NOI18N
        lblTktFooter2.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktFooter2.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktFooter2.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktFooter2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter2.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter2.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter2.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktFooter3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktFooter3.setText(AppLocal.getIntString("label.tktfooter3")); // NOI18N
        lblTktFooter3.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktFooter3.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktFooter3.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktFooter3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter3.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter3.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter3.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktFooter4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktFooter4.setText(AppLocal.getIntString("label.tktfooter4")); // NOI18N
        lblTktFooter4.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktFooter4.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktFooter4.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktFooter4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter4.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter4.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter4.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktFooter5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktFooter5.setText(AppLocal.getIntString("label.tktfooter5")); // NOI18N
        lblTktFooter5.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktFooter5.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktFooter5.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktFooter5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter5.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter5.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter5.setPreferredSize(new java.awt.Dimension(300, 30));

        lblTktFooter6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTktFooter6.setText(AppLocal.getIntString("label.tktfooter6")); // NOI18N
        lblTktFooter6.setToolTipText("");
        lblTktFooter6.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktFooter6.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktFooter6.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTktFooter6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter6.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter6.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter6.setPreferredSize(new java.awt.Dimension(300, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblTxtServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTktFooter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblTxtAESkey, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktFooter2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktFooter4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktFooter3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktFooter5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktFooter6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktHeader3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktHeader4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTktHeader5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(lblTktHeader6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jtxtTktFooter5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtTktFooter4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtTktFooter3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtTktFooter2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtTktFooter1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtTktHeader6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktHeader5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktHeader4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktHeader3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtAESKey, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtServerPort, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktFooter6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblTxtServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblTxtAESkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtAESKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jtxtTktHeader3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTktHeader3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jtxtTktHeader4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTktHeader4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jtxtTktHeader5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTktHeader5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jtxtTktHeader6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTktHeader6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTktFooter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktFooter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTktFooter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktFooter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTktFooter3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktFooter3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTktFooter4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktFooter4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTktFooter5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktFooter5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTktFooter6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtTktFooter6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jtxtAESKey;
    private javax.swing.JTextField jtxtServerPort;
    private javax.swing.JTextField jtxtTktFooter1;
    private javax.swing.JTextField jtxtTktFooter2;
    private javax.swing.JTextField jtxtTktFooter3;
    private javax.swing.JTextField jtxtTktFooter4;
    private javax.swing.JTextField jtxtTktFooter5;
    private javax.swing.JTextField jtxtTktFooter6;
    private javax.swing.JTextField jtxtTktHeader3;
    private javax.swing.JTextField jtxtTktHeader4;
    private javax.swing.JTextField jtxtTktHeader5;
    private javax.swing.JTextField jtxtTktHeader6;
    private javax.swing.JLabel lblTktFooter1;
    private javax.swing.JLabel lblTktFooter2;
    private javax.swing.JLabel lblTktFooter3;
    private javax.swing.JLabel lblTktFooter4;
    private javax.swing.JLabel lblTktFooter5;
    private javax.swing.JLabel lblTktFooter6;
    private javax.swing.JLabel lblTktHeader3;
    private javax.swing.JLabel lblTktHeader4;
    private javax.swing.JLabel lblTktHeader5;
    private javax.swing.JLabel lblTktHeader6;
    private javax.swing.JLabel lblTxtAESkey;
    private javax.swing.JLabel lblTxtServerPort;
    // End of variables declaration//GEN-END:variables

}
