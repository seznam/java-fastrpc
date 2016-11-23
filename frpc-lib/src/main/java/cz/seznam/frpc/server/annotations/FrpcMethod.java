package cz.seznam.frpc.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify arbitrary {@code FRPC} method value within a handler. Any public non-static method
 * annotated with this annotation will be registered under the value specified as a value of this annotation.
 * <br />
 * This annotation can be used to disambiguate multiple methods of the same value within one class.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FrpcMethod {

    /**
     * Specifies a value under which this method should be published by the {@code FRPC} server. <br />
     * Defaults to empty string which indicates that the value of the annotated method should be used.
     *
     * @return a value under which this method should be published by the {@code FRPC} server
     */
    String value() default "";

}
