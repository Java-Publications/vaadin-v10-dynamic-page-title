package org.rapidpm.vaadin.v10.tb.demo.views.v01;


import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.ResourceBundle.getBundle;
import static org.rapidpm.frp.matcher.Case.match;
import static org.rapidpm.frp.matcher.Case.matchCase;
import static org.rapidpm.frp.model.Result.success;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import org.rapidpm.frp.functions.CheckedFunction;

@Deprecated
public class Messages {

  public static final String RESOURCE_BUNDLE_NAME = "vaadinapp";

  private static final ResourceBundle RESOURCE_BUNDLE_DEFAULT = getBundle(RESOURCE_BUNDLE_NAME);
  private static final ResourceBundle RESOURCE_BUNDLE_EN = getBundle(RESOURCE_BUNDLE_NAME , ENGLISH);
  private static final ResourceBundle RESOURCE_BUNDLE_DE = getBundle(RESOURCE_BUNDLE_NAME , GERMAN);

  private static final List<Locale> locales = List.of(ENGLISH ,
                                                      GERMAN);

  public static String get(String key , Locale locale) {
    Objects.requireNonNull(key);
    return match(
        matchCase(() -> success(RESOURCE_BUNDLE_DEFAULT)) ,
        matchCase(() -> locale == null , () -> success(RESOURCE_BUNDLE_DEFAULT)) ,
        matchCase(() -> ENGLISH.getLanguage().equals(locale.getLanguage()) ,
                  () -> success(RESOURCE_BUNDLE_EN)) ,
        matchCase(() -> GERMAN.getLanguage().equals(locale.getLanguage()) ,
                  () -> success(RESOURCE_BUNDLE_DE))
    )
        .flatMap((CheckedFunction<ResourceBundle, String>) resourceBundle -> resourceBundle.getString(key))
        .ifFailed(System.out::println)
        .getOrElse(() -> "##" + key + "##-" + locale);
  }

}
