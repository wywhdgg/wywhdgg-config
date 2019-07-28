package com.wywhdgg.dzb.core.spring;

import com.wywhdgg.dzb.core.annotation.Conf;
import com.wywhdgg.dzb.core.client.ConfClient;
import com.wywhdgg.dzb.core.exception.ConfException;
import com.wywhdgg.dzb.core.factory.ConfBaseBeanFactory;
import com.wywhdgg.dzb.core.listener.impl.BeanRefreshConfListener;
import com.wywhdgg.dzb.core.util.FieldReflectionUtil;
import com.wywhdgg.dzb.core.util.XmlUtils;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.ReflectionUtils;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:  通过配置自动注入工厂bean
 *
 */
@Slf4j
public class ConfFactory extends InstantiationAwareBeanPostProcessorAdapter implements InitializingBean , DisposableBean , BeanNameAware , BeanFactoryAware {


    private  static BeanFactory beanFactory;

    private String beanName;

    private String envprop;		// like "wywhdgg-conf.properties" or "file:/data/webapps/wywhdgg-conf.properties", include the following env config
    /**http地址*/
    private String adminAddress;
    /**环境*/
    private String env;
    /**密钥*/
    private String accessToken;
    /**镜像文件*/
    private String mirrorfile;

    public void setEnvprop(String envprop) {
        this.envprop = envprop;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setMirrorfile(String mirrorfile) {
        this.mirrorfile = mirrorfile;
    }




    /**
     *在bean实例化之后，
     * 通过构造函数或工厂方法，
     * 但在Spring属性填充（来自显式属性或自动装配）
     * 之前执行操作。
     * url:https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/InstantiationAwareBeanPostProcessorAdapter.html
     * 注释方式
     * **/
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        // 1、Annotation('@Conf')：resolves conf + watch
        if (!beanName.equals(this.beanName)) {
            ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    if (field.isAnnotationPresent(Conf.class)) {
                        String propertyName = field.getName();
                        Conf xxlConf = field.getAnnotation(Conf.class);

                        String confKey = xxlConf.value();
                        String confValue = ConfClient.get(confKey, xxlConf.defaultValue());


                        // resolves placeholders
                        BeanRefreshConfListener.BeanField beanField = new BeanRefreshConfListener.BeanField(beanName, propertyName);
                        refreshBeanField(beanField, confValue, bean);

                        // watch
                        if (xxlConf.callback()) {
                            BeanRefreshConfListener.addBeanField(confKey, beanField);
                        }

                    }
                }
            });
        }

        return super.postProcessAfterInstantiation(bean, beanName);
    }


    /**
     * 在工厂将它们应用于给定bean之前对给定属性值进行后处理，而不需要属性描述符。
     * xml解析方式
     * */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        log.info("xml  begin init...... ");
        // 2、XML('$XxlConf{...}')：resolves placeholders + watch
        if (!beanName.equals(this.beanName)) {

            PropertyValue[] pvArray = pvs.getPropertyValues();
            for (PropertyValue pv : pvArray) {
                if (pv.getValue() instanceof TypedStringValue) {
                    String propertyName = pv.getName();
                    String typeStringVal = ((TypedStringValue) pv.getValue()).getValue();
                    if (XmlUtils.xmlKeyValid(typeStringVal)) {

                        // object + property
                        String confKey = XmlUtils.xmlKeyParse(typeStringVal);
                        String confValue = ConfClient.get(confKey, "");

                        // resolves placeholders
                        BeanRefreshConfListener.BeanField beanField = new BeanRefreshConfListener.BeanField(beanName, propertyName);
                        //refreshBeanField(beanField, confValue, bean);

                        Class propClass = String.class;
                        for (PropertyDescriptor item: pds) {
                            if (beanField.getProperty().equals(item.getName())) {
                                propClass = item.getPropertyType();
                            }
                        }
                        Object valueObj = FieldReflectionUtil.parseValue(propClass, confValue);
                        pv.setConvertedValue(valueObj);

                        // watch
                        BeanRefreshConfListener.addBeanField(confKey, beanField);

                    }
                }
            }

        }
        return super.postProcessPropertyValues(pvs, pds, bean, beanName);
    }



    /**
     * 在BeanFactory设置了所有提供的bean属性（并且满足BeanFactoryAware和ApplicationContextAware）之后，由BeanFactory调用。
     * 此方法允许bean实例仅在设置了所有bean属性时执行初始化，并在配置错误时抛出异常。
     * https://docs.spring.io/spring/docs/1.2.x/javadoc-api/org/springframework/beans/factory/InitializingBean.html
     * */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("factory begin init...... ");
        ConfBaseBeanFactory.init(adminAddress, env, accessToken, mirrorfile);
    }


    /***
     *
     * BeanFactory在销毁单例时调用的
     * */
    @Override
    public void destroy() throws Exception {
        log.info("object is destory ");
        ConfBaseBeanFactory.destroy();
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory=beanFactory;
    }


    // ---------------------- refresh bean with xxl conf  ----------------------

    /**
     * refresh bean with xxl conf (fieldNames)
     */
    public static void refreshBeanField(final BeanRefreshConfListener.BeanField beanField, final String value, Object bean){
        if (bean == null) {
            bean = beanFactory.getBean(beanField.getBeanName());		// 已优化：启动时禁止实用，getBean 会导致Bean提前初始化，风险较大；
        }
        if (bean == null) {
            return;
        }

        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);

        // property descriptor
        PropertyDescriptor propertyDescriptor = null;
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
        if (propertyDescriptors!=null && propertyDescriptors.length>0) {
            for (PropertyDescriptor item: propertyDescriptors) {
                if (beanField.getProperty().equals(item.getName())) {
                    propertyDescriptor = item;
                }
            }
        }

        // refresh field: set or field
        if (propertyDescriptor!=null && propertyDescriptor.getWriteMethod() != null) {
            beanWrapper.setPropertyValue(beanField.getProperty(), value);	// support mult data types
            log.info(">>>>>>>>>>> wywhdgg-conf, refreshBeanField[set] success, {}#{}:{}",
                beanField.getBeanName(), beanField.getProperty(), value);
        } else {

            final Object finalBean = bean;
            ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field fieldItem) throws IllegalArgumentException, IllegalAccessException {
                    if (beanField.getProperty().equals(fieldItem.getName())) {
                        try {
                            Object valueObj = FieldReflectionUtil.parseValue(fieldItem.getType(), value);
                            fieldItem.setAccessible(true);
                            fieldItem.set(finalBean, valueObj);		// support mult data types
                            log.info(">>>>>>>>>>> wywhdgg-conf, refreshBeanField[field] success, {}#{}:{}",
                                beanField.getBeanName(), beanField.getProperty(), value);
                        } catch (IllegalAccessException e) {
                            throw new ConfException(e);
                        }
                    }
                }
            });

			/*Field[] beanFields = bean.getClass().getDeclaredFields();
			if (beanFields!=null && beanFields.length>0) {
				for (Field fieldItem: beanFields) {
					if (beanField.getProperty().equals(fieldItem.getName())) {
						try {
							Object valueObj = FieldReflectionUtil.parseValue(fieldItem.getType(), value);

							fieldItem.setAccessible(true);
							fieldItem.set(bean, valueObj);		// support mult data types

							log.info(">>>>>>>>>>> wywhdgg-conf, refreshBeanField[field] success, {}#{}:{}",
									beanField.getBeanName(), beanField.getProperty(), value);
						} catch (IllegalAccessException e) {
							throw new ConfException(e);
						}
					}
				}
			}*/
        }

    }


    /**
     * register beanDefinition If Not Exists
     *
     * @param registry
     * @param beanClass
     * @param beanName
     * @return
     */
    public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, Class<?> beanClass, String beanName) {

        // default bean name
        if (beanName == null) {
            beanName = beanClass.getName();
        }

        if (registry.containsBeanDefinition(beanName)) {	// avoid beanName repeat
            return false;
        }

        String[] beanNameArr = registry.getBeanDefinitionNames();
        for (String beanNameItem : beanNameArr) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanNameItem);
            if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {	// avoid className repeat
                return false;
            }
        }

        BeanDefinition annotationProcessor = BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition();
        registry.registerBeanDefinition(beanName, annotationProcessor);
        return true;
    }



}
