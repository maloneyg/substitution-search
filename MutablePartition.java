
/*************************************************************************
 *  Compilation:  javac MutablePartition.java
 *  Execution:    java MutablePartition
 *
 *  A class representing a partition of objects of type E.
 *  It is assumed to contain no duplicate objects, although
 *  it has no way to guarantee this. 
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.io.Serializable;

class PartitionNode<E> implements Serializable {

    private final E data;

    // the node that comes after this one
    private PartitionNode<E> next;

    // the node that comes before this one
    private PartitionNode<E> previous;

    // is this a head node?
    private boolean head;

    // private constructor
    protected PartitionNode(E data) {
        this.data = data;
        this.next = null;
        this.previous= null;
        this.head = true;
    }

    // private constructor
    protected PartitionNode(E data, PartitionNode<E> next, PartitionNode<E> previous, boolean head) {

        this.data = data;
        this.next = next;
        this.previous = previous;
        this.head = head;
    }

    // set the previous node
    public void setPrevious(PartitionNode<E> nextOne) {
        previous = nextOne;
    }

    // get the previous node
    public PartitionNode<E> getPrevious() {
        return previous;
    }

    // set the next node
    public void setNext(PartitionNode<E> nextOne) {
        next = nextOne;
    }

    // get the next node
    public PartitionNode<E> getNext() {
        return next;
    }

    // return true if this is a head node
    public boolean isHead() {
        return head;
    }

    // get the data
    public E getData() {
        return data;
    }

    // find the next head node.
    // if this is a head node, return this.
    public PartitionNode<E> nextHead() {
        if (head) {
            return this;
        } else if (next == null) {
            return next;
        } else {
            return next.nextHead();
        }
    }

    // turn a head node into a non-head node, or vice-versa
    public void setHead(boolean toggle) {
        head = toggle;
    }

    // output a String
    public String toString() {
        return data.toString();
    }

} // end of class PartitionNode

public class MutablePartition<E> implements Serializable {

    private PartitionNode<E> head;

    // private constructor
    protected MutablePartition(E data) {
        head = new PartitionNode<>(data);
    }

    // add data and make it the head of a one-unit subset
    public void add(E data) {
        PartitionNode<E> newNode = new PartitionNode<>(data,head,null,true);
        head.setPrevious(newNode);
        head = newNode;
    }

    // add data at the end
    public void addToEnd(E data) {
        PartitionNode<E> current = head;
        while (current.getNext() != null) current = current.getNext();
        PartitionNode<E> newNode = new PartitionNode<>(data,null,current,true);
        current.setNext(newNode);
    }

    // get the head of the subset that contains data
    private PartitionNode<E> subset(E data) {
        PartitionNode<E> current = head;
        PartitionNode<E> output = head;
        while (current != null) {
            if (current.isHead()) output = current;
            if (current.getData().equals(data)) return output;
            current = current.getNext();
        }
        throw new IllegalArgumentException(data + " is not in this partition: " + this + " with size " + size() + ".");
        //throw new IllegalArgumentException(data + " is not in this partition (size = " + size() + " ).");
    }

    // return the head node
    public PartitionNode<E> getHead() {
        return head;
    }

    // split the class containing data into many pieces
    public void split(E data) {
        PartitionNode<E> current = subset(data).getNext();
        while (current != null && !current.isHead()) {
            current.setHead(true);
            current = current.getNext();
        }
    }

    // return true if these two things have been identified
    public boolean equivalent(E data1, E data2) {
        for (E candidate : equivalenceClass(data1)) {
            if (candidate.equals(data2)) return true;
        }
        return false;
    }

    // identify the classes containing one and two
    public void identify(E one, E two) {
        PartitionNode<E> c1 = subset(one);
        PartitionNode<E> c2 = subset(two);
        if (c1.equals(c2)) return;

        PartitionNode<E> last1 = c1;
        PartitionNode<E> afterLast1 = c1.getNext();
        while (afterLast1 != null && !afterLast1.isHead()) {
            last1 = afterLast1;
            afterLast1 = last1.getNext();
        }

        PartitionNode<E> last2 = c2;
        PartitionNode<E> afterLast2 = c2.getNext();
        while (afterLast2 != null && !afterLast2.isHead()) {
            last2 = afterLast2;
            afterLast2 = last2.getNext();
        }

        PartitionNode<E> before2 = c2.getPrevious();

        if (!c2.equals(afterLast1)) {
            last1.setNext(c2);
            c2.setPrevious(last1);

            last2.setNext(afterLast1);
            if (afterLast1 != null) afterLast1.setPrevious(last2);

            if (before2 != null) before2.setNext(afterLast2);
            if (afterLast2 != null) afterLast2.setPrevious(before2);
        }

        c2.setHead(false);
        if (head.equals(c2)) head = afterLast2;
    }

    public Iterable<E> equivalenceClass(E data) {
        final E thing = data;
        return new Iterable<E>() {
            // an iterator that iterates through things in this class
            public Iterator<E> iterator() {
                return new Iterator<E>() {

                    private PartitionNode<E> current = subset(thing);
                    private boolean fresh = true;

                    public boolean hasNext() {
                        return (fresh || !(current==null || current.isHead()));
                    }

                    public E next() {
                        fresh = false;
                        E output = current.getData();
                        current = current.getNext();
                        return output;
                    }

                    public void remove() { // do nothing
                    }

                };
            } // first iterator ends here
        };
    }

    // return true if this contains o
    public boolean contains(Object o) {
        PartitionNode currentNode = head;
        if (currentNode.getData().getClass() != o.getClass()) return false;
        while (currentNode != null) {
            if (currentNode.getData().equals(o)) return true;
            currentNode = currentNode.getNext();
        }
        return false;
    }

    // output a String
    public String toString() {
        String output = "";
        PartitionNode currentNode = head;
        while (currentNode != null) {
            if (currentNode.isHead()) output += "\n";
            output += currentNode.getData() + " ";
            currentNode = currentNode.getNext();
        }
        return output + "\n";
    }

    // equals method.
    // currently broken
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        MutablePartition l = (MutablePartition) obj;
        PartitionNode n1 = this.head;
        PartitionNode n2 = l.head;
        while (n1 != null && n2 != null) {
            if (!n1.getData().equals(n2.getData())) return false;
            n1 = n1.getNext();
            n2 = n2.getNext();
        }
        if (n1 != null || n2 != null) return false;
        return true;
    }

    // hashCode override.
    // currently broken
    public int hashCode() {
        int prime = 59;
        int result = 19;
        PartitionNode currentNode = head;
        while (currentNode != null)
            result = prime*result + currentNode.getData().hashCode();
        return result;
    }

    // the number of elements in the partition
    public int size() {
        int output = 0;
        PartitionNode<E> currentNode = head;
        while (currentNode != null) {
            output++;
            currentNode = currentNode.getNext();
        }
        return output;
    }


    // test client
    public static void main(String[] args) {

        String s1 = "good";
        String s2 = "bad";
        String s3 = "ugly";
        String s4 = "evil";
        String s5 = "great";
        MutablePartition<String> test = new MutablePartition<>(s1);
        test.add(s2);
        test.add(s3);
        test.add(s4);
        test.add(s5);

        System.out.println("The set:");
        System.out.println(test);
        System.out.println("Equating bad and evil:");
        test.identify("bad","evil");
        System.out.println(test);
        System.out.println("Equating good and great:");
        test.identify("good","great");
        System.out.println(test);
        System.out.println("Equating great and good:");
        test.identify("great","good");
        System.out.println(test);

        System.out.println("\nPrinting out all things equal to good.\n");
        for (String s : test.equivalenceClass("good")) System.out.print(s + " ");
        System.out.println("\n");

        System.out.println("Splitting good.");
        test.split("good");
        System.out.println(test);
        System.out.println("Adding troll, wicked, and cool.");
        test.add("troll");
        test.add("wicked");
        test.add("cool");
        System.out.println(test);

        System.out.println("Equating good and cool:");
        test.identify("good","cool");
        System.out.println(test);

        System.out.println("Equating wicked and cool:");
        test.identify("wicked","cool");
        System.out.println(test);

        System.out.println("Equating bad and great:");
        test.identify("bad","great");
        System.out.println(test);

        System.out.println("Splitting wicked:");
        test.split("wicked");
        System.out.println(test);

        Stack<Integer> something = new Stack<>();
        something.push(1);
        something.push(2);
        something.push(3);
        something.push(4);
        something.push(5);
        for (int i = 0; i < something.size(); i++) System.out.print(something.get(i)+" ");
        System.out.println();
        something.add(3,17);
        for (int i = 0; i < something.size(); i++) System.out.print(something.get(i)+" ");
        System.out.println();

    }

} // end of class MutablePartition
