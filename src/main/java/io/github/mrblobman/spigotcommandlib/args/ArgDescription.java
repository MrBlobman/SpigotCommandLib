package io.github.mrblobman.spigotcommandlib.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide an argument description for an argument to use
 * rather than the default. <br>
 * Note: Leaving the default value
 * will result in the command manager deciding on the values
 * itself.
 * Created on 11/12/2015.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgDescription {

    /**
     * @return a description of this argument
     */
    String[] description() default {};

    /**
     * @return the name of this argument
     */
    String name() default "";
}
