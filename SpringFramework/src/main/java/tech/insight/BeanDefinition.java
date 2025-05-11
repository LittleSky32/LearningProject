package tech.insight;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

// Bean的设计图
public class BeanDefinition {

    private final String name;
    private final Constructor<?> constructor;
    private final Method postConstructMethod;
    private final List<Field> autowiredFields;
    private final Class<?> beanType;

    public Class<?> getBeanType() {
        return beanType;
    }

    public BeanDefinition(Class<?> type){
        Component component = type.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        this.postConstructMethod = Arrays.stream(type.getDeclaredMethods()).
                filter(m -> m.isAnnotationPresent(PostConstruct.class)).
                findFirst().orElse(null);
        this.autowiredFields = Arrays.stream(type.getDeclaredFields()).
                filter(f -> f.isAnnotationPresent(Autowired.class)).toList();
        this.beanType = type;
        try {
            this.constructor = type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName(){
        return name;
    }

    public Constructor<?> getConstructor(){
        return constructor;
    }

    public Method getPostConstructMethod(){
        return postConstructMethod;
    }

    public List<Field> getAutowiredFields() {
        return autowiredFields;
    }
}
