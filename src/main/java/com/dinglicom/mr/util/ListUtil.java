package com.dinglicom.mr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListUtil {
    public static List<List<String>> splitList(List<String> list, int groupSize) {
        if (list == null || list.size() == 0){
            return null;
        }
        int length = list.size();
        int num = (length + groupSize - 1) / groupSize;
        CopyOnWriteArrayList<List<String>> newList = new CopyOnWriteArrayList<>();
        for (int i = 0; i < num; i++) {
            int fromIndex = i * groupSize;
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
