package cz.seznam.frpc.server;

import java.io.InputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcRequestProcessor {

    public FrpcRequestProcessingResult process(InputStream is) throws Exception;

}
