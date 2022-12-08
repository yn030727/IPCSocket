package BinderPool;

import android.os.RemoteException;

import com.example.ipcsocket.ISecurityCenter;

public class SecurityCenterI extends ISecurityCenter.Stub {
    private static final char SECRET_CODE = '^';

    //将传进来的字符串进行解密
    @Override
    public String encrypt(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        for(int i =0 ; i<chars.length ; i++){
            chars[i] ^= SECRET_CODE;
        }
        return new String(chars);
    }

    //用相同的方式给密码进行解密
    @Override
    public String decrypt(String password) throws RemoteException {
        return encrypt(password);
    }
}
