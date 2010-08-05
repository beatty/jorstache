package com.github.jorstache;

/**
* @author john
*/
class RedirectException extends RuntimeException {
  private final String url;

  RedirectException(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
