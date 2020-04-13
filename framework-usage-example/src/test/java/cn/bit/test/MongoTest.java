package cn.bit.test;

import cn.bit.example.dao.BookRepository;
import cn.bit.example.model.Book;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class MongoTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void testAllIgnoreNull() {
        Assert.assertNotNull(bookRepository.findByNameAndTitleAllIgnoreNull("发现新大陆", null));
    }

    @Test
    public void testIgnoreNull() {
        Assert.assertNotNull(bookRepository.findByNameAndTitleIgnoreNull("发现新大陆", null));
    }

    @Test
    public void testJPAUpsertWithSetOnInsert() {
        Book book = new Book();
        book.setName("发现新大陆");
        book.setTitle("一个");
        book.setCreateAt(new Date());
        book.setUpdateAt(book.getCreateAt());

        Assert.assertNotNull(bookRepository.upsertWithSetOnInsertNameAndCreateAtAndTitleByName(book, book.getName()));
    }

    @Test
    public void testJPAUpdateWithUnsetIfNull() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        Assert.assertNull(bookRepository.updateWithUnsetIfNullTitleByName(book, "发现新大陆").getTitle());
    }

    @Test
    public void testJPAUpsert() {
        Book book = new Book();
        book.setName("发现新大陆");
        book.setTitle("一个孤独旅行者的故事");
        book.setCreateAt(new Date());
        book.setUpdateAt(book.getCreateAt());

        Assert.assertNotNull(bookRepository.upsertByName(book, book.getName()));
    }

    @Test
    public void testJPAUpdateWithUnset() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        Assert.assertNull(bookRepository.updateWithUnsetTitleAndVersionByName(book, "发现新大陆").getTitle());
    }

    @Test
    public void testJPAUpdateWithAddToSet() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setAuthors(new HashSet<>(Arrays.asList("A", "B", "C")));

        Assert.assertTrue(bookRepository.updateWithAddToSetAuthorsByName(book,
                "发现新大陆").getAuthors().containsAll(book.getAuthors()));
    }

    @Test
    public void testJPAUpdateWithPush() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setAuthors(new HashSet<>(Arrays.asList("D")));

        Assert.assertTrue(bookRepository.updateWithPushAuthorsByName(book,
                "发现新大陆").getAuthors().containsAll(book.getAuthors()));
    }

    @Test
    public void testJPAUpdateWithPull() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setAuthors(new HashSet<>(Arrays.asList("D")));

        Assert.assertFalse(bookRepository.updateWithPullAuthorsByName(book,
                "发现新大陆").getAuthors().containsAll(book.getAuthors()));
    }

    @Test
    public void testJPAUpdateWithPushAll() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setAuthors(new HashSet<>(Arrays.asList("E", "F")));

        Assert.assertTrue(bookRepository.updateWithPushAllAuthorsByName(book,
                "发现新大陆").getAuthors().containsAll(book.getAuthors()));
    }

    @Test
    public void testJPAUpdateWithPullAll() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setAuthors(new HashSet<>(Arrays.asList("E", "F")));

        Assert.assertFalse(bookRepository.updateWithPullAllAuthorsByName(book,
                "发现新大陆").getAuthors().containsAll(book.getAuthors()));
    }

    @Test
    public void testJPAUpdateWithPopFirst() {
        Book book = new Book();
        book.setUpdateAt(new Date());

        Assert.assertFalse(bookRepository.updateWithPopFirstAuthorsByName(book,
                "发现新大陆").getAuthors().contains("A"));
    }

    @Test
    public void testJPAUpdateWithPopLast() {
        Book book = new Book();
        book.setUpdateAt(new Date());

        Assert.assertFalse(bookRepository.updateWithPopLastAuthorsByName(book,
                "发现新大陆").getAuthors().contains("C"));
    }

    @Test
    public void testJPAUpdateWithInc() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setVersion(1);

        Assert.assertEquals(1L, bookRepository.updateWithIncVersionByName(book, "发现新大陆").getVersion().longValue());
    }

    @Test
    public void testJPAUpdateWithMin() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setVersion(1);

        Assert.assertEquals(1L, bookRepository.updateWithMinVersionByName(book, "发现新大陆").getVersion().longValue());
    }

    @Test
    public void testJPAUpdateWithMax() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setVersion(99);

        Assert.assertEquals(99L, bookRepository.updateWithMaxVersionByName(book, "发现新大陆").getVersion().longValue());
    }

    @Test
    public void testJPAUpdateWithMul() {
        Book book = new Book();
        book.setUpdateAt(new Date());
        book.setVersion(0);

        Assert.assertEquals(0, bookRepository.updateWithMulVersionByName(book, "发现新大陆").getVersion().longValue());
    }

    @Test
    public void testJPAUpdateWithCurrentDate() {
        Book book = new Book();
        book.setVersion(2);

        Assert.assertNotNull(bookRepository.updateWithCurrentDateUpdateAtByName(book, "发现新大陆"));
    }

    @Test
    public void testJPAUpdateWithCurrentTimestamp() {
        Book book = new Book();
        book.setUpdateAt(new Date());

        Assert.assertNotNull(bookRepository.updateWithCurrentTimestampTimestampByName(book, "发现新大陆"));
    }

    @Test
    public void testJPAUpdateWithMixFieldOperations() {
        Book book = bookRepository.findByNameAndTitleIgnoreNull("发现新大陆", null);
        Integer oldVersion = book.getVersion();

        book.setVersion(1);

        book = bookRepository.updateWithIncVersionThenCurrentDateCreateAtAndUpdateAtByName(
                book, "发现新大陆");

        Assert.assertEquals(oldVersion + 1, book.getVersion().intValue());
        Assert.assertEquals(book.getCreateAt(), book.getUpdateAt());
    }
}
