package com.unicenta.pos.api;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.sales.restaurant.RestaurantDBUtils;
import com.openbravo.pos.ticket.TicketInfo;

import java.util.Date;

public class TicketDSL extends BaseDSL {


    /*private static TicketDSL instance = null;
    public static TicketDSL getInstance() {
        if (instance == null) {
            instance = new TicketDSL();
        }
        return instance;
    }*/

    public TicketDSL() {
    }

    public TicketInfo getTicketByPlaceID(String placeID) {

        String ticketID = restDB.getTicketIdInTable(placeID);
        TicketInfo ticket = null;
        try {
            ticket = receiptsLogic.getSharedTicket(placeID);
            if (ticket == null) {

                ticket = new TicketInfo();
//                ticket.setActiveCash(app.getActiveCashIndex());
                ticket.setDate(new Date());
                receiptsLogic.insertRSharedTicket(placeID, ticket, 0);
                restDB.setTicketIdInTableId(ticket.getId(), placeID);
            }
        } catch (BasicException e) {
            //TODO handle this
        }
        return ticket;
    }
}
