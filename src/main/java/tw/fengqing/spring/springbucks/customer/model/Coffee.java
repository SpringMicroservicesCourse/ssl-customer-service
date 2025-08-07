package tw.fengqing.spring.springbucks.customer.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.Money;
import tw.fengqing.spring.springbucks.customer.support.MoneyDeserializer;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coffee implements Serializable {
    private Long id;
    private String name;
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Money price;
    private Date createTime;
    private Date updateTime;
}
