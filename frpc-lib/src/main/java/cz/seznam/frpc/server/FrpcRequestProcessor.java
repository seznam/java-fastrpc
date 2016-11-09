package cz.seznam.frpc.server;

import cz.seznam.frpc.core.transport.FrpcRequest;

/**
 * Top-level abstraction of any object capable of processing {@code FRPC} requests. This class takes a request
 * in form of an {@link FrpcRequest} and returns an instance of {@link FrpcRequestProcessingResult} which
 * represents the result of {@code FRPC} method invocation.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcRequestProcessor {

    /**
     * Processes single {@code FRPC} request completely. That mean it generally takes following steps:
     * <ol>
     *     <li>
     *         chooses proper method to call based on method name from the request
     *     </li>
     *     <li>
     *         calls proper business logic implementing the {@code FRPC} method
     *     </li>
     *     <li>
     *         (optionally) works out under which key to store the result (or not if the result is already a map)
     *     </li>
     *     <li>
     *         returns the result as {@link FrpcRequestProcessingResult}
     *     </li>
     * </ol>
     * @param frpcRequest request to be processed
     * @return result of {@code FRPC} method call represented by the request body
     * @throws Exception if anything goes wrong
     */
    public FrpcRequestProcessingResult process(FrpcRequest frpcRequest) throws Exception;

}
