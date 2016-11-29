package cz.seznam.frpc.client;

import cz.seznam.frpc.core.FrpcType;

import java.lang.reflect.Type;
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

    public int getLength() {
        if(isNull()) {
            throw new NullPointerException("Wrapped array object is null");
        }
        return wrapped.length;
    }

    public FrpcCallResult get(int index) {
        return new FrpcCallResult(wrapped[index], httpResponseStatus);
    }

    public StructFrpcCallResult getStruct(int index) {
        return get(index).asStruct();
    }

    public <U> ArrayFrpcCallResult<U> getArrayOf(int index, Class<U> arrayType) {
        return doGetArrayOf(index, arrayType);
    }

    public <U> ArrayFrpcCallResult<U> getArrayOf(int index, FrpcType<U> arrayType) {
        return doGetArrayOf(index, arrayType.getGenericType());
    }

    private <U> ArrayFrpcCallResult<U> doGetArrayOf(int index, Type type) {
        return get(index).asArrayOf(type);
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
    private <U extends Collection<T>> U copyToCollection(U collection) {
        collection.addAll(Arrays.asList(wrapped));
        return collection;
    }

}
