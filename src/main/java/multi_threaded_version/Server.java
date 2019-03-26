package multi_threaded_version;


import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    //1、存储所有注册的客户端,使用ConcurrentHashMap较为安全
    private static Map<String ,Socket> clientMap =
            new ConcurrentHashMap<String, Socket>();
    //2、具体处理与每个客户端通信的内部类
    private static class ExecuteClient implements Runnable{
        private Socket client;
        public ExecuteClient(Socket client){
            this.client = client;
        }
        public void run() {
            try {
                //3、获取客户端输入流
                Scanner input = new Scanner(client.getInputStream());
                String strFromClient;
                while (true){
                    //4、对客户端输入流的内容进行处理
                    if (input.hasNext()){
                        strFromClient = input.nextLine();
                        //处理特殊情况：Windows下将默认换行\r\n中的\r替换为空格
                        Pattern pattern = Pattern.compile("\r");
                        Matcher matcher = pattern.matcher(strFromClient);
                        strFromClient = matcher.replaceAll(" ");
                        //注册的流程
                        if (strFromClient.startsWith("userName")){
                            String userName = strFromClient.split("\\:")[1];
                            registerUser(userName,client);
                            continue;
                        }
                        //群聊的流程
                        if (strFromClient.startsWith("G")){
                            String msg = strFromClient.split("\\:")[1];
                            groupChat(msg);
                            continue;
                        }
                        //私聊的流程
                        if (strFromClient.startsWith("P")){
                            String userName = strFromClient.split("\\:")[1].split("-")[0];
                            String msg = strFromClient.split("\\:")[1].split("-")[1];
                            privateChat(userName,msg);
                        }
                        //用户退出
                        if (strFromClient.contains("byebye")) {
                            String userName = null;
                            //根据Socket对象找到username
                            for (String keyName : clientMap.keySet()) {
                                if (clientMap.get(keyName).equals(client)) {
                                    userName = keyName;
                                }
                            }
                            System.out.println("用户："+userName+"下线了！");
                            clientMap.remove(userName);
                            continue;
                        }
                    }

                }
            }catch (IOException e){
                System.err.println("服务器通信异常，错误为："+e);
            }
        }

       //5、注册方法
       private void registerUser(String userName,Socket client){
           System.out.println("用户姓名为："+userName);
           System.out.println("用户"+userName+"上线了！");
           System.out.println("当前群聊人数为："+(clientMap.size()+1)+"人");
           //将用户信息保存到Map中
           clientMap.put(userName,client);
           try {
               PrintStream out = new PrintStream(client.getOutputStream(),true,"UTF-8");
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

       ///6、聊流程
       private void groupChat(String msg){
            //取出clientMap中所有Entry遍历发送群聊消息
           Set<Map.Entry<String,Socket>> clientSet = clientMap.entrySet();
           for (Map.Entry<String,Socket> entry:clientSet){
               Socket socket = entry.getValue();
               //取得每个客户端的输出流
               try {
                   PrintStream out = new PrintStream(socket.getOutputStream(),true,"UTF-8");
                   out.println("群聊消息为："+msg);
               } catch (IOException e) {
                   System.err.println("群聊异常，错误为："+e);
               }
           }
       }

       //7、私聊流程
        private void privateChat(String userName,String msg){
            Socket privateSocket = clientMap.get(userName);
            try {
                PrintStream out = new PrintStream(privateSocket.getOutputStream(),true,"UTF-8");
                out.println("私聊消息为："+msg);
            } catch (IOException e) {
                System.err.println("私聊异常，错误为："+e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        ServerSocket serverSocket = new ServerSocket(6666);
        for (int i =0;i<20;i++){
            System.out.println("等待客户端连接...");
            Socket client = serverSocket.accept();
            System.out.println("有新的客户端连接，端口号为："+client.getPort());
            executorService.submit(new ExecuteClient(client));
        }
        executorService.shutdown();
        serverSocket.close();
    }
}
