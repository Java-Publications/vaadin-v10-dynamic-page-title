package org.rapidpm.vaadin.v10.tb.demo.views.v02;

import java.util.Locale;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(View002.VIEW_002)
//@PageTitle("My PapeTitle")
public class View002 extends Composite<Div> implements HasDynamicTitle {
  public static final String VIEW_002 = "view002";

  @Override
  public String getPageTitle() {
    UI current = UI.getCurrent();
    Locale locale = current.getLocale();
    return new TitleFormatter().format("view.title", locale);
  }
}
