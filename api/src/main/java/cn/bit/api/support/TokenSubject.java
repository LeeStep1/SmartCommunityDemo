package cn.bit.api.support;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * Created by terry on 2018/1/15.
 */
@Data
public class TokenSubject implements Serializable{
    private String token;
    private ObjectId uid;

    public TokenSubject() {
    }

    public TokenSubject(String token, ObjectId uid) {
        this.token = token;
        this.uid = uid;
    }
}
