package org.rapidpm.vaadin.v10.tb.demo.views.v01;

import java.util.Locale;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(View001.VIEW_001)
public class View001 extends Composite<Div> {
  public static final String VIEW_001 = "view001";


  /**
   * A bad solution with a few bad practices...
   * <p>
   * Don´t use your own i18n Solution
   * Don´t repeat yourself for every View
   * Don´t mess up the constructor
   * implicit Message formatting via xx + " | " + yy
   */
  public View001() {
    UI current = UI.getCurrent();
    Locale locale = current.getLocale();
    current
        .getPage()
        .setTitle(Messages.get("global.app.name" , locale)
                  + " | "
                  + Messages.get("view.title" , locale));

  }


}
