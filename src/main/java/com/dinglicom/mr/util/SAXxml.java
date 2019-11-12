package com.dinglicom.mr.util;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 解析xml,是否放到我这里来做
 */
public class SAXxml {
    public static void main(String[] args) throws Exception {
        //
        SAXxml saXxml = new SAXxml();
        Set xmlElementSet = saXxml.getXmlElementSet("/Users/saber-opensource/dingli/mar/src/main/resources/生成基站UK.xml");
        System.out.println(xmlElementSet.toString());
    }

    public Set getXmlElementSet(String path) throws Exception {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(path));
        Element rootElement = document.getRootElement();
        List<Element> elements = rootElement.elements();
        Set<String> collect = elements.parallelStream().map(element -> {
            return element.getName();
        }).collect(Collectors.toSet());
        return collect;
    }
}
