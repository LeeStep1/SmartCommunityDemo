package cn.bit.user;

import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.user.CommunityUser;
import cn.bit.facade.model.user.User;
import cn.bit.user.dao.CardRepository;
import cn.bit.user.dao.CommunityUserRepository;
import cn.bit.user.dao.UserToRoomRepository;
import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by terry on 2018/1/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class MongoTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserToRoomRepository userToRoomRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CommunityUserRepository communityUserRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;

    @Test
    public void test() {
        /*DBObject dbObject = new BasicDBObject();
        dbObject.put("id", "5a5df3a6950e2e46aa2c8794");  //查询条件

        BasicDBObject fieldsObject=new BasicDBObject();
//指定返回的字段
        fieldsObject.put("id", true);
        Query query = new BasicQuery(dbObject,fieldsObject);
        User user = mongoTemplate.getFeedbackById(query, User.class);
        System.err.println(JSON.toJSONString(user));*/
        User user = new User();
        user.setId(new ObjectId("5a69ded5950e84c2078cc777"));
        user.setName("ali");
        user.setPhone("13987416741");
        Query query = new Query();

        buildExample(user).forEach(criteria -> query.addCriteria(criteria));
        System.err.println(query);
        boolean rs = mongoTemplate.exists(query, User.class);
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + rs);
        /*Query query = Query.query(Criteria.where("name").is("ali").and("phone").is("13987416741"));
        System.err.println(query);
        boolean rs = mongoTemplate.exists(query,User.class);
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + rs);*/
    }

    @Test
    public void test5() {
        Query query =Query.query(Criteria.where("name").regex("大爷"));
        query.fields().include("phone");
        for (User user : mongoTemplate.find(query, User.class)) {
            System.err.println(">>>>>>>>>>>>>>>>>>>>>>" + user.getName());
        }
    }

    @Test
    public void test6() {
        String search = "tom";
        Criteria c2 = Criteria.where("id").in("5a5df3a6950e2e46aa2c8794", "5a6046653e963e187f3fd6ea");
        Query query2 = Query.query(c2);
        if (StringUtils.isNotBlank(search)) {
            c2.orOperator(Criteria.where("name").regex(search),Criteria.where("email").regex(search));
        }
        System.err.println(query2);
        mongoTemplate.find(query2, User.class).forEach(user -> System.err.println(JSON.toJSONString(user)));
    }

//    @Test
//    public void test7() {
//        Role role = new Role();
//        role.setName("住户权限");
//        role.setClient(1000);
//        role.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
//        role.setDataStatus(DataStatusType.VALID.KEY);
//        role.setCreateAt(new Date());
//        role.setUpdateAt(role.getCreateAt());
//        role.setInterfaceResources(new ObjectId[]{
//                new ObjectId(),
//                new ObjectId()
//        });
//        role = roleRepository.insert(role);
//        System.err.println(role);
//
//        ClientUser systemUser = new ClientUser();
//        systemUser.setClient(role.getClient());
//        systemUser.setDataStatus(DataStatusType.VALID.KEY);
//        systemUser.setCreateAt(role.getCreateAt());
//        systemUser.setUpdateAt(role.getUpdateAt());
//        systemUser = clientUserRepository.insert(systemUser);
//        System.err.println(systemUser);
//    }

//    @Test
//    public void test8() {
//        List<UserToRoom> userToRooms = userToRoomRepository.findDistinctCommunityIdByUserIdIn(new ObjectId("5a8bd1c5d728e7a7da12e342"));
//        userToRooms.forEach(System.err::println);
//    }

    @Test
    public void test9() {
        CommunityUser communityUser = new CommunityUser();
        Set<Integer> clients = new HashSet<>();
        clients.add(ClientType.PROPERTY.value());
        Set<String> postCodes = Collections.singleton("TEST");
        communityUser.setClients(clients);
        communityUser.setUserId(new ObjectId());
        communityUser.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        communityUser.setRoles(postCodes);
        communityUser.setCreateAt(new Date());
        communityUser.setUpdateAt(communityUser.getCreateAt());
        communityUser.setDataStatus(DataStatusType.VALID.KEY);
        System.out.println("开始注册社区人员，communityUser:" + communityUser);
        communityUser = communityUserRepository.upsertWithAddToSetClientsAndRolesByCommunityIdAndUserIdAndDataStatus(communityUser,
                new ObjectId("5a82adf3b06c97e0cd6c0f3d"), communityUser.getUserId(), DataStatusType.VALID.KEY);
        System.out.println("开始注册社区人员，communityUser222:" + communityUser);
    }

    private List<Criteria> buildExample(User entity) {
        if (entity == null)
            return null;
        List<Criteria> criterias = new LinkedList<>();
        java.lang.reflect.Field[] fields = entity.getClass().getDeclaredFields();
        /*PropertyDescriptor[] propertyDescriptors= PropertyUtils.getPropertyDescriptors(entity.getClass());
        for (PropertyDescriptor pd : propertyDescriptors){
            pd.get
            PropertyUtils.get
        }*/
        for (java.lang.reflect.Field field : fields) {
            field.setAccessible(true);
            try {
                //PropertyUtils
                Object value = field.get(entity);
                if (value != null && !field.getType().isAssignableFrom(Set.class)) {
                    if (field.getAnnotation(Id.class) != null) {
                        criterias.add(Criteria.where("id").is(value));
                    } else {
                        criterias.add(Criteria.where(field.getName()).is(value));
                    }

                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return criterias;
    }

    private boolean isSimpleField(Object obj, Field field) {
        try {
            PropertyUtils.getSimpleProperty(obj, field.getName());
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }
}
