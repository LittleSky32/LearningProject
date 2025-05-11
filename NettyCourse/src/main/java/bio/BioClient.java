package bio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BioClient {
    public static void main(String[] args) throws Exception {
        Thread tom = new Thread(() -> {
            try {
                sendHello();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "Tom");

        Thread jerry = new Thread(() -> {
            try {
                sendHello();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "Jerry");

        tom.start();
        jerry.start();
        tom.join();
        jerry.join();

    }

    private static void sendHello() throws Exception {
        // 客户端发送消息
        Socket client = new Socket();
        client.connect(new InetSocketAddress("localhost", 8080));
        OutputStream outputStream = client.getOutputStream();
        for (int i = 0; i < 10; i++) {
            outputStream.write((Thread.currentThread().getName() + " hello " + i).getBytes());
            outputStream.flush();
        }
        Thread.sleep(10000);
        client.close();
    }
}
