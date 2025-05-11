package tech.insight.sub;

import tech.insight.Autowired;
import tech.insight.Component;
import tech.insight.PostConstruct;

@Component(name = "myDog")
public class Dog {
    @Autowired
    Cat cat;
    @Autowired
    Dog dog;

    @PostConstruct
    public void init(){
        System.out.println("Dog创建了 dog里有一个属性" + cat);
        System.out.println("Dog创建了 dog里有一只dog" + dog);
    }
}
