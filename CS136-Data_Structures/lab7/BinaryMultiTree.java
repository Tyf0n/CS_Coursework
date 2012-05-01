/*
 * Nathaniel Lim
 * CS 136 Williams College
 * Mon, April 14, 2008
 */

import java.util.ArrayList;

public class BinaryMultiTree<T> implements Iterable<T> {

    private Comparator<T> comparator;
    private BinaryMultiTree<T> left;
    private BinaryMultiTree<T> right;
    private ArrayList<T> values = new ArrayList<T>();

    public BinaryMultiTree(Comparator<T> comparator){
        this.comparator = comparator;
    }


    public int size(){
        int out = values.size();
        if (left!=null){
            out += left.size();
        }

        if (right!=null){
            out += right.size();
        }
        return out;
    }

    public void add(T x){
        //This should only add if the node was recently instantiated
        if (values.size()==0){
            values.add(x);
        } else {
            int c = comparator.compare(x, values.get(0));
            if (c == 0){
                values.add(x);
            } else if (c < 0){
                if (left == null){
                    //create a new node
                    left = new BinaryMultiTree<T>(comparator);
                }
                left.add(x);
            } else {
                if (right == null){
                    //create a new node
                    right = new BinaryMultiTree<T> (comparator);
                }
                right.add(x);
            }
        }
    }

    public boolean contains(T x){
        int c = comparator.compare(x, values.get(0));
        if (c == 0) {
            return values.contains(x);
        } else if (c < 0) {
            if (left!= null){
                return left.contains(x);
            } else {
                return false;
            }
        } else {
            if (right!= null){
                return right.contains(x);
            } else {
                return false;
            }
        }
    }

    public void clear(){
        values = new ArrayList<T>();
        left = new BinaryMultiTree<T>(comparator);
        right = new BinaryMultiTree<T>(comparator);
    }

    public T remove(T x){
        int c = comparator.compare(x, values.get(0));
        if (c == 0) {
            return values.contains(x);
        } else if (c < 0) {
            if (left!= null){
                return left.contains(x);
            } else {
                return false;
            }
        } else {
            if (right!= null){
                return right.contains(x);
            } else {
                return false;
            }
        }
    }
    public T get(T x){
        return null;
    }

    public Iterator<T> iterator() {

    }


    public class Name {
       private String first, last;
        public Name (String first, String last){
            this.first = first;
            this.last = last;
        }
        public String getFirst(){
            return first;
        }
        public String getLast(){
            return last;
        }
        public boolean equals (Object other){
            if (! other instanceof Name){
                return false; 
            } else {
                return last.equals(other.getLast()) && 
                    first.equals(other.getFirst());
            }
        }
        public String toString(){
            return first + " " + last;
        }
    }

    public LastNameComparator implements Comparator<Name> {
        public int compare (Name a, Name b){
            return a.getLast().compare(b.getLast());
        }
        public boolean equals (Object obj) {
            return false;
        }
    }



}
