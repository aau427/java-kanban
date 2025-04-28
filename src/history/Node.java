package history;

public class Node<T> {
    private Node<T> next;
    private Node<T> prev;
    private T data;

    public Node(Node<T> next, Node<T> prev, T data) {
        this.next = next;
        this.prev = prev;
        this.data = data;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }

    public void setPrev(Node<T> prev) {
        this.prev = prev;
    }

    public Node<T> getNext() {
        return next;
    }

    public Node<T> getPrev() {
        return prev;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                '}';
    }
}

