package cz.seznam.frpc.server;

import java.io.InputStream;

/**
 * Top-level abstraction of any object capable of processing {@code FRPC} request body. This class takes a request
 * body in form of an {@code InputStream} and returns an instance of {@link FrpcRequestProcessingResult} which
 * represents the result of {@code FRPC} method invocation.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcRequestProcessor {

    /**
     * Processes single {@code FRPC} request completely. That mean it takes in request body in form of an
     * {@code InputStream} and:
     * <ol>
     *     <li>
     *         deserializes method name
     *     </li>
     *     <li>
     *         chooses proper method to call based on method name read
     *     </li>
     *     <li>
     *         deserializes arguments of the method
     *     </li>
     *     <li>
     *         calls proper business logic implementing the {@code FRPC} method
     *     </li>
     *     <li>
     *         works out under which key to store the result (or not if the result is already a map)
     *     </li>
     *     <li>
     *         returns the result as {@link FrpcRequestProcessingResult}
     *     </li>
     * </ol>
     * @param requestBody request body to be processed
     * @return result of {@code FRPC} method call represented by the request body
     * @throws Exception if anything goes wrong
     */
    public FrpcRequestProcessingResult process(InputStream requestBody) throws Exception;

}
