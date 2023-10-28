package lk.ijse.dep11.pos.db;



import java.sql.*;
import java.util.List;

public class OrderDataAccess {
    private static final PreparedStatement STM_EXISTS_BY_CUSTOMER_ID;

    static {
        try {
            Connection connection = SingleConnectionDataSource.getInstance().getConnection();
            STM_EXISTS_BY_CUSTOMER_ID = connection.prepareStatement(
                    "SELECT * FROM \"order\" WHERE customer_id=?");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean existsOrderByCustoemrId(String customerid) throws SQLException {
        STM_EXISTS_BY_CUSTOMER_ID.setString(1, customerid);
        return STM_EXISTS_BY_CUSTOMER_ID.executeQuery().next();
    }
}