package cz.seznam.frpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FrpcName {

    /**
     * Returns a name under which this method should be published by the FastRPC server.
     * Defaults to empty string which indicates that the name of annotated method should be used.
     *
     * @return a name under which this method should be published by the FastRPC server
     */
    String value() default "";

}
