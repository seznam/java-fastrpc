package cz.seznam.frpc.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation used to mark non-static public methods in {@code FRPC} handlers which should not be
 * published by the {@code FRPC} server.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FrpcIgnore {
}
