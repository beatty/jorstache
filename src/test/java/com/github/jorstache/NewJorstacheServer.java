package com.github.jorstache;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jornado.*;
import org.joptparse.OptionParser;

/**
 * Example Jorstache Server that uses new PageHandler
 * <p/>
 * User: sam
 * Date: May 16, 2010
 * Time: 1:55:42 PM
 */
public class NewJorstacheServer {
  private final Module module;
  public static Injector injector;

  /**
   * Our jornado guice module
   */
  static class Module extends JorstacheModule {
    protected Module(final Config config) {
      super(config);
    }

    @Override
    public Iterable<Class<? extends PageHandler>> pageHandlers() {
      return Lists.newArrayList(HomeHandler.class, PersonHandler.class);
    }

    @Override
    protected void configure() {
      super.configure();
      bind(UserService.class).to(JorstacheUserService.class);
      bind(RequestFactory.class).to(JorstacheRequestFactory.class);
    }
  }

  public NewJorstacheServer(final Config config) {
    module = new Module(config);
  }

  public static void main(String[] args) throws Exception {
    final Config config = createConfig(args);
    NewJorstacheServer app = new NewJorstacheServer(config);
    injector = Guice.createInjector(app.module); // initialize the object tree with Guice
    injector.getInstance(JettyService.class).startAndWait(); // get the jetty service and start it
  }

  private static Config createConfig(String[] args) {
    final Config config = new Config();
    final OptionParser parser = new OptionParser(config);
    parser.parse(args);
    return config;
  }

  @Page(url="/", root="/tmp", template = "index.html")
  static class HomeHandler extends PageHandler {
    public Object page(final Request request) throws NotFoundException, RedirectException {
      return new Object() {
        String url = request.getReconstructedUrl();
      };
    }
  }

  @Page(url="/person/([A-Za-z0-9]+)", params={"name"}, root="/tmp", template = "index.html")
  static class PersonHandler extends PageHandler {
    public Object page(final Request request) throws NotFoundException, RedirectException {
      return new Object() {
        String url = request.getReconstructedUrl();
      };
    }
  }

  static class JorstacheUserService implements UserService<JorstacheUser> {
    public JorstacheUser load(String id) {
      return new JorstacheUser(id);
    }
  }
}
