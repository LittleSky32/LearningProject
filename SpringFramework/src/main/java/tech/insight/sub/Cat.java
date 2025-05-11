package tech.insight.sub;

import tech.insight.Autowired;
import tech.insight.Component;
import tech.insight.PostConstruct;

@Component
public class Cat {
    @Autowired
    private Dog dog;
    @PostConstruct
    public void init(){
        System.out.println("Cat创建了 cat里有一个属性" + dog);
    }
    public Cat(){}
}
