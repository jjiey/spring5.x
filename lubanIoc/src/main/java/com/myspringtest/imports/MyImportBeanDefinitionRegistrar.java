package com.myspringtest.imports;

import com.myspringtest.dao.Dao;
import com.myspringtest.invocationHandler.MyInvocationHandler;
import com.myspringtest.test.TestMain;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Proxy;

public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		//Dao dao = (Dao) Proxy.newProxyInstance(TestMain.class.getClassLoader(), new Class[]{Dao.class}, new MyInvocationHandler());
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Dao.class);
		GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
		registry.registerBeanDefinition("IndexDao", beanDefinition);
	}

}
