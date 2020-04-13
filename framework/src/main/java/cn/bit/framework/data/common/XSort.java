package cn.bit.framework.data.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2018/1/22 0022.
 */
public class XSort implements Iterable<XSort.Order>,Serializable{

    public static final Direction DEFAULT_DIRECTION = Direction.ASC;
    private final List<Order> orders;

    public XSort(Order... orders) {
        this(Arrays.asList(orders));
    }

    public XSort(List<Order> orders) {

        if (null == orders || orders.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one sort property to sort by!");
        }

        this.orders = orders;
    }

    public XSort(String... properties) {
        this(DEFAULT_DIRECTION, properties);
    }

    public XSort(Direction direction, String... properties) {
        this(direction, properties == null ? new ArrayList<String>() : Arrays.asList(properties));
    }

    public XSort(Direction direction, List<String> properties) {

        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }

        this.orders = new ArrayList<Order>(properties.size());

        for (String property : properties) {
            this.orders.add(new Order(property, direction));
        }
    }

    @Override
    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }

    public static XSort asc(String... properties) {
        return new XSort(properties);
    }

    public static XSort desc(String... properties) {
        return new XSort(Direction.DESC, properties);
    }

    @Data
    public static class Order {
        private String property;
        private Direction direction;

        public Order(String field) {
            this(field, DEFAULT_DIRECTION);
        }

        public Order(String property, Direction direction) {
            this.property = property;
            this.direction = direction;
        }
    }

    public static enum Direction {
        ASC, DESC;
    }
}
