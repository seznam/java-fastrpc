package cz.seznam.frpc;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public @interface FrpcName {

    /**
     * Returns a name under which this method should be published by the FastRPC server.
     * Defaults to empty string which indicates that the name of annotated method should be used.
     *
     * @return a name under which this method should be published by the FastRPC server
     */
    String value() default "";

}
