package com.github.jorstache;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sampullara.mustache.MustacheException;
import jornado.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author john
 */
public abstract class JorstacheModule<R extends Request> extends JornadoModule<R> {
  public JorstacheModule(Config config) {
    super(config);
  }

  @Override
  protected void configure() {
    super.configure();
    for (Class<? extends PageHandler> cls : pageHandlers()) {
      bind(cls);
    }
  }

  @Override
  protected Iterable<RouteHandler<R>> createRoutes() {
    List<RouteHandler<R>> lst = new LinkedList<RouteHandler<R>>();
    for (Class<? extends PageHandler> cls : pageHandlers()) {
      final Page annotation = cls.getAnnotation(Page.class);
      if (annotation == null) {
        throw new RuntimeException("missing Page annotation: " + cls.getClass());
      }
      final Route route = new RegexRoute(annotation.method(), annotation.url(), annotation.params());
      lst.add(new RouteHandler(route, cls));
    }
    return lst;
  }

  public abstract Iterable<Class<? extends PageHandler>> pageHandlers();
}
