package single_threaded_version;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        //1、定义IP地址和端口号
        String name = "127.0.0.1";
        Integer port = 6666;
        //2、创建客户端Socket对象连接服务器
        try {
            Socket client = new Socket(name, port);
            System.out.println("已连接服务器，服务器地址：" + client.getInetAddress());
            //3、获取输入输出流
            PrintStream out = new PrintStream(client.getOutputStream(), true, "UTF-8");
            Scanner in = new Scanner(client.getInputStream());
            in.useDelimiter("\r\n");
            //4、向服务器端输出内容
            out.println("Hi,i am client!");
            //5、读取服务器输入
            if (in.hasNext()) {
                System.out.println("服务器发来的消息为：" + in.hasNext());
            }
            //6、关闭输入输出流
            in.close();
            out.close();
            client.close();
        }catch (IOException e){
            System.err.println("客户端通信出现异常，错误为"+e);
        }
    }
}
