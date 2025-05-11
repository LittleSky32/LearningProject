package tech.insight;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationContext {

    public ApplicationContext(String packageName) throws IOException, URISyntaxException {
        initContext(packageName);
    }

    private Map<String, Object> ioc = new HashMap<>();
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private Map<String, Object> loadingIoc = new HashMap<>();
    private List<BeanPostProcessor> postProcessors = new ArrayList<>();

    // ---- 拿对象------
    // 通过名称拿对象
    public Object getBean(String name){
        if (name == null){
            return null;
        }
        Object bean = this.ioc.get(name);
        if (bean != null){
            return bean;
        }
        if (beanDefinitionMap.containsKey(name)){
            return createBean(beanDefinitionMap.get(name));
        }
        return null;
    }
    // 通过类型拿对象
    public <T> T getBean(Class<T> beanType){
        String beanName = this.beanDefinitionMap.values().stream().
                filter(bd -> beanType.isAssignableFrom(bd.getBeanType())).
                map(BeanDefinition::getName).findFirst().orElse(null);
        return (T) getBean(beanName);
    }

    public <T> List<T> getBeans(Class<T> beanType){
        return this.beanDefinitionMap.values().stream().
                filter(bd -> beanType.isAssignableFrom(bd.getBeanType())).
                map(BeanDefinition::getName).
                map(this::getBean).map(bean -> (T) bean).toList();
    }

    // ---- 造对象 ------
    public void initContext(String packageName) throws IOException, URISyntaxException {
        // 先创建beanDefinition, 再createBean，这样可以让getBean可以通过BeanDefinitionMap确认是否有该bean存在
        scanPackage(packageName).stream().
                filter(this::scanCreate).forEach(this::wrapper);
        initBeanPostProcessor();
        beanDefinitionMap.values().forEach(this::createBean);

    }

    private void initBeanPostProcessor() {
        beanDefinitionMap.values().stream().
                filter(bd -> BeanPostProcessor.class.isAssignableFrom(bd.getBeanType())).
                map(this::createBean).map(bean -> (BeanPostProcessor) bean).
                forEach(postProcessors::add);
    }

    private List<Class<?>> scanPackage(String packageName) throws IOException, URISyntaxException {
        // input: a.b.c --> a\b\c
//        URL resource = this.getClass().getClassLoader().
//                getResource(packageName.replace(".", File.separator));
//        Path path = Path.of(resource.getFile());
        // windows会有前置\,用URI替代
        Path path = Paths.get(this.getClass().getClassLoader().
                getResource(packageName.replace(".", File.separator)).toURI());
        // 收集类
        List<Class<?>> classList = new ArrayList<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".class")){
                    String replaceStr = absolutePath.toString().replace(File.separator, ".");
                    int packageIndex = replaceStr.indexOf(packageName);
                    String className = replaceStr.substring(packageIndex, replaceStr.length() - ".class".length());
                    try {
                        classList.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classList;
    }

    protected boolean scanCreate(Class<?> type){
        return type.isAnnotationPresent(Component.class);
    }

    protected BeanDefinition wrapper(Class<?> type){
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(beanDefinition.getName())){
            throw new RuntimeException("bean名字重复");
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    protected Object createBean(BeanDefinition beanDefinition){
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)){
            return ioc.get(name);
        }
        if (loadingIoc.containsKey(name)){
            return loadingIoc.get(name);
        }
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition){
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            autowriedBean(bean, beanDefinition);
            bean = initializeBean(bean, beanDefinition);
            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    private Object initializeBean(Object bean, BeanDefinition beanDefinition) throws InvocationTargetException, IllegalAccessException {
        for (BeanPostProcessor postProcessor : postProcessors) {
            bean = postProcessor.beforeInitializeBean(bean, beanDefinition.getName());
        }
        Method postConstructMethod = beanDefinition.getPostConstructMethod();
        if (postConstructMethod != null){
            postConstructMethod.invoke(bean);
        }
        for (BeanPostProcessor postProcessor : postProcessors) {
            bean = postProcessor.afterInitializeBean(bean, beanDefinition.getName());
        }
        return bean;
    }

    // 自动注入
    private void autowriedBean(Object bean, BeanDefinition beanDefinition) throws IllegalAccessException {
        for (Field autowiredField : beanDefinition.getAutowiredFields()) {
            autowiredField.setAccessible(true);
            Object autowiredBean = null;
            autowiredField.set(bean, getBean(autowiredField.getType()));
        }
    }

}
