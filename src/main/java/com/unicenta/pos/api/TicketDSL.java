package com.unicenta.pos.api;

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

    public TicketInfo getTicketByPlaceID() {
        TicketInfo ticket = new TicketInfo();
        return ticket;
    }
}
