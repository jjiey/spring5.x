package com.myspringtest.test;

import com.myspringtest.beanFactoryPostProcessor.MyBeanFactoryPostProcessor;
import com.myspringtest.app.Appconfig;
import com.myspringtest.dao.Dao;
import com.myspringtest.dao.IndexDao;
import com.myspringtest.dao.IndexDao2;
import com.myspringtest.dao.IndexDao3;
import com.myspringtest.invocationHandler.MyInvocationHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Proxy;

public class TestMain {

	public static void main(String[] args) {
		Dao dao = (Dao) Proxy.newProxyInstance(TestMain.class.getClassLoader(), new Class[]{Dao.class}, new MyInvocationHandler());
		dao.query();
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.register(Appconfig.class);
		annotationConfigApplicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor());
		annotationConfigApplicationContext.refresh();
		IndexDao indexDao = annotationConfigApplicationContext.getBean(IndexDao.class);
		IndexDao indexDao1 = annotationConfigApplicationContext.getBean(IndexDao.class);
		System.out.println(indexDao.hashCode() + "======" + indexDao1.hashCode());
		indexDao.query();
		IndexDao2 indexDao2 = annotationConfigApplicationContext.getBean(IndexDao2.class); // ImportSelector
		indexDao2.query();
		IndexDao3 indexDao3 = annotationConfigApplicationContext.getBean(IndexDao3.class); // ImportSelector
		indexDao3.query();
	}

}
