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

    @Override
    public boolean isFault() {
        return false;
    }

    public FrpcCallResult get(int index) {
        return new FrpcCallResult(doGet(index), httpResponseStatus);
    }

    public StructFrpcCallResult getStruct(int index) {
        return get(index).asStruct();
    }

    public <T> ArrayFrpcCallResult<T> getArray(int index, Class<T> arrayType) {
        return get(index).asArrayOf(arrayType);
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

    public <U extends Collection<T>> U asCollection(Supplier<U> collectionSupplier) {
        return copyToCollection(collectionSupplier.get());
    }

    protected Object doGet(int index) {
        if(index < 0 || index > wrapped.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return wrapped[index];
    }

    @SuppressWarnings("unchecked")
    protected <U extends Collection<T>> U copyToCollection(U collection) {
        for(Object o : wrapped) {
            collection.add(elementsClass.cast(o));
        }
        return collection;
    }

}
