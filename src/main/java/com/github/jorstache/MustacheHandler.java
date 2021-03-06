package com.github.jorstache;

import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheCompiler;
import com.sampullara.mustache.MustacheException;
import com.sampullara.mustache.Scope;
import com.sampullara.util.RuntimeJavaCompiler;
import jornado.ErrorResponse;
import jornado.Handler;
import jornado.Request;
import jornado.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;

/**
 * Handle a mustache template request.
 * <p/>
 * User: sam
 * Date: May 16, 2010
 * Time: 5:01:24 PM
 */
public abstract class MustacheHandler<R extends Request> implements Handler<R> {

  private MustacheCompiler mc;
  private Mustache mustache;
  private long timestamp;
  private File template;
  private File code;
  private String path;
  private String codePath;
  private String classname;
  private long lastcheck = System.currentTimeMillis();
  protected Class clazz;
  private long codetimestamp;


  public MustacheHandler() {
    this.path = pathAnnotation();
    final File root = rootAnnotation();
    template = new File(root, path);
    timestamp = template.lastModified();
    mc = new MustacheCompiler(root);
    try {
      mustache = mc.parseFile(this.path);
    } catch (MustacheException e) {
      throw new RuntimeException(e);
    }
  }

  public File rootAnnotation() {
    final Page pageAnnotation = annotation(this, Page.class);
    if (pageAnnotation == null) throw new RuntimeException("missing page annotation: " + this.getClass());
    return new File(pageAnnotation.root());
  }

  public String pathAnnotation() {
    final Page pageAnnotation = annotation(this, Page.class);
    if (pageAnnotation == null) throw new RuntimeException("missing page annotation: " + this.getClass());
    return pageAnnotation.template();
  }

  public MustacheHandler(File root, String path) throws MustacheException {
    this.path = path;
    template = new File(root, path);
    timestamp = template.lastModified();
    mc = new MustacheCompiler(root);
    mustache = mc.parseFile(this.path);
  }

  public MustacheHandler(File root, String templatePath, String codePath, String classname) throws MustacheException {
    this.path = templatePath;
    this.codePath = codePath;
    this.classname = classname;
    template = new File(root, templatePath);
    code = new File(root, codePath);
    timestamp = template.lastModified();
    codetimestamp = code.lastModified();
    mc = new MustacheCompiler(root);
    mc.setSuperclass("com.github.jorstache.Jorstache");
    mustache = mc.parseFile(this.path);
    try {
      clazz = RuntimeJavaCompiler.compile(new PrintWriter(System.out, true), classname, getText(codePath, new BufferedReader(new FileReader(code)))).loadClass(classname);
    } catch (Exception e) {
      e.printStackTrace();
      throw new MustacheException("Failed to read code: " + codePath, e);
    }
  }

  private static String getText(String template, BufferedReader br) {
    StringBuilder text = new StringBuilder();
    String line;
    try {
      while ((line = br.readLine()) != null) {
        text.append(line);
        text.append("\n");
      }
      br.close();
    } catch (IOException e) {
      throw new AssertionError("Failed to read template file: " + template);
    }
    return text.toString();
  }

  public Response handle(Object scope) {
    // Periodically check to see if the file has changed
    if (System.currentTimeMillis() - lastcheck > 10000) {
      if (template.lastModified() != timestamp) {
        lastcheck = timestamp = template.lastModified();
        try {
          mustache = mc.parseFile(path);
        } catch (MustacheException e) {
          // Log an error but continue running
          e.printStackTrace();
        }
      }
    }
    try {
      return new MustacheResponse(mustache, new Scope(scope));
    } catch (MustacheException e) {
      return new ErrorResponse("Failed to execute mustache: " + mustache.getPath());
    }
  }

  public Class<?> getCode() {
    if (code != null && System.currentTimeMillis() - lastcheck > 10000) {
      if (code.lastModified() != codetimestamp) {
        lastcheck = codetimestamp = code.lastModified();
        try {
          clazz = RuntimeJavaCompiler.compile(new PrintWriter(System.out, true), classname, getText(codePath, new BufferedReader(new FileReader(code)))).loadClass(classname);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return clazz;
  }

  /**
   * returns the specified annotation for the given object, regardless of whether the
   * class has been enhaced by guice
   *
   * @param obj
   * @param annotationCls
   * @param <T>
   * @return
   */
  private static <T extends Annotation> T annotation(Object obj, Class<T> annotationCls) {
    T annotation = obj.getClass().getAnnotation(annotationCls);
    if (annotation == null) {
      annotation = (T) loadUnenhancedClass(obj).getAnnotation(annotationCls);
    }
    return annotation;
  }

  private static Class loadUnenhancedClass(Object obj) {
    final String mangledName = obj.getClass().getName();
    final int crapidx = mangledName.indexOf("$$EnhancerByGuice");
    if (crapidx >= 0) {
      final String originalName = mangledName.substring(0, crapidx);
      try {
        return obj.getClass().getClassLoader().loadClass(originalName);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("could not find original class " + originalName + " for mangled class " + mangledName);
      }
    } else {
      return null;
    }
  }
}
