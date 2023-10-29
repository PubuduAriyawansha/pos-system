package lk.ijse.dep11.pos.tm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Search implements Serializable {
    private String id;
    private Date date;
    private String customer_id;
    private String customer_name;
    private BigDecimal total;
}
