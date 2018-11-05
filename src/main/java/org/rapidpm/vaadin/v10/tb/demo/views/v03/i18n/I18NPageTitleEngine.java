package org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n;

import static org.rapidpm.frp.matcher.Case.match;
import static org.rapidpm.frp.matcher.Case.matchCase;
import static org.rapidpm.frp.model.Result.failure;
import static org.rapidpm.frp.model.Result.success;

import java.util.List;
import java.util.Locale;

import org.rapidpm.dependencies.core.logger.HasLogger;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class I18NPageTitleEngine implements VaadinServiceInitListener, UIInitListener, BeforeEnterListener, HasLogger {


  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Class<?> navigationTarget = event.getNavigationTarget();
    I18NPageTitel annotation = navigationTarget.getAnnotation(I18NPageTitel.class);
    match(
        matchCase(() -> success(annotation.messageKey())) ,
        matchCase(() -> annotation == null ,
                  () -> failure("no annotation found at class " + navigationTarget.getName())) ,
        matchCase(() -> annotation.messageKey().isEmpty() ,
                  () -> success(annotation.defaultValue()))
    )
        .ifPresentOrElse(
            msgKey -> {
              final I18NProvider i18NProvider = VaadinService
                  .getCurrent()
                  .getInstantiator()
                  .getOrCreate(VaadinI18NProvider.class);
              final Locale locale = event.getUI().getLocale();
              final List<Locale> providedLocales = i18NProvider.getProvidedLocales();
              match(
                  matchCase(() -> success(providedLocales.get(0))) ,
                  matchCase(() -> locale == null && providedLocales.isEmpty() ,
                            () -> failure("no locale provided and i18nProvider #getProvidedLocales()# list is empty !! " + i18NProvider.getClass().getName())) ,
                  matchCase(() -> locale == null ,
                            () -> success(providedLocales.get(0))) ,
                  matchCase(() -> providedLocales.contains(locale) ,
                            () -> success(locale))
              ).ifPresentOrElse(
                  finalLocale -> UI.getCurrent()
                                   .getPage()
                                   .setTitle(i18NProvider.getTranslation(msgKey , finalLocale)
                                             + " | "
                                             + i18NProvider.getTranslation("global.app.name" , finalLocale)) ,
                  failed -> logger().info(failed));
            }
            , failed -> logger().info(failed));


  }

  @Override
  public void uiInit(UIInitEvent event) {
    final UI ui = event.getUI();
    ui.addBeforeEnterListener(this);
    //addListener(ui, PermissionsChangedEvent.class, e -> ui.getPage().reload());
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event
        .getSource()
        .addUIInitListener(this);
  }
}
