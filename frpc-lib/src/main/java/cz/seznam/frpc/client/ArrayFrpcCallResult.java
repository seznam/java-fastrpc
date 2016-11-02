package cz.seznam.frpc.client;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ArrayFrpcCallResult<T> extends AbstractFrpcCallResult<Object[]> {

    private Class<T> elementsClass;

    ArrayFrpcCallResult(Object[] wrapped, int httpResponseStatus, Class<T> elementsClass) {
        super(wrapped, httpResponseStatus);
        this.elementsClass = elementsClass;
    }

    @SuppressWarnings({"unchecked"})
    public T[] asArray() {
        T[] copy = (T[]) Array.newInstance(elementsClass, wrapped.length);
        for(int i = 0; i < wrapped.length; i++) {
            copy[i] = elementsClass.cast(wrapped[i]);
        }
        return copy;
    }

    public List<T> asList() {
        return copyToCollection(new ArrayList<>());
    }

    public Set<T> asSet() {
        return copyToCollection(new HashSet<>());
    }

    public SortedSet<T> asSortedSet() {
        return copyToCollection(new TreeSet<>());
    }

    public <U extends Collection<T>> U asCollection(Supplier<U> collectionSupplier) {
        return copyToCollection(collectionSupplier.get());
    }

    @SuppressWarnings("unchecked")
    protected <U extends Collection<T>> U copyToCollection(U collection) {
        for(Object o : wrapped) {
            collection.add(elementsClass.cast(o));
        }
        return collection;
    }

}
