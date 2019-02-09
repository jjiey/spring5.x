package com.myspringtest.factoryBean;

import com.myspringtest.dao.Dao;
import com.myspringtest.invocationHandler.MyInvocationHandler;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class MyFactoryBean implements FactoryBean {

	@Override
	public Object getObject() throws Exception {
		Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Dao.class}, new MyInvocationHandler());
		return proxy;
	}

	@Override
	public Class<?> getObjectType() {
		return Dao.class;
	}

}
