package cn.bit.test;

import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.jdbc.IDaoSupport;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2018/2/26 0026.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class JdbcTemplateTest {

    @Autowired
    private IDaoSupport daoSupport;

    @Test
    public void test() {
        Book book = new Book("b1", "xxx");
        daoSupport.insert("book", book);
        //获取自增长ID
        Integer id = this.daoSupport.getLastId("book");
        book.setId(id);
        System.err.println(JSON.toJSONString(book));
    }

    @Test
    public void test1() {
        daoSupport.execute("delete from book where id = ?", 1);
    }

    @Test
    public void test2() {
        for (int i = 0; i < 100; i++) {
            Book book = new Book("book" + i, "xxx" + i);
            daoSupport.insert("book", book);
        }
    }

    @Test
    public void test3() {
        Page<Book> page = daoSupport.queryForPage("select * from book where id > ?", 1, 20, 5);
        System.err.println(JSON.toJSONString(page));
    }

    @Data
    public static class Book implements Serializable {

        private Integer id;
        private String title;
        private String name;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date createAt = new Date();

        public Book() {
        }

        public Book(String title, String name) {
            this.title = title;
            this.name = name;
        }
    }
}
