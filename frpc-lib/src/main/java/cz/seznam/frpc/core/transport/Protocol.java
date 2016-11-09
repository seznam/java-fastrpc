package cz.seznam.frpc.core.transport;

/**
 * Enum specifying protocol used by the client. Order of elements in this enum specifies their preference (from
 * highest to lowest) in case multiple protocols are supported by the server. The first element has the highest
 * preference.
 */
public enum Protocol {
    /**
     * FastRPC as defined by <a href="http://opensource.seznam.cz/frpc/">FastRPC Specification</a>. <br />
     * Its content type is <i>application/x-frpc</i>.
     */
    FRPC("application/x-frpc"),
    /**
     * XML-RPC as defined by <a href="http://xmlrpc.scripting.com/spec.html">XML-RPC Specification</a>.
     * Its content type is <i>text/xml</i>.
     */
    XML_RPC("text/xml");

    /**
     * The content type used by this instance to be used in content negotiation.
     */
    private String contentType;

    private Protocol(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the content type used by this {@code Protocol} to be used in content negotiation.
     *
     * @return content type used by this {@code Protocol}
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns instance of {@code Protocol} based on given {@code contentType}.
     *
     * @param contentType content type to return protocol for
     * @return instance of {@code Protocol} based on given {@code contentType}
     *
     * @throws IllegalArgumentException if there is no protocol for given content type
     */
    public static Protocol fromContentType(String contentType) {
        switch (contentType) {
            case "application/x-frpc":
                return FRPC;
            case "text/xml":
                return XML_RPC;
            default:
                throw new IllegalArgumentException("No known protocol for content type \"" + contentType + "\"");
        }
    }

}
