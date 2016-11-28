package cz.seznam.frpc.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify arbitrary {@code FRPC} method name within a handler. When using
 * {@link cz.seznam.frpc.server.ReflectiveFrpcMethodLocator}, then any public non-static method annotated with this
 * annotation will be registered under the name specified as a value of this annotation.
 * <br />
 * This annotation can be used to disambiguate multiple methods of the same name within one class.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FrpcMethod {

    /**
     * Specifies a name under which this method should be published by the {@code FRPC} server. <br />
     * Defaults to empty string which indicates that the name of the annotated method should be used.
     *
     * @return a name under which this method should be published by the {@code FRPC} server
     */
    String value() default "";

}
