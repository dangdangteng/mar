package com.dinglicom.mr.util;

import lombok.extern.java.Log;

import java.math.BigDecimal;

@Log
public class NumCompare {
    /**
     * @param a 失败数量int
     * @param b 任务总数int
     * @param c 比较参数double
     * @return
     */
    public static Boolean doubleCompare(int a, int b, double c) {
        BigDecimal aDecimal = new BigDecimal(a);
        BigDecimal bDecimal = new BigDecimal(b);
        BigDecimal compare = new BigDecimal(c);
        BigDecimal divide = aDecimal.divide(bDecimal);
        if (divide.compareTo(compare)>0){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        //
        Boolean aBoolean = doubleCompare(0, 6,0.2);
        System.out.println(aBoolean);
    }
}
