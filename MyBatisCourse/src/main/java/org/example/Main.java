package org.example;

public class Main {
    public static void main(String[] args) {
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        UserMapper userMapper = mySqlSessionFactory.getMapper(UserMapper.class);
        User user = userMapper.selectById(2);
        System.out.println(user);
        User user1 = userMapper.selectByName("Alice");
        System.out.println(user1);
        User user2 = userMapper.selectByNameAndAge("Alice", 25);
        System.out.println(user2);
    }
}
