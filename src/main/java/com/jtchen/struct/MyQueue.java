package com.jtchen.struct;

import java.util.Iterator;

/*******************************
 * @author JT Chen
 * @version 2.1
 * @date 2020/11/05
 ******************************/
public class MyQueue<Item> implements Iterable<Item> {

    private Node head, tail;
    private int length;

    /**
     * 构建MyQueue
     */
    public MyQueue() {
        this.head = null;
        this.tail = null;
        this.length = 0;
    }

    /**
     * 入队
     *
     * @param i 入队元素
     */
    public void enqueue(Item i) {
        if (isEmpty()) {
            head = new Node(i);
            tail = head;
        } else {
            tail.next = new Node(i);
            tail = tail.next;
        }
        length++;
    }

    /**
     * 出队元素
     *
     * @return 元素值
     */
    @SuppressWarnings("UnusedReturnValue")
    public Item dequeue() {
        if (isEmpty()) throw new IllegalArgumentException("The queue is empty!");
        Item tmp = head.item;
        head = head.next;
        length--;
        return tmp;
    }

    /**
     * 观察队头元素
     *
     * @return 队头元素值
     */
    public Item peek() {
        return head.item;
    }

    /**
     * 判断是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * 返回队列大小
     *
     * @return 队列大小
     */
    public int size() {
        return length;
    }

    @Override
    public String toString() {
        Node tmp = head;
        StringBuilder builder = new StringBuilder("[");
        while (tmp != null) {
            builder.append(tmp.item).append(", ");
            tmp = tmp.next;
        }
        builder.append("\b\b]");
        if (isEmpty()) return "[]";
        else return builder.toString();
    }

    @Override
    public Iterator<Item> iterator() {
        return new MyQueueIterator();
    }

    private class MyQueueIterator implements Iterator<Item> {
        private Node current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Item next() {
            Item item = current.item;
            current = current.next;
            return item;
        }
    }

    /**
     * 私有链表数据结构
     */
    private class Node {
        public Node next;
        public Item item;

        public Node(Item item) {
            this.item = item;
        }
    }


}
