package lk.ijse.dep11.pos.db;

import lk.ijse.dep11.pos.tm.Search;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchAccessDataSource {
    private static final PreparedStatement STM_EXISTS_ORDER_ID;
    private static final PreparedStatement STM_LOAD_ALL;

    static {
        try{
            Connection connection = SingleConnectionDataSource.getInstance().getConnection();
            STM_LOAD_ALL=connection.prepareStatement("SELECT o.id,o.date,c.id,c.name,orderTotal.total FROM \"order\" AS o\n" +
                    "    INNER JOIN customer AS c on c.id = o.customer_id\n" +
                    "        INNER JOIN\n" +
                    "    (SELECT o.id,SUM(oi.unit_price*oi.qty) AS total FROM \"order\" AS o INNER JOIN order_item oi " +
                    "on o.id = oi.order_id GROUP BY o.id) AS orderTotal ON o.id=orderTotal.id;");
            STM_EXISTS_ORDER_ID=connection.prepareStatement("SELECT o.id,o.date,c.id,c.name,orderTotal.total FROM \"order\" AS o\n" +
                    "    INNER JOIN customer AS c on c.id = o.customer_id\n" +
                    "        INNER JOIN\n" +
                    "    (SELECT o.id,SUM(oi.unit_price*oi.qty) AS total FROM \"order\" AS o \n" +
                    "        INNER JOIN order_item oi on o.id = oi.order_id GROUP BY o.id) AS orderTotal ON o.id=orderTotal.id\n" +
                    "WHERE o.id LIKE ? OR CAST(o.date AS varchar(20))  LIKE ? OR c.id LIKE ? OR c.name LIKE ? ;");
        } catch (SQLException e){
            throw new RuntimeException();
        }
    }

    public static List<Search> searchItem(String input) throws SQLException {
        STM_EXISTS_ORDER_ID.setString(1,"%"+input+"%");
        STM_EXISTS_ORDER_ID.setString(2,"%"+input+"%");
        STM_EXISTS_ORDER_ID.setString(3,"%"+input+"%");
        STM_EXISTS_ORDER_ID.setString(4,"%"+input+"%");
        ResultSet rst = STM_EXISTS_ORDER_ID.executeQuery();
        List<Search> searchList = new ArrayList<>();
        while (rst.next()){
            String orderID = rst.getString(1);
            Date date = rst.getDate(2);
            String customerId = rst.getString(3);
            String customerName = rst.getString(4);
            BigDecimal total = rst.getBigDecimal(5);
            searchList.add(new Search(orderID,date,customerId,customerName,total));
        }
        return searchList;
    }
    public static List<Search> loadAll() throws SQLException {
        List <Search> searchList = new ArrayList<>();
        ResultSet rst = STM_LOAD_ALL.executeQuery();
        while (rst.next()){
            String orderID = rst.getString(1);
            Date date = rst.getDate(2);
            String customerID = rst.getString(3);
            String name = rst.getString(4);
            BigDecimal total = rst.getBigDecimal(5);
            searchList.add(new Search(orderID,date,customerID,name,total));
        }
        return searchList;

    }
}
