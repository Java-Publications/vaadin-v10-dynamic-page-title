package org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n;

import java.util.Locale;

import com.vaadin.flow.i18n.I18NProvider;

public class DefaultTitleFormatter implements TitleFormatter {

  @Override
  public String applyWithException(I18NProvider i18NProvider , Locale locale , String key) throws Exception {
    return i18NProvider.getTranslation(key, locale);
  }
}
