package history;


import java.util.ArrayList;
import java.util.List;

public class CustomLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;

    public Node<T> linkLast(T newElement) {
        Node<T> oldTail = tail;
        final Node<T> newTail = new Node<>(null, oldTail, newElement);
        tail = newTail;
        if (oldTail == null) {
            head = newTail;
        } else {
            oldTail.setNext(newTail);
        }
        return newTail;
    }

    public void removeNode(Node<T> node) {
        Node<T> oldPrev = node.getPrev();
        Node<T> oldNext = node.getNext();
        if (node == head) {
            head = oldNext;
        }
        if (node == tail) {
            tail = oldPrev;
        }
        if (oldPrev != null) {
            oldPrev.setNext(oldNext);
        }
        if (oldNext != null) {
            oldNext.setPrev(oldPrev);
        }
    }

    public List<T> getDataList() {
        Node<T> currentNode = head;
        List<T> returnList = new ArrayList<>();
        while (currentNode != null) {
            returnList.add(currentNode.getData());
            currentNode = currentNode.getNext();
        }
        return returnList;
    }
}

