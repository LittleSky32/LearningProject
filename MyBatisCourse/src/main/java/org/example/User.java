package org.example;

import lombok.Data;

@Data
@Table(tableName = "user")
public class User {
    Integer id;
    Integer age;
    String name;
}
