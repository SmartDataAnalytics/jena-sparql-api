package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A linked list node used in the combinatoric stream
 *
 * @author raven
 *
 * @param <T>
 */
public class LinkedListNode<T>
    implements Iterable<T>
{
    public static class LinkedListNodeIterator<T>
        implements Iterator<T>
    {
        protected LinkedListNode<T> current;
        
        public LinkedListNodeIterator(LinkedListNode<T> current) {
            super();
            this.current = current;
        }

        @Override
        public boolean hasNext() {
            boolean result = current.isTail();
            return result;
        }
    
        @Override
        public T next() {
            T result = current.data;
            current = current.successor;
            return result;
        }
    }    
        
    public T data;
    public LinkedListNode<T> predecessor;
    public LinkedListNode<T> successor;

    void append(LinkedListNode<T> node) {
        successor = node;
        node.predecessor = this;
    }

    void unlink() {
        predecessor.successor = successor;
        successor.predecessor = predecessor;
    }

    void relink() {
        successor.predecessor = this;
        predecessor.successor = this;
    }

//    boolean isEmpty() {
//        boolean result = predecessor == null && successor == null;
//        return result;
//    }
    boolean isHead() {
        boolean result = predecessor == null;
        return result;
    }

    boolean isTail() {
        boolean result = successor == null;
        return result;
    }

    boolean isFirst() {
        boolean result = predecessor.isHead();
        return result;
    }

    boolean isLast() {
        boolean result = successor.isTail();
        return result;
    }

    public List<T> toList() {
        List<T> result = new ArrayList<T>();
        LinkedListNode<T> curr = this;
        while(!curr.isTail()) {
            result.add(curr.data);
            curr = curr.successor;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = toList().toString();
        return result;
    }

    public static <S> LinkedListNode<S> create(Iterable<S> it) {
        LinkedListNode<S> head = new LinkedListNode<S>();
        head.data = null;

        LinkedListNode<S> curr = head;
        for(S item : it) {
            LinkedListNode<S> next = new LinkedListNode<S>();
            next.data = item;
            curr.append(next);
            curr = next;
        }

        LinkedListNode<S> tail = new LinkedListNode<S>();
        curr.append(tail);

        return head;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = isHead()
                ? new LinkedListNodeIterator<>(this.successor)
                : new LinkedListNodeIterator<>(this);

        return result;
    }
}