package com.example.ipcsocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;

    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;
    private PrintWriter mPrintWriter;
    private Socket mClientSocket;
    //5.Handler类处理发送过来的信息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_RECEIVE_NEW_MSG:{
                    mMessageTextView.setText(mMessageTextView.getText()+(String)msg.obj);
                }
                case MESSAGE_SOCKET_CONNECTED:{
                    mSendButton.setEnabled(true);
                }
                default:{
                    break;
                }
            }
        }
    };

    //1.客户端Activity启动的时候，会在onCreate中开启一个线程去连接服务端Socket
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMessageTextView = (TextView) findViewById(R.id.msg_container);
        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(this);
        mMessageEditText = (EditText) findViewById(R.id.msg);
        Intent service = new Intent(this, TCPServerService.class);
        startService(service);
        new Thread(){
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
    }

    //4.当Activity退出的时候，关闭Socket
    @Override
    protected void onDestroy() {
        if( mClientSocket != null){
            try {
                mClientSocket.shutdownOutput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v == mSendButton){
            //将编辑框上的文字读取下来赋值给msg
            final String msg = mMessageEditText.getText().toString();
            if(!TextUtils.isEmpty(msg) && mPrintWriter != null){
                mPrintWriter.println(msg);
                mMessageEditText.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showedMsg = "self " + time + ":" + msg + "\n";
                mMessageTextView.setText(mMessageTextView.getText() + showedMsg);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String formatDateTime(long time) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }


    private void connectTCPServer(){
        //2.连接服务端的Socket
        //这里采用了超时重连的策略，每次连接失败都会重新建立尝试建立连接。
        //为了降低重试机制带来的开销，我们加入休眠的机制
        Socket socket = null;
        while(socket == null){
            try {
                socket = new Socket("localhost",8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("connect server success");
            } catch (IOException e) {
                //每次重启前停止1000ms
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed , retry...");
                e.printStackTrace();
            }
        }
        //3.连接成功后，就可以和服务端进行通信了
        //下面通过while不断读取服务端发送过来的消息，当Activity退出的时候，就退出循环并终止线程
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(!MainActivity.this.isFinishing()){
                String msg = br.readLine();
                System.out.println("receive :"+msg);
                if(msg!=null){
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "server" + time + ":" + msg + "\n";
                    //5.处理发送过来的消息
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG,showedMsg).sendToTarget();
                }
            }
            //关闭流
            System.out.println("quit...");
            MyUtils.close(mPrintWriter);
            MyUtils.close(br);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}