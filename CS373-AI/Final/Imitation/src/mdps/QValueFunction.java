package mdps;

public interface QValueFunction {
	// should return V(s) = max_a Q(s, a) for any given state s
    double getValue(int state);
    // Q(s, a)
    double getValue(int state, int action);

}
