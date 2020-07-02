package com.cloudtech.shell;

import java.util.Random;

/**
 * Created by jiantao.tu on 2018/4/28.
 */
public class GenerateVal {

    //根据指定长度生成字母和数字的随机数
    //0~9的ASCII为48~57
    //A~Z的ASCII为65~90
    //a~z的ASCII为97~122
    public static String createRandomCharData(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();//随机用以下三个随机生成器
        Random randData = new Random();
        int data;
        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(3);
            //目的是随机选择生成数字，大小写字母
            switch (index) {
                case 0:
                    data = randData.nextInt(10);//仅仅会生成0~9
                    sb.append(data);
                    break;
                case 1:
                    data = randData.nextInt(26) + 65;//保证只会产生65~90之间的整数
                    sb.append((char) data);
                    break;
                case 2:
                    data = randData.nextInt(26) + 97;//保证只会产生97~122之间的整数
                    sb.append((char) data);
                    break;
            }
        }
        return sb.toString();
    }

    public static String genRandomMail() {
// 26*2个字母+10个数字
        final int maxNum = 36;
        int i; // 生成的随机数
        int count = 0; // 生成的密码的长度
        char[] str = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        Random r = new Random();
        int pwd_len = (int) (5 + Math.random() * (12 - 5 + 1));
        StringBuilder sb = new StringBuilder("");
        while (count < pwd_len) {
// 生成随机数，取绝对值，防止生成负数，
            i = Math.abs(r.nextInt(maxNum)); // 生成的数最大为62-1
            if (i >= 0 && i < str.length) {
                sb.append(str[i]);
                count++;
            }
        }
        String account = sb.toString();
        String[] mails = {"qq", "126", "163", "hotmail", "outlook", "139", "sina", "189", "yeah"};
        int max = 8;
        int min = 0;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return account + "@" + mails[s] + ".com";
    }
}
