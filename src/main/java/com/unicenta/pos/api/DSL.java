package com.openbravo.pos.api;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.ThumbNailBuilder;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;


public class DSL extends DataLogicSystem {

    private static final Logger logger = Logger.getLogger("DSL");
    final ThumbNailBuilder api_thumb = new ThumbNailBuilder(100, 100, "com/openbravo/images/package.png");

    private Session s;
    private DataLogicReceipts receiptsLogic;

    public DSL() {
    }

    public void init(Session s) {
        this.s = s;
        super.init(s);

    }

    public Session getS() {
        return s;
    }

    public void setReceiptsLogic(DataLogicReceipts receiptsLogic) {
        this.receiptsLogic = receiptsLogic;
    }


    private TicketInfo getTicketInfo(String id) {
        try {
            return receiptsLogic.getSharedTicket(id);
        } catch (BasicException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    /**
     * converts strinngs like 'P.ID' into 'ID'
     *
     * @param s
     * @return
     */
    public String parseSQLColumn(String s) {

        //NOTE dot is a regex, it's escaped here
        String[] parts = s.split("\\.");
        return parts.length == 2 ? parts[1] : s;
    }

    public byte[] getImageThumb(byte[] bytes) {
        try {
            Image img = api_thumb.getThumbNail(ImageUtils.readImage(bytes));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage bimg = new BufferedImage(
                    img.getWidth(null),
                    img.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
            bimg.createGraphics().drawImage(img, 0, 0, null);
            ImageIO.write(bimg, "png", baos);
            baos.flush();
            byte[] ret = baos.toByteArray();
            baos.close();
            return ret;
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    public final Object getDBImageBytes(String tableName, String pk) {
        String query = String.format(
                "SELECT IMAGE FROM `%s` WHERE ID = ? LIMIT 1",
                tableName.replace("`", "``")
        );

        logger.warning(tableName);
        logger.warning(pk);
        logger.warning(query);

        PreparedSentence ps = new PreparedSentence(
                s,
                query,
                new SerializerWriteBasic(Datas.STRING),
                SerializerReadBytes.INSTANCE);
        try {
            Object bytes = ps.find(new String[]{pk});
            return bytes;

        } catch (BasicException e) {
            e.printStackTrace(System.out);
            return null;
        }
    }

    public final Object getDBImageThumbnail(String tableName, String pk)
            throws BasicException {

        Object bytes = getDBImageBytes(tableName, pk);
        return getImageThumb((byte[]) bytes);

    }


    public final HashMap listSharedTickets() throws BasicException {
        List<SharedTicketInfo> infoList = receiptsLogic.getSharedTicketList();
        HashMap ret = new HashMap();
        //map place id to shared ticket
        infoList.stream()
                .filter(el -> el != null)
                .forEach(el -> ret.put(el.getId(), this.getTicketInfo(el.getId())))
        ;
        System.out.print(ret);
        return ret;
    }

    public final SerializerRead getReader(String[] columnNames) {
        SerializerRead reader = new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                HashMap ret = new HashMap();
                for (int i = 0; i < columnNames.length; i++) {
                    String columnName = parseSQLColumn(columnNames[i]);

//                    logger.warning(columnNames[i]);
//                    logger.warning(columnName);

                    switch (columnName) {

                        case "PARENTID": //CATEGORIES
                            String val = dr.getString(i + 1);
                            ret.put(columnName, val == null ? null : val);
                            break;

                        case "IMAGE":
                            if (dr.getBytes(i + 1) != null) {

                                ret.put(columnName, true);

                                /*

                                // NOTE thumb works, but we replace with TRUE
                                // since cordova-file-cache does the caching job
                                // by downloading every url

                                Base64 codec = new Base64();
                                ret.put(columnNames[i], codec.encodeBase64String(
                                        getImageThumb(dr.getBytes(i + 1))
                                ));
                                 */
                            } else {
                                ret.put(columnName, null);
                            }
                            break;
                        default:
                            ret.put(columnName, "" + dr.getString(i + 1));
                            break;
                    }
                }
                return ret;
            }

        };
        return reader;
    }

    private List tryCatchList(SentenceList sl) throws BasicException {
        try {
            return sl.list();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new BasicException(e.getMessage(), e);
        }
    }


    public final List listFloors() throws BasicException {
        String[] columnNames = "ID NAME IMAGE" //IMAGE
                .split(" ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM FLOORS";

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );
        return tryCatchList(sl);
    }

    public final List listPlaces() throws BasicException {
        String[] columnNames = "ID NAME X Y FLOOR"
                .split(" ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM PLACES";

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );
        return tryCatchList(sl);
    }


    public final List listProductCategoryOrder() throws BasicException {
        String[] columnNames = "PRODUCT CATORDER".split(" ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM PRODUCTS_CAT";

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );
        return tryCatchList(sl);
    }

    public final List listTaxes() throws BasicException {
        String[] columnNames = "ID NAME CATEGORY CUSTCATEGORY PARENTID RATE RATECASCADE RATEORDER"
                .split(" ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM TAXES";

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );
        return tryCatchList(sl);
    }


    public final List listProductCategories() throws BasicException {
        String[] columnNames = "ID NAME PARENTID IMAGE".split(" ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM CATEGORIES";

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );
        return tryCatchList(sl);
    }

    public final List listProducts() throws BasicException {
        String[] columnNames = "P.ID, NAME, P.CATEGORY, TAXCAT, ISKITCHEN, PRICESELL, IMAGE".split(", ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM PRODUCTS P, PRODUCTS_CAT O WHERE P.ID = O.PRODUCT";

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );
        return tryCatchList(sl);
    }


    public final List listUsers() throws BasicException {
        String[] columnNames = "ID NAME CARD ROLE IMAGE".split(" ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM PEOPLE WHERE VISIBLE = " + s.DB.TRUE();

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );

        return tryCatchList(sl);
    }

    public final List listRoles() throws BasicException {
        String[] columnNames = "ID NAME PERMISSIONS".split(" ");
        String sql = "SELECT "
                + String.join(", ", columnNames)
                + " FROM ROLES";

        SentenceList sl = new StaticSentence(
                s, sql, null, getReader(columnNames)
        );
        return tryCatchList(sl);
    }


}
