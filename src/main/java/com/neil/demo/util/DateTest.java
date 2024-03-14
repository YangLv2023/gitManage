package com.neil.demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTest {

    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stamp = "";
        if (!"".equals(time)) {//时间不为空
            try {
                stamp = String.valueOf(sdf.parse(time).getTime()/1000);
            } catch (Exception e) {
                System.out.println("参数为空！");
            }
        }else {    //时间为空
            long current_time = System.currentTimeMillis();  //获取当前时间
            stamp = String.valueOf(current_time/1000);
        }
        return stamp;
    }


    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(Long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time_Date = sdf.format(new Date(time));
        return time_Date;

    }

    public static void main(String[] args) {
        String time1 = "2020-2-7 12:30:56";
        Long time2 = 20210918190500L;
        System.out.println(System.currentTimeMillis());
        System.out.println("将时间转为时间戳："+dateToStamp(time1));
        System.out.println("将时间戳转为时间："+stampToDate(time2));
    }
}
