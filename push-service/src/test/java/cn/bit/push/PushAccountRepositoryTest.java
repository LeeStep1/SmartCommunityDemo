package cn.bit.push;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.push.PushAccount;
import cn.bit.push.dao.PushAccountRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class PushAccountRepositoryTest {

    @Autowired
    private PushAccountRepository pushAccountRepository;

    @Test
    public void test() {
        PushAccount pushAccount = new PushAccount();
        pushAccount.setAppKey("8130aaa776f8d1d5aea94486");
        pushAccount.setAppSecret("2b9df5618ad692e5991b9829");
        pushAccount.setProvider("jpush");
        pushAccount.setCreateAt(new Date());
        pushAccount.setUpdateAt(pushAccount.getCreateAt());
        pushAccount.setDataStatus(DataStatusType.VALID.KEY);

        System.err.println(pushAccountRepository.insert(pushAccount));
    }

}
