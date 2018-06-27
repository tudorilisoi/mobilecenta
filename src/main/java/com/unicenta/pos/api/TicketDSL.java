package com.unicenta.pos.api;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.ticket.TicketInfo;

public class TicketDSL extends BaseDSL {
    private static TicketDSL instance = null;

    public static TicketDSL getInstance() {
        if (instance == null) {
            instance = new TicketDSL();
        }
        return instance;
    }

    public TicketDSL() {
    }

    public TicketInfo getTicketByPlaceID(String ID) {
        TicketInfo ticket = null;
        try {
            ticket = receiptsLogic.getSharedTicket(ID);
            if (ticket == null) {

                ticket = new TicketInfo();
                receiptsLogic.insertRSharedTicket(ID, ticket, 0);
            }
        } catch (BasicException e) {
            //TODO handle this
        }
        return ticket;
    }
}
