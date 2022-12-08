// ISecurityCenter.aidl
package com.example.ipcsocket;

// Declare any non-default types here with import statements

interface ISecurityCenter {
    //ISecurityCenter接口提供解密功能
    String encrypt(String content);
    String decrypt(String password);
}