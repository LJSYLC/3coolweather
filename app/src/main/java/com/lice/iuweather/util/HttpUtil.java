package com.lice.iuweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();//客户端类，全局实例，只使用一个对象就可以
        Request request = new Request.Builder()//封装了一些请求报文的信息,request build模式创建对象
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);//request,连接request和response桥梁，不阻塞进程，异步，开启子线程，回调onresponse
    //dispatcher
    }
    //拦截器：
    // 1.失败重试重定向
    //2.bridgeinterceptor 用户构造的请求转换为发送到服务器的请求，服务器返回的响应转回给用户友好输出
    //3缓存，读取更新缓存
    //4.和服务器进行连接的连接器
}
