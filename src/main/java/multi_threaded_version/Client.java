package multi_threaded_version;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

//读取服务器信息线程 ReadFromServerThread
//通过继承Runnable接口建立线程
class ReadFromServerThread implements Runnable{
    private Socket client;
    //构造方法及属性
    public ReadFromServerThread(Socket client){
        this.client = client;
    }

    public void run() {
        try {
            //1、获取客户端输入流
            Scanner input = new Scanner(client.getInputStream());
            input.useDelimiter("\r\n");
            while (true){
                //2、判断服务器是否发来消息，即客户端输入流是否有内容
                if (input.hasNext()){
                    System.out.println("服务器发来的消息为："+input.next());
                }
                //3、当客户端处于被关闭状态时，输入流中内容无意义
                if (client.isClosed()){
                    System.out.println("客户端已关闭");
                    break;
                }
            }
            input.close();
        }catch (IOException e){
            System.err.println("客户端读取出现异常，异常为："+e);
        }
    }
}


//将信息发送给服务器线程 WriteToServerThread
class WriteToServerThread implements Runnable{
    private Socket client;
    public WriteToServerThread(Socket client){
        this.client = client;
    }
    public void run() {
        try {
            //1、提供客户端键盘输入
            Scanner in = new Scanner(System.in);
            in.useDelimiter("\r\n");
            //2、获取客户端的输出流
            PrintStream out = new PrintStream(client.getOutputStream());
            //3、将输入框内容写入到输出流中
            while (true){
                System.out.println("请输入要发送的信息....");
                String strToServer;
                if (in.hasNext()){
                    strToServer = in.nextLine().trim();
                    out.println(strToServer);
                    //4、客户端主动退出时要告诉服务器，定退出标志为886
                    if (strToServer.equals("byebye")){
                        System.out.println("关闭客户端");
                        in.close();
                        out.close();
                        client.close();
                        break;
                    }
                }
            }
        }catch (IOException e){
            System.err.println("客户端写入线程出现异常，异常为："+e);
        }
    }
}
//多线程中的客户端
public class Client {
    public static void main(String[] args) throws IOException {
        try{
            Socket client = new Socket("127.0.0.1",6666);
            //读取服务器消息线程
            Thread readFromServer = new Thread(new ReadFromServerThread(client));
            //向服务器发送消息线程
            Thread writeToServer = new Thread(new WriteToServerThread(client));
            readFromServer.start();
            writeToServer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
