package com.neil.demo.util;

import org.junit.Test;

import java.math.BigDecimal;

public class PriceTest {


    @Test
    public void priceTestMain(){
        System.out.println(this.priceTest(new BigDecimal("1.1110")));
    }

    /**
     * @Author ly
     * @Description 校验小数点后位数
     * @Date 9:00 2021/9/3
     **/
    public int priceTest(BigDecimal price){
        String priceStr = price.toString();
        return this.priceTest(priceStr);
    }

/*    public int priceTest(String priceStr){
        int index = priceStr.lastIndexOf(".");
        if(index < 0){
            return 0;
        }
        String flotStr = priceStr.substring(index + 1);
        char[] flotArr = flotStr.toCharArray();
        int i = flotArr.length-1;
        for(;i >= 0;i--){
            if(flotArr[i] != '0'){
                break;
            }
        }
        return ++i;
        //return priceStr.substring(priceStr.lastIndexOf(".") + 1).length();
    }*/

    public int priceTest(String priceStr){
        return new BigDecimal(priceStr).stripTrailingZeros().scale();
        //return priceStr.substring(priceStr.lastIndexOf(".") + 1).length();
    }
}
