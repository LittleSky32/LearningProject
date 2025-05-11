package tech.insight;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 类上
@Retention(RetentionPolicy.RUNTIME) // 可以在运行时获得注解信息
public @interface Component {
    String name() default "";
}
