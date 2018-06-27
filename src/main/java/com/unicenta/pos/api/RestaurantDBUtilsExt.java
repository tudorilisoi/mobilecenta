package com.unicenta.pos.api;

import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.sales.restaurant.RestaurantDBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class RestaurantDBUtilsExt extends RestaurantDBUtils {

    private static final Logger logger = Logger.getLogger("DSL");

    //NOTE properties are copied from parent class
    private Session s;
    private Connection con;
    private Statement stmt;
    private PreparedStatement pstmt;
    private String SQL;
    private ResultSet rs;
    private AppView m_App;

    protected DataLogicSystem dlSystem;

    /**
     * @param oApp
     */
    public RestaurantDBUtilsExt(AppView oApp) {
        super(oApp);
    }

    // NOTE this seems to have something to do with split or move
    // not clear atm
    public void setTicketIdInTableId(String TicketID, String tableID) {
        try {
            SQL = "UPDATE places SET TICKETID=? WHERE ID=?";
            pstmt = con.prepareStatement(SQL);
            pstmt.setString(1, TicketID);
            pstmt.setString(2, tableID);
            pstmt.executeUpdate();
        } catch (Exception e) {
            logger.warning("UPDATE ERROR:" + e.toString());
        }
    }
}
