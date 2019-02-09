package com.myspringtest.test;

import com.myspringtest.beanFactoryPostProcessor.MyBeanFactoryPostProcessor;
import com.myspringtest.app.Appconfig;
import com.myspringtest.dao.IndexDao;
import com.myspringtest.dao.IndexDao2;
import com.myspringtest.dao.IndexDao3;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestMain {

	public static void main(String[] args) {
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
		IndexDao3 indexDao3 = annotationConfigApplicationContext.getBean(IndexDao3.class); //
		indexDao3.query();
	}

}
