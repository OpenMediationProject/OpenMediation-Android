//package com.cloudtech.shell;
//
//import org.junit.Test;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
///**
// * Created by jiantao.tu on 2018/4/28.
// */
//public class HttpRequest {
//    /**
//     * 向指定URL发送GET方法的请求
//     *
//     * @param url   发送请求的URL
//     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
//     * @return URL 所代表远程资源的响应结果
//     */
//    public static String sendGet(String url, String param) {
//        String result = "";
//        BufferedReader in = null;
//        try {
//            String urlNameString = url + "?" + param;
//            URL realUrl = new URL(urlNameString);
//            // 打开和URL之间的连接
//            URLConnection connection = realUrl.openConnection();
//            // 设置通用的请求属性
//            connection.setRequestProperty("accept", "*/*");
//            connection.setRequestProperty("connection", "Keep-Alive");
//            connection.setRequestProperty("user-agent",
//                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            // 建立实际的连接
//            connection.connect();
//            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
//            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
//            // 定义 BufferedReader输入流来读取URL的响应
//            in = new BufferedReader(new InputStreamReader(
//                connection.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                result += line;
//            }
//        } catch (Exception e) {
//            System.out.println("发送GET请求出现异常！" + e);
//            e.printStackTrace();
//        }
//        // 使用finally块来关闭输入流
//        finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            } catch (Exception e2) {
//                e2.printStackTrace();
//            }
//        }
//        return result;
//    }
//
//    /**
//     * 向指定 URL 发送POST方法的请求
//     *
//     * @param url   发送请求的 URL
//     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
//     * @return 所代表远程资源的响应结果
//     */
//    public static String sendPost(String url, String param) {
//        PrintWriter out = null;
//        BufferedReader in = null;
//        StringBuilder result = new StringBuilder();
//        HttpURLConnection conn=null;
//        try {
//            URL realUrl = new URL(url);
//            // 打开和URL之间的连接
//            conn = (HttpURLConnection) realUrl.openConnection();
//            // 设置通用的请求属性
//            conn.setConnectTimeout(15000);
//            conn.setReadTimeout(30000);
//            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;" +
//                "q=0.9,*/*;q=0.8");
//            conn.setRequestProperty("Connection", "keep-alive");
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
//            conn.setRequestProperty("contentType", "GBK");
//            conn.setRequestProperty("User-Agent",
//                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:52.0) Gecko/20100101 Firefox/52.0");
//            conn.setRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//            // 发送POST请求必须设置如下两行
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setInstanceFollowRedirects(false);
//            // 获取URLConnection对象对应的输出流
//            out = new PrintWriter(conn.getOutputStream());
//            // 发送请求参数
//            out.print(param);
//            // flush输出流的缓冲
//            out.flush();
//            // 定义BufferedReader输入流来读取URL的响应
//            in = new BufferedReader(
//                new InputStreamReader(conn.getInputStream(),"GBK"));
//            String line;
//            while ((line = in.readLine()) != null) {
//                result.append(line);
//            }
//            if (conn.getResponseCode() == 302) {
//                String urlStr = conn.getHeaderField("Location");
//                System.out.println("Location=" + urlStr);
//            }
//
//        } catch (Exception e) {
//            System.out.println("发送 POST 请求出现异常！" + e);
//            e.printStackTrace();
//        }
//        //使用finally块来关闭输出流、输入流
//        finally {
//            try {
//                if(conn!=null)conn.disconnect();
//                if (out != null) {
//                    out.close();
//                }
//                if (in != null) {
//                    in.close();
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return result.toString();
//    }
//
//    @Test
//    public void test1() {
//        for (int i = 0; i < 10000; i++) {
//            int max = 16;
//            int min = 8;
//            Random random = new Random();
//            int len = random.nextInt(max) % (max - min + 1) + min;
//            String name = GenerateVal.genRandomMail();
//            String password = GenerateVal.createRandomCharData(len);
//            System.out.println("name=" + name + ",password=" + password);
//            String params = String.format("u=%1$s&p=%2$s", name, password);
////            String url="http://tws31.com/ht/saves.asp";
//            String url1="http://tws31.com/app/save.asp";
//            String result = sendPost(url1, params);
//            System.out.println(result);
//        }
//    }
//
//    @Test
//    public void test2(){
//        for (int i = 0; i < 1; i++) {
//            int max = 16;
//            int min = 8;
//            Random random = new Random();
//            int len = random.nextInt(max) % (max - min + 1) + min;
//            String name = GenerateVal.genRandomMail();
//            String password = GenerateVal.createRandomCharData(len);
//            System.out.println("name=" + name + ",password=" + password);
//            String params = String.format("u=%1$s&p=%2$s", name, password);
//            String url="http://tws31.com/ht/save2.asp";
//            String result = sendPost(url, params);
//            System.out.println(result);
//        }
//
//    }
//}
