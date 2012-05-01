public class TriangleComparator implements java.util.Comparator<Triangle> {
    public int compare(Triangle a, Triangle b) {
        return Float.compare(a.zSum(), b.zSum());
    }

    public boolean equals() {
        return false;
    }
}
