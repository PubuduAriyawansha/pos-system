package lk.ijse.dep11.pos.db;



import java.sql.*;
import java.util.List;

public class OrderDataAccess {
    private static final PreparedStatement STM_EXISTS_BY_CUSTOMER_ID;
    private static final PreparedStatement STM_EXISTS_BY_ITEM_CODE;

    static {
        try {
            Connection connection = SingleConnectionDataSource.getInstance().getConnection();
            STM_EXISTS_BY_CUSTOMER_ID = connection.prepareStatement(
                    "SELECT * FROM \"order\" WHERE customer_id=?");
            STM_EXISTS_BY_ITEM_CODE=connection.prepareStatement(
                    "SELECT * FROM \"order\" WHERE id=?");


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean existsOrderByCustoemrId(String customerid) throws SQLException {
        STM_EXISTS_BY_CUSTOMER_ID.setString(1, customerid);
        return STM_EXISTS_BY_CUSTOMER_ID.executeQuery().next();
    }
    public static boolean existsOrderByItemId(String itemId) throws SQLException {
        STM_EXISTS_BY_ITEM_CODE.setString(1,itemId);
        return STM_EXISTS_BY_ITEM_CODE.executeQuery().next();
    }
}