package org.example;

public interface UserMapper {
    User selectById(@Param(value = "id") int id);
    User selectByName(@Param(value = "name") String name);

    User selectByNameAndAge(@Param(value = "name") String name, @Param(value = "age") int age);
}
