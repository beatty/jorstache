package com.github.jorstache;

import jornado.Method;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
* @author john
*/
@Retention(RetentionPolicy.RUNTIME)
@interface Page {
  Method method() default Method.GET;
  String url();
  String[] params() default {};
  String root();
  String template();
}
