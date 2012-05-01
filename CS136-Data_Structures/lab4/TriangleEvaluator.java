public class TriangleEvaluator implements Evaluator<Triangle> {
    /** Returns a float representing the sort rank of this value.*/
    public float evaluate(Triangle t) {
        return t.zSum();
    }
}
