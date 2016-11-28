package cz.seznam.frpc.client;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ArrayFrpcCallResult<T> extends AbstractFrpcCallResult<T[]> {

    ArrayFrpcCallResult(T[] wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    @Override
    public boolean isFault() {
        return false;
    }

    public T get(int index) {
        if(index < 0 || index > wrapped.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return wrapped[index];
    }

    public T[] asArray() {
        return wrapped;
    }

    public List<T> asList() {
        return copyToCollection(new ArrayList<>());
    }

    public Set<T> asSet() {
        return copyToCollection(new HashSet<>());
    }

    public <U extends Collection<T>> U asCollection(Supplier<U> collectionSupplier) {
        return copyToCollection(collectionSupplier.get());
    }

    @SuppressWarnings("unchecked")
    protected <U extends Collection<T>> U copyToCollection(U collection) {
        collection.addAll(Arrays.asList(wrapped));
        return collection;
    }

}
