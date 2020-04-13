package cn.bit.facade.vo.user.mili;

public class Service {

    /**
     * 分类ID
     */
    private Integer category_id;

    public Integer getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }

    public Service(){}

    public Service(Integer category_id) {
        this.category_id = category_id;
    }
}
