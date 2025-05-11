package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MySqlSessionFactory {
    private static final String URL = "jdbc:mysql://localhost:3306/sql_learn?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    @SuppressWarnings("all")
    public <T> T getMapper(Class<T> mapperClass){

        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{mapperClass}, new MapperInvocationHanlder()
        );
    }

    static  class MapperInvocationHanlder implements  InvocationHandler{
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().startsWith("select")){
                return invokeSelect(proxy, method, args);
            }
            return null;
        }

        private Object invokeSelect(Object proxy, Method method, Object[] args) {
            String sql = createSelectSql(method);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = conn.prepareStatement(sql)
            ){
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Integer){
                        statement.setInt(i+1, (int) arg);
                    } else if (arg instanceof String){
                        statement.setString(i+1, (String) arg);
                    }
                }
                ResultSet rs = statement.executeQuery();
                if (rs.next()){
                    return parseResult(rs,method.getReturnType());
                }
            }catch (Exception e){

            }
            return null;
        }

        private Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();
            Field[] declaredFields = returnType.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Object column = null;
                String name = declaredField.getName();
                if (declaredField.getType() == Integer.class){
                    column = rs.getInt(name);
                } else if (declaredField.getType() == String.class){
                    column = rs.getString(name);
                }
                declaredField.setAccessible(true);
                declaredField.set(result, column);
            }
            return result;
        }

        private String createSelectSql(Method method) {
            List<String> selectedCols = getSelectCols(method.getReturnType());
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select ");
            sqlBuilder.append(String.join(",", selectedCols));
            sqlBuilder.append(" from ");
            sqlBuilder.append(getSelectedTableName(method.getReturnType()));
            sqlBuilder.append(" where ");
            String whereSql = getSelectWhere(method);
            sqlBuilder.append(whereSql);
            System.out.println(sqlBuilder);
            return sqlBuilder.toString();
        }

        private String getSelectWhere(Method method) {
            return Arrays.stream(method.getParameters()).map(
                    (parameter) -> {
                        Param param = parameter.getAnnotation(Param.class);
                        return param.value() + " = ? ";
                    }).collect(Collectors.joining(" and "));
        }

        private String getSelectedTableName(Class<?> returnType) {
            Table table = returnType.getAnnotation(Table.class);
            if (table == null){
                throw new RuntimeException("返回值无法确定查询表");
            }
            return table.tableName();
        }

        private List<String> getSelectCols(Class<?> returnType) {
            Field[] declaredFields = returnType.getDeclaredFields();
            List<String> list = Arrays.stream(declaredFields).map(Field::getName).toList();
            return list;
        }
    }

//    public static void main(String[] args) {
//        Connection connection = null;
//        Statement statement = null;
//        ResultSet resultSet = null;
//
//        try {
//            // 1. 注册驱动（JDBC 4.0 起可省略，但保留更安全）
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            // 2. 获取数据库连接
//            connection = DriverManager.getConnection(URL, USER, PASSWORD);
//
//            // 3. 创建 Statement 对象
//            statement = connection.createStatement();
//
//            // 4. 执行查询 SQL
//            String query = "SELECT * FROM user";
//            resultSet = statement.executeQuery(query);
//
//            // 5. 遍历结果集
//            System.out.println("用户列表：");
//            while (resultSet.next()) {
//                int id = resultSet.getInt("id");
//                String name = resultSet.getString("name");
//                int age = resultSet.getInt("age");
//
//                System.out.println("ID: " + id + ", Name: " + name + ", Age: " + age);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // 6. 关闭资源
//            try { if (resultSet != null) resultSet.close(); } catch (Exception e) {}
//            try { if (statement != null) statement.close(); } catch (Exception e) {}
//            try { if (connection != null) connection.close(); } catch (Exception e) {}
//        }
//    }
}
