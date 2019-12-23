package com.dinglicom.mr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListUtil {
    public static List<List<String>> splitList(List<String> list, int groupSize) {
        int length = list.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize;
        CopyOnWriteArrayList<List<String>> newList = new CopyOnWriteArrayList<>();
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            newList.add(list.subList(fromIndex, toIndex));
        }
        return newList;
    }

    public static void main(String[] args) {
        //
        List a = new ArrayList();
        for (int i = 0; i < 34; i++) {
          a.add(i+":");
        }
        List list = splitList(a, 200);
    System.out.println(list);
    }
}
