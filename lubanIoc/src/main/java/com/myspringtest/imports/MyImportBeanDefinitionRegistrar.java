package com.myspringtest.imports;

import com.myspringtest.dao.Dao;
import com.myspringtest.factoryBean.MyFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		// 扫描所有接口
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Dao.class);
		GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
		beanDefinition.setBeanClass(MyFactoryBean.class);
		registry.registerBeanDefinition("IndexDao", beanDefinition);
	}

}
