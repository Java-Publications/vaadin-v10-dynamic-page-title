package org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.ResourceBundle.getBundle;
import static org.rapidpm.frp.matcher.Case.match;
import static org.rapidpm.frp.matcher.Case.matchCase;
import static org.rapidpm.frp.model.Result.success;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.rapidpm.dependencies.core.logger.HasLogger;
import com.vaadin.flow.i18n.I18NProvider;

public class VaadinI18NProvider implements I18NProvider, HasLogger {

  public VaadinI18NProvider() {
    logger().info("VaadinI18NProvider was found..");
  }

  public static final String RESOURCE_BUNDLE_NAME = "vaadinapp";

  private static final ResourceBundle RESOURCE_BUNDLE_EN = getBundle(RESOURCE_BUNDLE_NAME , ENGLISH);
  private static final ResourceBundle RESOURCE_BUNDLE_DE = getBundle(RESOURCE_BUNDLE_NAME , GERMAN);


  @Override
  public List<Locale> getProvidedLocales() {
    logger().info("VaadinI18NProvider getProvidedLocales..");
    return List.of(ENGLISH ,
                   GERMAN);
  }

  @Override
  public String getTranslation(String key , Locale locale , Object... params) {
//    logger().info("VaadinI18NProvider getTranslation.. key : " + key + " - " + locale);
    return match(
        matchCase(() -> success(RESOURCE_BUNDLE_EN)) ,
        matchCase(() -> GERMAN.equals(locale) , () -> success(RESOURCE_BUNDLE_DE)) ,
        matchCase(() -> ENGLISH.equals(locale) , () -> success(RESOURCE_BUNDLE_EN))
    )
        .map(resourceBundle -> {
          if (! resourceBundle.containsKey(key))
            logger().info("missing resource key (i18n) " + key);

          return (resourceBundle.containsKey(key)) ? resourceBundle.getString(key) : key;

        })
        .getOrElse(() -> key + " - " + locale);
  }

}
