package cn.bit.facade.enums;

/**
 * Created by fxiao
 * on 2018/3/3
 * 菜单的一些类型
 */
public enum MenuSomeType {

    MENU_TYPE_H5(1, "H5的菜单"), MENU_TYPE_APP(2, "APP的菜单");

    public Integer key;

    public String value;

    MenuSomeType(int key, String value) {
        this.key = key;
        this.value = value;
    }

}
