package com.example.ipcsocket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

//先来看看服务端的设计
public class TCPServerService extends Service {
    private boolean mISServiceDestroyed = false;
    //自定义的消息
    private String[] mDefinedMessages = new String[]{
            "你好啊，哈哈",
            "请问你叫什么名字",
            "今天北京天气不错啊",
            "你知道吗？我可是可以和多个人聊天的哦",
            "给你讲个笑话吧：据说爱笑的人运气不会差，不知道真假"
    };

    public TCPServerService() {
    }
    //0.服务启动
    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mISServiceDestroyed = true ;
        super.onDestroy();
    }
    //1.当服务启动的时候，会在线程中建立TCP服务。
    //然后就可以等待用户的连接请求
    private class TcpServer implements Runnable{

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                //监听本地8688端口
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                System.err.println("establish tcp server failed , port:8688");
                e.printStackTrace();
                return ;
            }

            //2.如果有客户端申请，就创建一个Socket
            while(!mISServiceDestroyed){
                try {
                    //接收客户端的申请
                    final Socket client = serverSocket.accept();
                    System.out.println("accept");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //3.服务端每接收到客户端的消息
    //4.就会随机发送一句话给客户端
    private void responseClient(Socket client) throws IOException{
        //用于接收客户端消息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //用于向客户带发送信息
        PrintWriter out =new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
        out.println("欢迎来到聊天室");

        //5.当客户端断开连接时，关闭对应Socket并结束通话流程。、
        //这里通过观察in及客户端的返回值来判断是否断开连接
        while(!mISServiceDestroyed){
            String str = in.readLine();
            System.out.println("msg from client:"+str);
            if(str == null){
                //客户端断开连接
                break;
            }
            int i = new Random().nextInt(mDefinedMessages.length);
            String msg = mDefinedMessages[i];
            out.println(msg);
            System.out.println("send:"+msg);
        }
        System.out.println("client quit.");
        //6.检测到客户端断开连接后，关闭流
        MyUtils.close(out);
        MyUtils.close(in);
        client.close();

    }
}