package lab.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test class that starts Docker containers. Gazelle reads it from source and gives the
 * class its own target tagged {@code requires-docker}, so runners without Docker can filter it out.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RequiresDocker {}
