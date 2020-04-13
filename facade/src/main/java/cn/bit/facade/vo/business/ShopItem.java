package cn.bit.facade.vo.business;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class ShopItem implements Serializable{

    private ObjectId id;

    private String name;

    private String logo;

    private Set<String> tag;

    private String address;

    private List<String> couponNames;

}
