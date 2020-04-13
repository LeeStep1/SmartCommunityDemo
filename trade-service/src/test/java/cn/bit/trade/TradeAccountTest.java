package cn.bit.trade;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.PlatformType;
import cn.bit.facade.model.trade.TradeAccount;
import cn.bit.trade.dao.TradeAccountRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class TradeAccountTest {

    @Autowired
    private TradeAccountRepository tradeAccountRepository;

    @Test
    public void test() {
        List<TradeAccount> tradeAccounts = new ArrayList<>(2);

        TradeAccount wechat = new TradeAccount();
        wechat.setName("微信交易账号");
        wechat.setPlatform(PlatformType.WECHAT.value());
        wechat.setAppId("wxcfddfef2dbc38ada");
        wechat.setPartnerId("1489957462");
        wechat.setKey("lkR54288544POdstyUtY87HhbfxzVGVB");
        wechat.setCreateAt(new Date());
        wechat.setUpdateAt(wechat.getCreateAt());
        wechat.setDataStatus(DataStatusType.VALID.KEY);
        tradeAccounts.add(wechat);

        TradeAccount alipay = new TradeAccount();
        alipay.setName("支付宝交易账号");
        alipay.setPlatform(PlatformType.ALIPAY.value());
        alipay.setAppId("2017092208862984");
        alipay.setPartnerId("2088721571590650");
        alipay.setPubKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjGMDS8sfPo3NCHerrkkHyNRTYXzqT2mlZ02jHl/V14umoU0H56pPZTCOTkGHZMweHIYyxZrmQaNgKTnUzhz4FVasRsjVPkJNhuLz3omB484uvtmpSOBP+rR0BgxvtpoE+NAWIpjEHIcwg/KOg89rPduL9VJkZKiG9TKFoarGfYlPl8MUXDG5ftrM0+bv3baUGg9itV8cc55X/oHFoT/FGQlJlWQpBGz6synHL3K7bEQvEYPggG+BpyCsePKX/jko5hjHiuH9fl6GHxckOVKGESt4Cv49W4oUJGX7ucrXvajXqvVjEDCWQGHYH6ddVaUuZxBHTLDgzaHpUGjmjf4o/QIDAQAB");
        alipay.setPvtKey("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCMYwNLyx8+jc0Id6uuSQfI1FNhfOpPaaVnTaMeX9XXi6ahTQfnqk9lMI5OQYdkzB4chjLFmuZBo2ApOdTOHPgVVqxGyNU+Qk2G4vPeiYHjzi6+2alI4E/6tHQGDG+2mgT40BYimMQchzCD8o6Dz2s924v1UmRkqIb1MoWhqsZ9iU+XwxRcMbl+2szT5u/dtpQaD2K1Xxxznlf+gcWhP8UZCUmVZCkEbPqzKccvcrtsRC8Rg+CAb4GnIKx48pf+OSjmGMeK4f1+XoYfFyQ5UoYRK3gK/j1bihQkZfu5yte9qNeq9WMQMJZAYdgfp11VpS5nEEdMsODNoelQaOaN/ij9AgMBAAECggEAPji4QkSh8YC56lHYFuQpfhqVZjUOSOpDNDkV3iWNyv4LeZyBr20tyWSu/gJPNx69Ddlw8WJJQbheq4cFSeFPF24V5z2mPfT3FZzLh8ucdVJyJ4ajYDiDWlPWxMOIU/+Jypm35dedvCMzHphIECXDm2QOcUn2UyLaxhyBW/ksBoFIX94tWxANGtI3HCLVwpRiW+U/Qx5iHEs3Zvp3SebO/MS4WsHQiyxlyxW3ALg/vFWbMB4gPNIrjOrp2vMUFGtr1LpRL4VLR7arzDhxA+IXFjiLZ2Jwt1mnK3afK/gb0f6TbxGe3TR9uIl0rZZukKFc9gAg9cWmR/TTfuEkBVkuQQKBgQDBXm+4HrG7TXjzGzvfODag76DbUHbpY58L/4b6TLdDgLLwUNHYdEaJwln4j21KNrsSZlN2ADmAJAm/+WEtJGV6OvrzK55EZD2RYSWx8Gv1o/aKxD/PoelZibsl321DPPlEXgH4+L1v9KBhS90TlP/nwGZAacpl2ZzRVUtfRrVZ0QKBgQC523WHC1lEDFrPKzKUS8LwuNTt48BO9Z28fL8kuqbwheTFRUrxIfwBYu5D35kj7c9XMQQwD26uVNMN+PIHTx5j9O03Vo/EF5mI/dHiSAb2Tj1z36x1GEnMef1LvAlYKnh6yz63Qy2jxUojk5NTtgR0VgNJ0wEqn5io3YqF/wf7bQKBgHUFXpzRToPofZK534DV9xFsEy/GQUA6vqy3Jgth0+JxB1kxv9y7eVizGlm3Cs/H0WxwKoAV4LZwmMnp9GoqRZM0EFyLAAupkizh2rsVoXAVmwUdgPR5qss5890Wmnv/cWZzccQnXBVduJVJIPBR0pCAuiCvJQKAMEvqz2NIWkWRAoGAVTmRNw+5Kz8PFRiV5PKovYHEAiIBuTNf1WLOs6TzkC+Vq/AOYWxYBrq6z1zk+FjATxcm+HLbKg2ziiCxuzBIm0Vg0ZNb8Wtw+CSL7dthdeiCvXO/vSIaFS2LPQNItakj/grdA2RGtWZujMnLMQOyHzah42RikI0Gj8inELLVkjECgYB/HE2ZaX3cl1RbQ6EC0g5NsdtR/+ZdQlPl5hOu+bgm72LVHYZuzT2poxvBsqyZ0XL82p7OErlY6Ae0M5jd2307ibi2HnBozVJTuOw33t2p7unl70mdgmeTFUbMppoiAEKQdqNcyEr1yPzGHrunZOoVWQpKocLVNDM0Du1+8XEwcg==");
        alipay.setAlipayPubKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmXLnosEguDtExnUMvW7wr0j7QOSnb+K1llprwscZbLdmCMd2foBFyPRWimfE3VtP3WWP7/5ARVW0ZwheHBoOaS3ZEPfOdkrUvl5vaEIJf0+Xg1MYRyA+6kcfV1U/ClYGep9FDyvWIPNVK27zTqd+wOsIC//XiWZQUWOLy3aUIEMWQ57VQjfhAFtc84InysP2notQ7NvDO26dMTBZZ3Eb053dmCel0s8pvtsTbgudJ3eAvhcobJtCSOGKORmNfPn454Uo8fzLLLe2+iqAbX9zEZIZXMovBSHpJOwo5bza1Z4odn2xctf+ullY7CZeyvkqt+ma7nroGevsQnLHH7l3pwIDAQAB");
        alipay.setCreateAt(new Date());
        alipay.setUpdateAt(alipay.getCreateAt());
        alipay.setDataStatus(DataStatusType.VALID.KEY);
        tradeAccounts.add(alipay);

        tradeAccountRepository.insertAll(tradeAccounts);

        for (TradeAccount tradeAccount : tradeAccounts) {
            System.err.println(tradeAccount.getId());
        }
    }

}
