package com.cloudtech.shell.ex;

/**
 * Created by jiantao.tu on 2018/4/9.
 */
public class NotMakeException extends Exception {

    public NotMakeException(){
        super("This is not exists make , get dex make error");
    }

}
