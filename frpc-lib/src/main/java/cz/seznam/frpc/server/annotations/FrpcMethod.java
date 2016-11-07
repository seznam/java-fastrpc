package cz.seznam.frpc.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify arbitrary {@code FRPC} method name within a handler. Any public non-static method
 * annotated with this annotation will be registered under the name specified as a name of this annotation.
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
    String name() default "";

    /**
     * Specifies a resultKey under which the result of the annotated method will be stored in the resulting map. <br />
     * Defaults to empty string which indicates no resultKey was specified. It is only legal to omit this property if the
     * annotated method itself returns a {@code Map}. In that case that map will be returned by the {@code FRPC} as
     * a result of {@code FRPC} method call. <br />
     * If the annotated method returns anything but {@code Map}, then setting this property is <strong>mandatory</strong>
     * and its value will be used as described above.
     *
     * @return a resultKey under which the result of the annotated method will be stored in the resulting map
     */
    String resultKey() default "";

}
