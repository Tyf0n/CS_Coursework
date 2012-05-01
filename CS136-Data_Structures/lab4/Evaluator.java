public interface Evaluator<Value> {
    /** Returns a float representing the sort rank of this value.*/
    public float evaluate(Value v);    
}
