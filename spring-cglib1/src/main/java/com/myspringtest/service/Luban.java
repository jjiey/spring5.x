package com.myspringtest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Luban {
	@Autowired
	IndexService indexService;
}
