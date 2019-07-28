package com.wywhdgg.dzb.core.listener.impl;

import com.wywhdgg.dzb.core.listener.ConfListener;
import com.wywhdgg.dzb.core.spring.ConfFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description: 配置监听接口
 */
@Slf4j
public class BeanRefreshConfListener implements ConfListener {

    // key : object-field[]
    private static Map<String, List<BeanField>> key2BeanField = new ConcurrentHashMap<String, List<BeanField>>();


    public static void addBeanField(String key, BeanField beanField){
        List<BeanField> beanFieldList = key2BeanField.get(key);
        if (beanFieldList == null) {
            beanFieldList = new ArrayList<>();
            key2BeanField.put(key, beanFieldList);
        }
        for (BeanField item: beanFieldList) {
            if (item.getBeanName().equals(beanField.getBeanName()) && item.getProperty().equals(beanField.getProperty())) {
                return; // avoid repeat refresh
            }
        }
        beanFieldList.add(beanField);
    }

    @Override
    public void onChange(String key, String value) throws Exception {
        List<BeanField> beanFieldList = key2BeanField.get(key);
        if (beanFieldList!=null && beanFieldList.size()>0) {
            for (BeanField beanField: beanFieldList) {
                ConfFactory.refreshBeanField(beanField, value, null);
            }
        }
    }

    // object + field
    @Data
    @ToString
    public static class BeanField {
        private String beanName;
        private String property;

        public BeanField() {
        }

        public BeanField(String beanName, String property) {
            this.beanName = beanName;
            this.property = property;
        }

    }
}
