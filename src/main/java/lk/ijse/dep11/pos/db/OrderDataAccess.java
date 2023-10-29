package lk.ijse.dep11.pos.db;



import lk.ijse.dep11.pos.tm.OrderItem;

import java.sql.*;
import java.util.List;

public class OrderDataAccess {
    private static final PreparedStatement STM_EXISTS_BY_CUSTOMER_ID;
    private static final PreparedStatement STM_EXISTS_BY_ITEM_CODE;
    private static final PreparedStatement STM_GET_LAST_ID;
    private static final PreparedStatement STM_INSERT_ORDER;
    private static final PreparedStatement STM_INSERT_ORDER_ITEM;
    private static final PreparedStatement STM_INSERT_UPDATE_STOCK;

    static {
        try {
            Connection connection = SingleConnectionDataSource.getInstance().getConnection();
            STM_EXISTS_BY_CUSTOMER_ID = connection.prepareStatement(
                    "SELECT * FROM \"order\" WHERE customer_id=?");
            STM_EXISTS_BY_ITEM_CODE=connection.prepareStatement(
                    "SELECT * FROM \"order\" WHERE id=?");
            STM_GET_LAST_ID=connection.prepareStatement(
                    "SELECT id FROM \"order\" ORDER BY id DESC FETCH FIRST ROWS ONLY ");
            STM_INSERT_ORDER = connection.prepareStatement(
                    "INSERT INTO \"order\" (id,date,customer_id) VALUES (?,?,?)");
            STM_INSERT_ORDER_ITEM = connection.prepareStatement(
                    "INSERT INTO order_item (order_id, item_code, qty, unit_price) VALUES (?,?,?,?)");
            STM_INSERT_UPDATE_STOCK=connection.prepareStatement(
                    "UPDATE item SET qty=qty-? WHERE code = ?");


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveOrder(String orderId, Date orderdate, String custoemrID, List<OrderItem> orderItemList) throws SQLException {
        SingleConnectionDataSource.getInstance().getConnection().setAutoCommit(false);

        try {
            //save order
            STM_INSERT_ORDER.setString(1,orderId);
            STM_INSERT_ORDER.setDate(2,orderdate);
            STM_INSERT_ORDER.setString(3,custoemrID);
            STM_INSERT_ORDER.executeUpdate();

            //save orderItemList
            for (OrderItem orderItem : orderItemList) {
                STM_INSERT_ORDER_ITEM.setString(1,orderId);
                STM_INSERT_ORDER_ITEM.setString(2,orderItem.getCode());
                STM_INSERT_ORDER_ITEM.setInt(3,orderItem.getQty());
                STM_INSERT_ORDER_ITEM.setBigDecimal(4,orderItem.getUnit_price());
                STM_INSERT_ORDER_ITEM.executeUpdate();

                STM_INSERT_UPDATE_STOCK.setInt(1,orderItem.getQty());
                STM_INSERT_UPDATE_STOCK.setString(2,orderItem.getCode());
                STM_INSERT_UPDATE_STOCK.executeUpdate();
            }

            //update stock


            SingleConnectionDataSource.getInstance().getConnection().commit();
        } catch (Throwable e){
            SingleConnectionDataSource.getInstance().getConnection().rollback();
            e.printStackTrace();
        } finally {
            SingleConnectionDataSource.getInstance().getConnection().setAutoCommit(true);
        }
    }

    public static boolean existsOrderByCustomerId(String customerid) throws SQLException {
        STM_EXISTS_BY_CUSTOMER_ID.setString(1, customerid);
        return STM_EXISTS_BY_CUSTOMER_ID.executeQuery().next();
    }
    public static boolean existsOrderByItemId(String itemId) throws SQLException {
        STM_EXISTS_BY_ITEM_CODE.setString(1,itemId);
        return STM_EXISTS_BY_ITEM_CODE.executeQuery().next();
    }
    public static String getLastOrderId() throws SQLException {
        ResultSet rst = STM_GET_LAST_ID.executeQuery();
        return rst.next()? rst.getString(1) : null;
    }
}