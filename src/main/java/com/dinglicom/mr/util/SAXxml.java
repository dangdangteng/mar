package com.dinglicom.mr.util;

import com.dinglicom.mr.response.MessageCode;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 解析xml,是否放到我这里来做
 */
public class SAXxml {

    private static Logger logger = LoggerFactory.getLogger(SAXxml.class);

    private final static ReentrantLock reentrantLock = new ReentrantLock();

    public static String getXmlElementSet(String path) throws Exception {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(path));
        Element rootElement = document.getRootElement();
        List<Element> elements = rootElement.elements();
        Set<String> collect = elements.parallelStream().map(element -> {
            return element.getName();
        }).collect(Collectors.toSet());
        boolean site = collect.add("Sites");
        if(site){
            return "1";
        }
        AtomicReference<String> ukFileName = new AtomicReference<>(new String());
        if (!site) {
            rootElement.attributes()
                    .parallelStream()
                    .forEach(
                            attribute -> {
                                String qualifiedName = attribute.getQualifiedName();
                                if ("ResultFile".equals(qualifiedName)){
                                    ukFileName.set(attribute.getValue());
                                }
                                OutputFormat format = OutputFormat.createPrettyPrint();
                                XMLWriter xmlWriter = null;
                                try {
                                    xmlWriter = new XMLWriter(new FileOutputStream(path), format);
                                    xmlWriter.write(document);
                                    xmlWriter.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
        }
        return ukFileName.get();
    }

    public static Boolean addUkElement(String xmlPath, String ukPath) throws Exception {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(xmlPath));
        Element rootElement = document.getRootElement();
        Element element = rootElement.element("Sites").element("Site");
        Attribute fileName = element.attribute("FileName");
        if (fileName.getName().equals("FileName")) {
            fileName.setValue(ukPath);
            logger.info(fileName.getValue());
            return true;
        }
        return false;
    }

    public static CopyOnWriteArrayList<String> getItemFileNameList(String xmlPathName) throws Exception {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(xmlPathName));
        Element rootElement = document.getRootElement();
        Element UnifieldKpiFilesElement = rootElement.element("UnifieldKpiFiles");
        List<Element> itemElements = UnifieldKpiFilesElement.elements("Item");
        CopyOnWriteArrayList<String> fileNameList = new CopyOnWriteArrayList<String>();
        itemElements
                .parallelStream()
                .forEach(
                        element -> {
                            Attribute fileName = element.attribute("FileName");
                            fileNameList.add(fileName.getValue());
                        });
        return fileNameList;
    }

    public static MessageCode<CopyOnWriteArrayList> snoreOppositeMe(String xmlPathName, Double proportion) throws Exception {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(xmlPathName));
        Element rootElement = document.getRootElement();
        Element UnifieldKpiFilesElement = rootElement.element("UnifieldKpiFiles");
        List<Element> itemElements = UnifieldKpiFilesElement.elements("Item");
        CopyOnWriteArrayList<String> fileNameList = new CopyOnWriteArrayList<>();
        itemElements
                .parallelStream()
                .forEach(
                        element -> {
                            Attribute fileName = element.attribute("FileName");
                            File file = new File(fileName.getValue());
                            if (file.exists()) {
                                logger.info("create file success!");
                                return;
                            }
                            try {
                                fileNameList.add(fileName.getValue());
                                reentrantLock.lock();
                                boolean remove = UnifieldKpiFilesElement.remove(element);
                                OutputFormat format = OutputFormat.createPrettyPrint();
                                XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(xmlPathName), format);
                                xmlWriter.write(document);
                                xmlWriter.close();
                                logger.info("移除文件 ：——————" + remove);
                            } catch (Exception e) {
                                logger.info(e.toString());
                            } finally {
                                reentrantLock.unlock();
                            }

                        });
            return new MessageCode(1, "将军额上能跑马，宰相肚里能撑船", fileNameList);
    }
    public static List<String> findSceneName(String xmlPath) throws Exception{
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(xmlPath));
        Element rootElement = document.getRootElement();
        List<Element> elements = rootElement.elements();
        List<List> collect = elements.parallelStream().map(element -> {
            if (element.getName().equals("Scenes")) {
                Element scenes = rootElement.element("Scenes");
                List<Element> scene = scenes.elements("Scene");
                List c = new CopyOnWriteArrayList();
                scene.parallelStream().forEach(element1 -> {
                    Attribute name = element1.attribute("Name");
                    c.add(name.getValue());
                });
                return c;
            }
            return null;
        }).collect(Collectors.toList());
        List list = collect.get(0);
        return list;
    }

  public static void main(String[] args) throws Exception {
    //
      List<String> sceneName = findSceneName("/Users/saber-opensource/dingli/mar/src/main/resources/自定义模板报表.xml");
    System.out.println(sceneName.toString());
  }
}
