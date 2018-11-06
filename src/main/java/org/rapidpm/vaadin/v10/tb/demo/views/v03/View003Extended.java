package org.rapidpm.vaadin.v10.tb.demo.views.v03;

import java.util.Locale;

import org.rapidpm.dependencies.core.logger.HasLogger;
import org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n.I18NPageTitle;
import org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n.TitleFormatter;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.Route;

@Route(View003Extended.VIEW_003)
@I18NPageTitle(messageKey = "view.title", formatter = View003Extended.PersonalFormatter.class)
public class View003Extended extends Composite<Div> implements HasLogger {


  public static class PersonalFormatter implements TitleFormatter {
    @Override
    public String applyWithException(I18NProvider i18NProvider , Locale locale , String s) throws Exception {
      return "Stupid Implementation .. ";
    }
  }


  public static final String VIEW_003 = "view003extended";

}
