package cn.bit.system;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.ResourceType;
import cn.bit.facade.model.system.*;
import cn.bit.system.dao.*;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class MongoTest {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    public void test() {
        List<App> apps = appRepository.findAll();
        for (App app : apps) {
            System.err.println(app);
        }
    }

    @Test
    public void test1() {
        App app = appRepository.findById(new ObjectId("5a77d452a05e32e3455f7ea1"));
        System.err.println(app);
    }

    @Test
    public void test2() {
        App app = new App();
        app.setId(new ObjectId());
        System.err.println(appRepository.updateOne(app));
    }

    @Test
    public void test3() {
        Version version = versionRepository.findTop1ByAppIdAndPublishedAndHasErrorAndSequenceGreaterThanOrderByCreateAtDesc(
                new ObjectId("5a77d452a05e32e3455f7ea1"),
                true,
                false,
                "v1.0.0"
        );
        System.out.println(version);
    }

    @Test
    public void test4() {
        Client client = new Client();
        client.setName("住户端");
        client.setType(1000);
        client.setPushAccountId(new ObjectId("5a965ca60f0c7e599d090200"));
        client.setCreateAt(new Date());
        client.setUpdateAt(client.getCreateAt());
        client.setDataStatus(DataStatusType.VALID.KEY);
        System.err.println(clientRepository.insert(client));
    }

    @Test
    public void test5() {
        Role role = new Role();
        role.setKey("Admin");
        role.setName("系统管理员");
        role.setDescr("系统管理员，最高权限");
        role = roleRepository.insert(role);
        System.err.println(role);
    }

    @Test
    public void test6() {
        List<Resource> resources = resourceRepository.findByGroupIdExistsAndTypeAndDataStatus(false, ResourceType.MENU.value(),
                DataStatusType.VALID.KEY);
        System.err.println(resources.size());
    }

}
