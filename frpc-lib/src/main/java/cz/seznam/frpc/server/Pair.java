package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class Pair<X, Y> {

    private X first;
    private Y second;

    public Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public X getFirst() {
        return first;
    }

    public Y getSecond() {
        return second;
    }

}
