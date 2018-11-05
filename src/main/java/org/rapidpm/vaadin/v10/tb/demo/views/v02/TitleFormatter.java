package org.rapidpm.vaadin.v10.tb.demo.views.v02;

import java.util.Locale;

import org.rapidpm.vaadin.v10.tb.demo.views.v01.Messages;

public class TitleFormatter {
  public String format(String key, Locale locale){
    return Messages.get("global.app.name" , locale)
           + " | "
           + Messages.get(key , locale);
  }
}
