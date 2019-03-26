package single_threaded_version;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException {
        //1、创建服务器端Socket对象
        ServerSocket serverSocket = new ServerSocket(6666);
        //2、等待客户端连接，连接后返回客户端Socket对象；否则线程一直阻塞在此处
        System.out.println("等待客户端连接...");
        try {
            Socket client = serverSocket.accept();
            //3、获取客户端的输入输出流
            Scanner clientInput = new Scanner(client.getInputStream());
            clientInput.useDelimiter("\r\n");
            PrintStream clientout = new PrintStream(client.getOutputStream(), true, "UTF-8");
            //4、读取客户端的输入
            if (clientInput.hasNext()) {
                System.out.println(client.getInetAddress() + "客户端说：" + clientInput.hasNext());
            }
            //5、向客户端输出
            clientout.println("Hi,I am Server!");
            //6、关闭输入输出流
            clientInput.close();
            clientout.close();
            serverSocket.close();
        }catch (IOException e){
            System.err.println("服务器端出现异常，异常为："+e);
        }
    }
}
