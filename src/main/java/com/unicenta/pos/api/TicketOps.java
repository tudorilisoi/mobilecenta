/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unicenta.pos.api;

import bsh.EvalError;
import bsh.Interpreter;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tudor
 */
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
