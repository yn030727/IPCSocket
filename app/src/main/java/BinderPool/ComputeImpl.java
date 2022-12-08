package BinderPool;

import android.os.RemoteException;

import com.example.ipcsocket.ICompute;

public class ComputeImpl extends ICompute.Stub {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a+b;
    }
}
