package org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n;

import java.util.Locale;

import org.rapidpm.frp.functions.CheckedTriFunction;
import com.vaadin.flow.i18n.I18NProvider;

public interface TitleFormatter extends CheckedTriFunction<I18NProvider, Locale, String, String> {
}
