package cn.bit.framework.utils.dataStructure.tree;


import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.FastHashMap;

import java.io.Serializable;
import java.util.*;

/**
 * 多叉树
 *
 * @author terry
 * @create 2016-07-25 18:21
 **/
public class MultipleBranchTree<K extends Comparable<K>> implements Serializable{

    private TreeNode root;

    public MultipleBranchTree() {
    }

    public MultipleBranchTree(TreeNode<K> root) {
        this.root = root;
    }

    public TreeNode<K> findNode(K id) {
        return root == null ? null : root.findNode(id);
    }

    public TreeNode<K> root() {
        return root;
    }

    public void root(TreeNode<K> root) {
        this.root = root;
    }

    public boolean addNode(TreeNode<K> node) {

        //首先查找是否存在此ID的节点，如果有则不添加
        if (this.findNode(node.getId()) != null)
            return false;
        //查找父节点是否存在
        K parentId = node.getParentId();
        TreeNode parent = this.findNode(parentId);
        if (parent == null)
            return false;
        parent.addChild(node);
        return true;
    }


    public boolean removeNode(K id) {
        TreeNode<K> node = this.findNode(id);
        return removeNode(node);
    }

    public boolean removeNode(TreeNode<K> node) {
        if (node == null)
            return false;
        if (node.equals(root)) {
            root = null;
            return true;
        }
        if (node.getParentId() == null)
            return false;
        TreeNode parent = this.findNode(node.getParentId());
        if (parent == null)
            return false;
        parent.removeChild(node.getId());
        return true;
    }

    public abstract static class TreeNode<K> implements Serializable {
        private K id;
        private K parentId;
        private String name;
        private TreeNode<K> parent;
        private Map<K, TreeNode> childrenMap;

        public TreeNode() {
        }

        public TreeNode(K id, String name, K parentId) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;
        }

        public TreeNode(K id, String name, TreeNode<K> parent) {
            this.id = id;
            this.name = name;
            this.parent = parent;
            this.parentId = parent == null ? null : parent.getId();
        }

        public K getId() {
            return id;
        }

        public void setId(K id) {
            this.id = id;
        }

        public K getParentId() {
            return parentId;
        }

        public void setParentId(K parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Collection<TreeNode> getChildren() {
            return childrenMap == null ? null : childrenMap.values();
        }

        public void clearChildren() {
            childrenMap = null;
        }

        public Collection<TreeNode<K>> traverseDown() {
            List<TreeNode<K>> nodes = new LinkedList<>();
            Queue<TreeNode<K>> queue = new LinkedList<>();
            TreeNode<K> node = this;
            queue.offer(node);
            while (!queue.isEmpty()) {
                node = queue.poll();
                nodes.add(node);
                if (node.getChildren() != null && !node.getChildren().isEmpty())
                    node.getChildren().forEach(queue::offer);
            }
            //nodes.sort(null);
            return nodes;
        }


        public Collection<K> traverseDownIds() {
            List<K> ids = new LinkedList<>();
            Queue<TreeNode<K>> queue = new LinkedList<>();
            TreeNode<K> node = this;
            queue.offer(node);
            while (!queue.isEmpty()) {
                node = queue.poll();
                ids.add(node.getId());
                if (node.getChildren() != null && !node.getChildren().isEmpty())
                    node.getChildren().forEach(queue::offer);
            }
            //ids.sort(null);
            return ids;
        }

        public Collection<TreeNode<K>> traverseUp() {
            List<TreeNode<K>> nodes = new LinkedList<>();
            Stack<TreeNode<K>> stack = new Stack<>();
            TreeNode<K> node = this;
            stack.push(node);
            while (!stack.empty()) {
                node = stack.pop();
                nodes.add(node);
                if (node.parent != null)
                    stack.push(node.parent);
            }
            //nodes.sort(null);
            return nodes;
        }

        public Collection<K> traverseUpIds() {
            List<K> nodes = new LinkedList<>();
            Stack<TreeNode<K>> stack = new Stack<>();
            TreeNode<K> node = this;
            stack.push(node);
            while (!stack.empty()) {
                node = stack.pop();
                nodes.add(node.getId());
                if (node.parent != null)
                    stack.push(node.parent);
            }
            //nodes.sort(null);
            return nodes;
        }

        /**
         * 添加到此节点
         *
         * @param child
         */
        public void addChild(TreeNode child) {
            if (child == null)
                return;
            child.parent = this;
            if (this.childrenMap == null)
                childrenMap = new FastHashMap();
            childrenMap.put((K) child.getId(), child);
        }

        public void removeChild(K id) {
            childrenMap.remove(id);
        }


        /**
         * 从此节点往下递归查找
         *
         * @param id
         * @return
         */
        public TreeNode findNode(K id) {
            TreeNode node = null;
            if (id.equals(this.id))
                return this;
            if (childrenMap == null || childrenMap.isEmpty()) {
                return null;
            }
            if ((node = childrenMap.get(id)) != null)
                return node;
            for (TreeNode child : childrenMap.values()) {
                node = child.findNode(id);
                if (node != null)
                    return node;
            }
            return node;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            TreeNode other = (TreeNode) obj;
            return other.getId() == null && other.getId().equals(this.id);
        }


    }

    public void print() {
        System.err.println(root);
    }

    private static class MyNode extends TreeNode<Long> {

        public MyNode(Long id, String name, Long parentId) {
            super(id, name, parentId);
        }

        public MyNode(Long id, String name, TreeNode<Long> parent) {
            super(id, name, parent);
        }
    }

    public static void main(String[] args) {
        TreeNode<Long> root = new MyNode(0l, "root", (TreeNode<Long>) null);
        MultipleBranchTree<Long> tree = new MultipleBranchTree(root);

        TreeNode<Long> node1 = new MyNode(1l, "1", root);
        TreeNode<Long> node2 = new MyNode(2l, "2", root);
        TreeNode<Long> node3 = new MyNode(3l, "3", node1);
        TreeNode<Long> node4 = new MyNode(4l, "4", node2);

        tree.addNode(node1);
        tree.addNode(node2);
        tree.addNode(node3);
        tree.addNode(node4);

        /*tree.print();
        Collection<TreeNode<Long>> c = tree.root().traverseDown();
        c.forEach(n -> System.err.println(n));*/
        //System.err.println(tree.findNode(2l));
        Collection<Long> c = node4.traverseUpIds();
        c.forEach(n -> System.err.println(n));
    }
}
