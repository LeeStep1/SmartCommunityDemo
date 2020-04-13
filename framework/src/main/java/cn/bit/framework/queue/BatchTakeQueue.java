package cn.bit.framework.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/6/13.
 */
public abstract class BatchTakeQueue<E> implements BlockingQueue<E>, Runnable {

    /**
     *
     */
    private static final long serialVersionUID = -4177810446459549938L;

    private BlockingQueue<E> queue;

    private int capacity;

    private int take_size;

    /**
     * @param capacity
     * @param take_size
     */
    public BatchTakeQueue(int capacity, int take_size) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        if (take_size <= 0)
            throw new IllegalArgumentException("Non-positive size.");
        if (take_size > capacity)
            throw new IllegalArgumentException("Take Size can not gt capacity.");

        this.queue = new LinkedBlockingQueue<>(capacity); //new ArrayBlockingQueue<>(capacity);
        this.take_size = take_size;
    }

    /**
     * @return
     * @see Collection#size()
     */
    public int size() {
        return queue.size();
    }

    /**
     * @return
     * @see Collection#isEmpty()
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * @param e
     * @return
     * @see BlockingQueue#add(Object)
     */
    public boolean add(E e) {
        return queue.add(e);
    }

    /**
     * @return
     * @see Collection#iterator()
     */
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    /**
     * @return
     * @see java.util.Queue#remove()
     */
    public E remove() {
        return queue.remove();
    }

    /**
     * @param e
     * @return
     * @see BlockingQueue#offer(Object)
     */
    public boolean offer(E e) {
        return queue.offer(e);
    }

    /**
     * @return
     * @see java.util.Queue#poll()
     */
    public E poll() {
        return queue.poll();
    }

    /**
     * @return
     * @see Collection#toArray()
     */
    public Object[] toArray() {
        return queue.toArray();
    }

    /**
     * @return
     * @see java.util.Queue#element()
     */
    public E element() {
        return queue.element();
    }

    /**
     * @return
     * @see java.util.Queue#peek()
     */
    public E peek() {
        return queue.peek();
    }

    /**
     * @param a
     * @return
     * @see Collection#toArray(Object[])
     */
    public <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    /**
     * @param e
     * @throws InterruptedException
     * @see BlockingQueue#put(Object)
     */
    public void put(E e) throws InterruptedException {
        queue.put(e);
    }

    /**
     * @param e
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @see BlockingQueue#offer(Object, long,
     * TimeUnit)
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        return queue.offer(e, timeout, unit);
    }

    /**
     * @return
     * @throws InterruptedException
     * @see BlockingQueue#take()
     */
    public E take() throws InterruptedException {
        return queue.take();
    }

    /**
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @see BlockingQueue#poll(long,
     * TimeUnit)
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    /**
     * @return
     * @see BlockingQueue#remainingCapacity()
     */
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    /**
     * @param o
     * @return
     * @see BlockingQueue#remove(Object)
     */
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    /**
     * @param o
     * @return
     * @see BlockingQueue#contains(Object)
     */
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    /**
     * @param c
     * @return
     * @see BlockingQueue#drainTo(Collection)
     */
    public int drainTo(Collection<? super E> c) {
        return queue.drainTo(c);
    }

    /**
     * @param c
     * @return
     * @see Collection#containsAll(Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    /**
     * @param c
     * @param maxElements
     * @return
     * @see BlockingQueue#drainTo(Collection,
     * int)
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        return queue.drainTo(c, maxElements);
    }

    /**
     * @param c
     * @return
     * @see Collection#addAll(Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
        return queue.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see Collection#removeAll(Collection)
     */
    public boolean removeAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    /**
     * @param c
     * @return
     * @see Collection#retainAll(Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return queue.retainAll(c);
    }

    /**
     * @see Collection#clear()
     */
    public void clear() {
        queue.clear();
    }


    @Override
    public void run() {
        List<E> lst = new LinkedList<>();
        int size = queue.size() >= take_size ? take_size
                : queue.size();
        for (int i = 0; i < size; i++) {
            lst.add(queue.poll());
        }
        if (lst.size() > 0) {
            try {
                take(lst);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void take(List<E> lst);
}
