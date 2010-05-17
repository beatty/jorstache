package com.github.jorstache;

import jornado.Body;
import jornado.MediaType;
import com.sampullara.util.FutureWriter;

/**
* TODO: Edit this
* <p/>
* User: sam
* Date: May 16, 2010
* Time: 2:13:47 PM
*/
public class MustacheBody implements Body {
  private FutureWriter fw;

  public MustacheBody(FutureWriter fw) {
    this.fw = fw;
  }

  @Override
  public Class getRenderServiceClass() {
    return MustacheRenderer.class;
  }

  @Override
  public MediaType getMediaType() {
    return MediaType.TEXT_HTML_UTF8;
  }

  public FutureWriter getFutureWriter() {
    return fw;
  }
}
