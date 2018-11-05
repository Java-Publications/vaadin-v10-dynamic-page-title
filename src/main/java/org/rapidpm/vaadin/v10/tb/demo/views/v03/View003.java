package org.rapidpm.vaadin.v10.tb.demo.views.v03;

import org.rapidpm.dependencies.core.logger.HasLogger;
import org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n.I18NPageTitel;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(View003.VIEW_003)
@I18NPageTitel(messageKey = "view.title")
public class View003 extends Composite<Div> implements HasLogger {

  public static final String VIEW_003 = "view003";

}
