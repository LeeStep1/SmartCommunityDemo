package cn.bit.example.dao;

import cn.bit.example.model.Book;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, ObjectId> {

    /**
     * 在By后面任意位置指定AllIgnoreNull标识，将会把所有null值的查询条件忽略
     * <p>
     *     例如，findByNameAndTitleAllIgnoreNull(null, "a")，对应产生的query为{"title" : "1"}，name字段对应的查询值为null，被忽略
     * </p>
     *
     * <p>
     *     在By后的指定查询条件中指定IgnoreNull，该查询条件将会在所对应的字段值为null时被忽略
     * </p>
     * <p>
     *     例如，findByNameIgnoreNullAndTitle(null, null)，对应产生的query为{"title" : null}，name查询条件指定IgnoreNull标识，
     *     字段对应的查询值为null，被忽略
     * </p>
     *
     * @param name
     * @param title
     * @return
     */
    Book findByNameAndTitleAllIgnoreNull(String name, String title);

    Book findByNameAndTitleIgnoreNull(String name, String title);

    /**
     * 格式为upsert[|update][WithFieldOperationFieldA[AndFieldB...][ThenFieldOperationFieldC[AndFieldD...]...]]ByField1[AndField2...](entity, field1, field2, ...)
     * <ul>
     *     <li>
     *         其中By后面的查询条件跟JPA查询方法一致，详见{@link org.springframework.data.repository.query.parser.Part.Type}
     *     </li>
     *     <li>
     *         内置的FieldOperation详见{@link org.springframework.data.mongodb.repository.query.parser.FieldOperation.Type}
     *     </li>
     *     <li>
     *         entity中被方法名中的FieldOperation指定的字段，会按照所指定的FieldOperation操作进行更新；
     *         其余的非null值字段会默认按照$set操作进行更新，null值字段将被忽略
     *     </li>
     * </ul>
     *
     * @param toUpsert
     * @param name
     * @return
     */
    Book upsertWithSetOnInsertNameAndCreateAtAndTitleByName(Book toUpsert, String name);

    Book updateWithUnsetTitleAndVersionByName(Book toUpdate, String name);

    Book updateWithUnsetIfNullTitleByName(Book toUpdate, String name);

    Book upsertByName(Book toUpsert, String name);

    Book updateWithAddToSetAuthorsByName(Book toUpdate, String name);

    Book updateWithPushAuthorsByName(Book toUpdate, String name);

    Book updateWithPullAuthorsByName(Book toUpdate, String name);

    Book updateWithPushAllAuthorsByName(Book toUpdate, String name);

    Book updateWithPullAllAuthorsByName(Book toUpdate, String name);

    Book updateWithPopFirstAuthorsByName(Book toUpdate, String name);

    Book updateWithPopLastAuthorsByName(Book toUpdate, String name);

    Book updateWithIncVersionByName(Book toUpdate, String name);

    Book updateWithMinVersionByName(Book toUpdate, String name);

    Book updateWithMaxVersionByName(Book toUpdate, String name);

    Book updateWithMulVersionByName(Book toUpdate, String name);

    Book updateWithCurrentDateUpdateAtByName(Book toUpdate, String name);

    Book updateWithCurrentTimestampTimestampByName(Book toUpdate, String name);

    Book updateWithIncVersionThenCurrentDateCreateAtAndUpdateAtByName(Book toUpdate, String name);

}
