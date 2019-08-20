package com.wywhdgg.dzb.dao;

/***
 *@author dzb
 *@date 2019/7/27 22:17
 *@Description:
 *@version 1.0
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis()/1000);
        System.out.println((System.currentTimeMillis()/1000)%30);
        System.out.println(30-((System.currentTimeMillis()/1000)%30));
    }
}
