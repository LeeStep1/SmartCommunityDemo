package cn.bit.example.model;

import lombok.Data;
import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "book")
public class Book implements Serializable {

    private ObjectId id;
    private String title;
    private String name;
    private Set<String> authors;
    private Integer version;
    private Date createAt;
    private Date updateAt;
    private BSONTimestamp timestamp;

    public Book() {
    }

    public Book(String title, String name) {
        this.title = title;
        this.name = name;
    }
}
