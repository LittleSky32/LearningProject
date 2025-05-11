package tech.insight.sub;

import tech.insight.BeanPostProcessor;
import tech.insight.Component;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object beforeInitializeBean(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        System.out.println(beanName + " after Initialize");
        return bean;
    }
}
