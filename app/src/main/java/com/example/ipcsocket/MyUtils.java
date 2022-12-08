package com.example.ipcsocket;

import java.io.Closeable;
import java.io.IOException;

public class MyUtils {
    public static void close(Closeable closeable){
        if(closeable!=null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
