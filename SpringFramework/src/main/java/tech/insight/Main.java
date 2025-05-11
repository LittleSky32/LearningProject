package tech.insight;

import tech.insight.ApplicationContext;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationContext ioc = new ApplicationContext("tech.insight");
    }
}