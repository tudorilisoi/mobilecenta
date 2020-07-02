package com.unicenta.pos.api;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

public class UIHelpers {

    private static final Logger logger = Logger.getLogger("com.openbravo.pos.api.UIHelpers");

    public static void attachClickHandler(JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                logger.warning("Label clicked");
            }
        });
    }
}
