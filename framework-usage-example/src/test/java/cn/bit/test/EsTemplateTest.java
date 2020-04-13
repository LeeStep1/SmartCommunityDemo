package cn.bit.test;

import cn.bit.framework.data.common.BaseEntity;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Created by Administrator on 2018/1/23 0023.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class EsTemplateTest {

    @Autowired
    private EsTemplate esTemplate;

    /**
     * 测试插入
     */
    @Test
    public void testInsert() {

        String id = esTemplate.insert("book", "book", UUID.randomUUID().toString(), new Book("xxx", "xxx", 10245));
        System.err.println("id >>>>>>> " + id);
        //插入数据，id可以不赋值，es会自动生成
        id = esTemplate.insert("book", "book", null, new Book("xxxx", "xxxx", 10254));
        System.err.println("id >>>>>>> " + id);

    }
    /**
     * 测试异步插入
     */
    @Test
    public void testInsertAsync() throws InterruptedException {
        esTemplate.insertAsync("book", "book", null, new Book("xxxxx", "xxxxe", 10256));
        Thread.sleep(10000);
    }

    @Test
    public void testUpdate(){
        esTemplate.update("book", "book","AWEhrRZtosO4ccVfUgEf",new Book("ssss", "ssss", 10236));
    }

    @Data
    private static class Book extends BaseEntity {
        private String title;
        private String author;
        private long words;

        public Book() {
        }

        public Book(String title, String author, long words) {
            this.title = title;
            this.author = author;
            this.words = words;
        }
    }
}
