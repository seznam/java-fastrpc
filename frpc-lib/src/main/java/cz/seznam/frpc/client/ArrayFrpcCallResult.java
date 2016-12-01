package cz.seznam.frpc.client;

import cz.seznam.frpc.core.FrpcType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;


/**
 * Class representing a {@code FRPC} call result converted into an array with specific component type. Provides
 * convenience methods for accessing individual elements of the array and potentially converting these into other types.
 *
 * @param <T> component type of array wrapped by this {@code FRPC} call result
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ArrayFrpcCallResult<T> extends AbstractFrpcCallResult<T[]> {

    /**
     * Constructs new instance from given parameters.
     *
     * @param wrapped            the array to wrap
     * @param httpResponseStatus HTTP status code of the response returned by the {@code FRPC} method
     */
    ArrayFrpcCallResult(T[] wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    /**
     * Returns the length of the underlying array.
     *
     * @return the length of the underlying array
     * @throws NullPointerException if the array is {@code null}
     */
    public int getLength() {
        if (isNull()) {
            throw new NullPointerException("Wrapped array object is null");
        }
        return wrapped.length;
    }

    /**
     * Returns the {@code index}-th element in the underlying array wrapped into {@link FrpcCallResult} so that
     * additional utility methods can be called upon it.
     *
     * @param index index pointing to an element in the underlying array which is to be wrapped in
     *              {@code FrpcCallResult} and returned
     * @return {@code index}-th element in the underlying array wrapped into {@link FrpcCallResult}
     * @see #getRaw(int)
     */
    public FrpcCallResult<T> get(int index) {
        return new FrpcCallResult<>(wrapped[index], httpResponseStatus);
    }

    /**
     * Returns the {@code index}-th element in the underlying array as-is
     *
     * @param index index pointing to an element in the underlying array which is to be returned
     * @return {@code index}-th element in the underlying array as-is
     */
    public T getRaw(int index) {
        return wrapped[index];
    }

    /**
     * Convenience method for calling {@link #get(int)} with given parameter and subsequently calling
     * {@link FrpcCallResult#asStruct()} on the result.
     *
     * @param index index pointing to an element in the underlying array which is to be converted to a structure,
     *              wrapped in {@code StructFrpcCallResult} and returned
     * @return result of operations described above
     */
    public StructFrpcCallResult getStruct(int index) {
        return get(index).asStruct();
    }

    /**
     * Convenience method for calling {@link #get(int)} with given parameter and subsequently calling
     * {@link FrpcCallResult#asArrayOf(Class)} on the result with second parameter.
     *
     * @param index     index pointing to an element in the underlying array which is to be converted to an array of given
     *                  type, wrapped in {@code ArrayFrpcCallResult} and returned
     * @param arrayType component type of the array to be returned
     * @return result of operations described above
     */
    public <U> ArrayFrpcCallResult<U> getArrayOf(int index, Class<U> arrayType) {
        return doGetArrayOf(index, arrayType);
    }

    /**
     * Convenience method for calling {@link #get(int)} with given parameter and subsequently calling
     * {@link FrpcCallResult#asArrayOf(FrpcType)} on the result with second parameter.
     *
     * @param index     index pointing to an element in the underlying array which is to be converted to an array of given
     *                  type, wrapped in {@code ArrayFrpcCallResult} and returned
     * @param arrayType {@link FrpcType} describing the component type of the array to be returned
     * @return result of operations described above
     */
    public <U> ArrayFrpcCallResult<U> getArrayOf(int index, FrpcType<U> arrayType) {
        return doGetArrayOf(index, arrayType.getGenericType());
    }

    private <U> ArrayFrpcCallResult<U> doGetArrayOf(int index, Type type) {
        return get(index).asArrayOf(type);
    }

    /**
     * Returns the underlying array as-is, no copy is made.
     *
     * @return the underlying array, as-is
     */
    public T[] asArray() {
        return wrapped;
    }

    /**
     * Instantiates new {@link ArrayList}, puts all elements of the underlying array into it and returns it.
     *
     * @return new instance of {@link ArrayList} into which all elements of the underlying array were put
     */
    public List<T> asList() {
        return copyToCollection(new ArrayList<>());
    }

    /**
     * Instantiates new {@link HashSet}, puts all elements of the underlying array into it and returns it.
     *
     * @return new instance of {@link HashSet} into which all elements of the underlying array were put
     */
    public Set<T> asSet() {
        return copyToCollection(new HashSet<>());
    }

    /**
     * Obtains a collection instance from given supplier, puts all elements of the underlying array into it and returns
     * it. Using this method, the underlying array can be converted into any kind of {@link Collection}.
     *
     * @return collection obtained from given supplier into which all elements of the underlying array were put
     */
    public <U extends Collection<T>> U asCollection(Supplier<U> collectionSupplier) {
        return copyToCollection(collectionSupplier.get());
    }

    @SuppressWarnings("unchecked")
    private <U extends Collection<T>> U copyToCollection(U collection) {
        collection.addAll(Arrays.asList(wrapped));
        return collection;
    }

}
