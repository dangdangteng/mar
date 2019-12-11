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

import java.io.File;
import java.io.FileOutputStream;
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

    public static void main(String[] args) throws Exception {
        //        String xmlElementSet =
        // getXmlElementSet("/Users/saber-opensource/CQT-集团巡检VoLTE-指标模板_ByLog_V1.xml");
        //        Boolean wocaonima =
        // addUkElement("/Users/saber-opensource/dingli/mar/src/main/resources/NeighborCell.xml",
        // "wocaonima");
        //        System.out.println(wocaonima);
        //        List<String> itemFileNameList =
        List<String> itemFileNameList = getItemFileNameList("/Users/saber-opensource/dingli/mar/src/main/resources/a.xml");
        System.out.println(itemFileNameList.toString());
        System.out.println(itemFileNameList.size());
        removeElementByAttribute(
                "/home/fleet/fleetSwapDatas/swapTemps/report/20191126/677/report_1_9dc153fc5630906e4dd862c3f94.uk",
                "/Users/saber-opensource/dingli/mar/src/main/resources/a.xml",
                "FileName");
        List<String> a = getItemFileNameList("/Users/saber-opensource/dingli/mar/src/main/resources/a.xml");
        System.out.println(a.toString());
        System.out.println(a.size());
    }

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

    public static void removeElementByAttribute(String attributeNameValue, String fileName, String attributeNameKey) throws Exception {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(fileName));
        Element rootElement = document.getRootElement();
        Element UnifieldKpiFilesElement = rootElement.element("UnifieldKpiFiles");
        List<Element> itemElements = UnifieldKpiFilesElement.elements("Item");
        itemElements.stream()
                .forEach(
                        element -> {
                            if (element.attribute("FileName").getValue().equals(attributeNameValue)) {
                                boolean remove = UnifieldKpiFilesElement.remove(element);
                                logger.info(remove + "");
                            }
                        });
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
}
