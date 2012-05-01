public class IntegerEvaluator implements Evaluator<Integer> {
    /** Returns a float representing the sort rank of this value.*/
    public float evaluate(Integer i) {
        return (float)i.intValue();
    }
}
