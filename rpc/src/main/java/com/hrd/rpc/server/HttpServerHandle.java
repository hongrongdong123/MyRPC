package com.hrd.rpc.server;

import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.registry.LocalRegistry;
import com.hrd.rpc.serializer.JdkSerializer;
import com.hrd.rpc.serializer.Serializer;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;


/**
 * description:
 */
public class HttpServerHandle {
    public void handle(HttpServletRequest req, HttpServletResponse resp) {
        //  处理请求

        // 记录日志
        System.out.println("Received request: " + req.getMethod() + " " + req.getRequestURI());

        //构造响应结果
        RpcResponse rpcResponse = new RpcResponse();
        try {
            //指定序列化器
            Serializer serializer = new JdkSerializer();

            // 从请求中读取字节数据
            byte[] bodyBytes = readBytesFromRequest(req);

            //反序列化
            RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);

            //服务发现
            Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
            Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(implClass.newInstance(), rpcRequest.getParameters());

            //封装返回结果
            rpcResponse.setData(result);
            rpcResponse.setMessage("ok");
            rpcResponse.setDataType(method.getReturnType());

            //再序列化
            byte[] bytes = serializer.serialize(rpcResponse);

            // 设置响应内容类型
            resp.setContentType("application/json");
            resp.setContentLength(bytes.length);

            // 写入响应
            resp.getOutputStream().write(bytes);
            resp.getOutputStream().flush();

        }catch (Exception e) {
            e.printStackTrace();
            rpcResponse.setException(e);
            rpcResponse.setMessage(e.getMessage());
            try {
                // 序列化错误响应并写回
                byte[] errorBytes = new JdkSerializer().serialize(rpcResponse);
                resp.getOutputStream().write(errorBytes);
                resp.getOutputStream().flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
    private byte[] readBytesFromRequest(HttpServletRequest request) throws IOException {
        InputStream inputStream = request.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
