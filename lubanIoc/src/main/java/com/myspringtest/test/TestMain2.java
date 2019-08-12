package com.myspringtest.test;

import com.myspringtest.app.Appconfig;
import com.myspringtest.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestMain2 {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
		// 如果注册的不是配置类不需要调用refresh方法, 后面getBean会直接把这个类创建出来
		annotationConfigApplicationContext.register(IndexDao.class);
//		annotationConfigApplicationContext.register(Appconfig.class);
//		// refresh()初始化spring的环境
//		annotationConfigApplicationContext.refresh();
		IndexDao indexDao = annotationConfigApplicationContext.getBean(IndexDao.class);
		indexDao.query();
	}

}
