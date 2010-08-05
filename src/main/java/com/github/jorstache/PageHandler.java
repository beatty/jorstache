package com.github.jorstache;

import com.sampullara.mustache.MustacheException;
import jornado.NotFoundResponse;
import jornado.RedirectResponse;
import jornado.Request;
import jornado.Response;

/**
* @author john
*/
public abstract class PageHandler<R extends Request> extends MustacheHandler<R> {

  @Override
  public Response handle(R request) {
    try {
      return super.handle(page(request));
    } catch (NotFoundException e) {
      return new NotFoundResponse();
    } catch (RedirectException e) {
      return new RedirectResponse(e.getUrl());
    }
  }


  /**
   * Handle a request, returning either a model object or a Scope object
   * @param request
   * @return
   */
  abstract Object page(R request) throws NotFoundException, RedirectException;
}
