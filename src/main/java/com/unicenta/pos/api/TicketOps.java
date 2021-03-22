/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unicenta.pos.api;

import bsh.EvalError;
import bsh.Interpreter;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentInfoCash;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.JPanelTicket;
import com.openbravo.pos.sales.TaxesException;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.sales.restaurant.RestaurantDBUtils;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tudor
 */
@Slf4j
public class TicketOps {

    private final JRootApp app;

    private final DSL dsl;
    private final TicketInfo ticketInfo;
    private final AppUser user;
    private final String placeID;
    private final TicketParser m_TTP2;

    public TicketOps(JRootApp app, DSL dsl, TicketInfo ticketInfo, AppUser user, String placeID) {
        this.app = app;
        this.dsl = dsl;
        this.ticketInfo = ticketInfo;
        this.user = user;
        this.placeID = placeID;
        m_TTP2 = new TicketParser(app.getDeviceTicket(), dsl.systemLogic);
    }

    public boolean closeTicket(TicketInfo ticket, Object ticketext) {
        /*if (listener != null) {
            listener.stop();
        }*/
        boolean resultok = false;
        AppConfig m_config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        m_config.load();
        RestaurantDBUtils restDB = new RestaurantDBUtils(app);

//        if (user.hasPermission("sales.Total")) {
        if (true) {

//            warrantyCheck(ticket);

            try {
//                java.util.List<TaxInfo> taxlist = senttax.list();
                TaxesLogic taxeslogic = dsl.taxesLogic;
//                String ticketext = "Foo";

                taxeslogic.calculateTaxes(ticket);
                if (ticket.getTotal() >= 0.0) {
                    ticket.resetPayments();
                }

//                if (executeEvent(ticket, ticketext, "ticket.total") == null) {
                if (true) {
//                    if (listener != null) {
//                        listener.stop();
//                    }

                    printTicket("Printer.TicketTotal", ticket, ticketext.toString());

//                    JPaymentSelect paymentdialog = ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
//                            ? paymentdialogreceipt
//                            : paymentdialogrefund;
//                    paymentdialog.setPrintSelected("true".equals(m_jbtnconfig.getProperty("printselected", "true")));
//
//                    paymentdialog.setTransactionID(ticket.getTransactionID());

//                    if (paymentdialog.showDialog(ticket.getTotal(), ticket.getCustomer())) {

//                    ticket.setPayments(paymentdialog.getSelectedPayments());

                    List<PaymentInfo> payments = new ArrayList<>();
                    double amount = ticket.getTotal();
                    payments.add(new PaymentInfoCash(amount, amount));
                    ticket.setPayments(payments);


                    ticket.setUser(user.getUserInfo());
                    ticket.setActiveCash(app.getActiveCashIndex());
                    ticket.setDate(new Date());
                    log.debug("TICKET", ticketInfo);
                    log.warn("TICKET ID " + ticketInfo.getId());
//                    if (true) {
//                        return true;
//                    }

//                        if (executeEvent(ticket, ticketext, "ticket.save") == null) {

                    try {
                        ticket.setTicketType(TicketInfo.RECEIPT_NORMAL);
                        dsl.salesLogic.saveTicket(ticket, app.getInventoryLocation());
                        m_config.setProperty("lastticket.number", Integer.toString(ticket.getTicketId()));
                        m_config.setProperty("lastticket.type", Integer.toString(ticket.getTicketType()));
                        m_config.save();

                    } catch (BasicException eData) {
//                        MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
//                        msg.show(this);
                        log.error(eData.getMessage());
                    } catch (IOException ex) {
                        log.error(ex.getMessage());
                    }

//                    executeEvent(ticket, ticketext, "ticket.close",
//                            new JPanelTicket.ScriptArg("print", paymentdialog.isPrintSelected()));

//                    printTicket(paymentdialog.isPrintSelected() || warrantyPrint
//                            ? "Printer.Ticket"
//                            : "Printer.Ticket2", ticket, ticketext);

                    printTicket("Printer.Ticket", ticket, ticketext.toString());
                    printTicket("Printer.Ticket2", ticket, ticketext.toString());
//                    Notify(AppLocal.getIntString("notify.printing"));
//
                    resultok = true;

                    if ("restaurant".equals(app.getProperties()
                            .getProperty("machine.ticketsbag")) && !ticket.getOldTicket()) {
                        /* Deliberately Explicit to allow for reassignments - future
                         * even though we could do a single SQL statment sweep for reset
                         */
                        restDB.clearCustomerNameInTable(ticketext.toString());
                        restDB.clearWaiterNameInTable(ticketext.toString());
                        restDB.clearTicketIdInTable(ticketext.toString());
                    }
//                        }
//                    }
                }
            } catch (TaxesException e) {
//                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
//                        AppLocal.getIntString("message.cannotcalculatetaxes"));
//                msg.show(this);
                resultok = false;
            }

//            m_oTicket.resetTaxes();
//            m_oTicket.resetPayments();
//
//            jCheckStock.setText("");

        }

        return resultok;
    }

    public String getPickupString(TicketInfo pTicket) {
        if (pTicket == null) {
            return ("0");
        }
        String tmpPickupId = Integer.toString(pTicket.getPickupId());
        String pickupSize = (app.getProperties().getProperty("till.pickupsize"));

        if (pickupSize != null && (Integer.parseInt(pickupSize) >= tmpPickupId.length())) {
            while (tmpPickupId.length() < (Integer.parseInt(pickupSize))) {
                tmpPickupId = "0" + tmpPickupId;
            }
        }
        return (tmpPickupId);
    }

    /**
     * @param resource
     */
    public void printTicket(String resource) {
        printTicket(resource, ticketInfo, placeID);
    }

    private void printTicket(String sresourcename, TicketInfo ticket, String table) {
        if (ticket != null) {

            if (ticket.getPickupId() == 0) {
                try {
                    ticket.setPickupId(dsl.salesLogic.getNextPickupIndex());
                } catch (BasicException e) {
                    ticket.setPickupId(0);
                }
            }

            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);

                script.put("ticket", ticket);
                script.put("place", getPlaceName(placeID));
                script.put("pickupid", getPickupString(ticket));

                m_TTP2.printTicket(script.eval(dsl.systemLogic.getResourceAsXML(sresourcename)).toString());

            } catch (BasicException | ScriptException | TicketPrinterException e) {
                Logger.getLogger(TicketOps.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public String getPlaceName(String placeID) throws BasicException {
        List<HashMap> places = dsl.listPlaces();
        HashMap place = places.stream()
                .filter(p -> placeID.equals(p.get("ID")))
                .findFirst()
                .orElse(null);
        return place == null ? null : (String) place.get("NAME");
    }

    public void printToKitchen() {
        String rScript = (dsl.getResourceAsText("script.SendOrder"));

        Interpreter i = new Interpreter();
        try {

            i.set("ticket", ticketInfo);
            i.set("place", getPlaceName(placeID));
            i.set("user", user);
            i.set("sales", this);
            i.set("pickupid", ticketInfo.getPickupId());
            Object result = i.eval(rScript);
        } catch (Exception ex) {
            Logger.getLogger(TicketOps.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
