package cz.seznam.frpc;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation used to mark non-static public methods in FastRPC handlers which should not be
 * published by the FastRPC server.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FrpcIgnore {
}