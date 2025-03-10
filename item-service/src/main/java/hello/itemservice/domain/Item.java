package hello.itemservice.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Item {

    private Long id;
    private String itemName;
    private Integer price;
    private Integer quantity = 0;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
