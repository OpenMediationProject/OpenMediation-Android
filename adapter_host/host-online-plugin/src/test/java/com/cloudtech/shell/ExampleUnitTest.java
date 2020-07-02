package com.cloudtech.shell;

import com.cloudtech.shell.utils.MD5;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void md5Test() throws Exception {
        File file = new File("libs/app-release.apk");
//        String md5Str = MD5.getMd5ByFile(file);
//        System.out.print(md5Str);
    }


    @Test
    public void NULLTest() {
        String str = null;
        String a = (String) str;
        if (a == null) {
            System.out.println("aaaaaaaaaaaaa");
        } else {
            System.out.println(a);
        }
    }


    @Test
    public void hexCompare() {
        System.out.print((1 << 2) & 4);
    }

    @Test
    public void generateUrl() {
        String url = Apis.SDK_UPDATE_API + "?vs=%1$s&pkg=%2$s&slot=%3$s&aid=%4$s&gaid=%5$s&time=%6$s";
        String str = String.format(url, "js,1.1.0,houjie,1.2.0","com.mobi","247",
                "sdfdsfd", "ssss",System.currentTimeMillis());
        System.out.println(str);
    }

    @Test
    public void time(){
        System.out.println(TimeUnit.SECONDS.toMillis(10));
    }


//    @Test
//    public void test1(){
//        String url = "http://www.baidu.com?vs=%1$s&pkg=%2$s&slot=%3$s&aid=%4$s&gaid=%5$s&time=%6$s";
//        String str = String.format(url, "js,1.1.0,houjie,1.2.0","com.mobi","247",
//            "sdfdsfd", "ssss",System.currentTimeMillis());
//        System.out.println("url="+url);
//        String queryStr=URI.create(str).getQuery();
//        System.out.println("queryStr: "+queryStr);
//        String queryXor=CheckSum.xor(queryStr);
//        System.out.println("queryXor: "+queryXor);
//        System.out.println("checksum: "+CheckSum.getKey(234 & 0xff));
//
//    }


    @Test
    public void test2(){
        String str="";
        boolean isDel=false;
        boolean isActive=false;
        boolean isDown=false;
        System.out.println(!(isDel || isActive || isDown));
//        StringBuilder sb=new StringBuilder("subscription,4,1.0.0,promote,4,1.0.0,,");
//        if (sb.lastIndexOf(",")==sb.length()-1) {
//            str=sb.substring(0,sb.length()-1);
//        }else{
//            str=sb.toString();
//        }
//        System.out.println(str);
    }

}