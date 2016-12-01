package cz.seznam.frpc.core.transport;

/**
 * Represents a <i>fault</i> as specified both {@code FastRPC} and {@code XML-RPC} protocols.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 * @see <a href="http://xmlrpc.scripting.com/spec.html">http://xmlrpc.scripting.com/spec.html</a>
 *      <br />
 *      <a href="http://fastrpc.sourceforge.net/?page=manual&sub=spec">
 *          http://fastrpc.sourceforge.net/?page=manual&sub=spec
 *      </a>
 */
public class FrpcFault {

    /**
     * Status code returned by the server.
     */
    private final Integer statusCode;
    /**
     * Status message returned by the server.
     */
    private final String statusMessage;

    /**
     * Creates new instance from given parameters.
     *
     * @param statusCode status code returned by the server
     * @param statusMessage status message returned by the server
     */
    public FrpcFault(Integer statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    /**
     * Returns status code from the server.
     *
     * @return status code from the server
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Returns status message from the server.
     *
     * @return status message from the server
     */
    public String getStatusMessage() {
        return statusMessage;
    }

}
