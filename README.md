<center>
<a href="https://vaadin.com">
 <img src="https://vaadin.com/images/hero-reindeer.svg" width="200" height="200" /></a>
</center>

# Vaadin V10 app with 18N Page Title, dynamically created
What I want to show in this example is, how you could deal with 
a dynamic page title per view (or time or what ever)
that will handle your browser local as well.


## Solution 01 - the worst case
Quite often I can see code like this one you will find in solution 01.
The solution in Version 01 is based on a Message-Holder that 
will load all Ressource-Bundles and it is the static entry point 
into the **message key to message** world.

The **holder** is responsible to load and hold the resource bundles.

```java
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
```

So far so good. You can implement this in different ways. The only thing you should have in mind is,
that your solution is able to handle more than one Thread at the same time.
Even during the initialisation phase. Sometimes I see solutions with a critical init-phase.
For example, that a public constructor will set a static attribute. If you want to build a Singleton, 
build a thread save version. Having in mind, that this will be used heavily, 
the call to get a translation should **not** be blocking.

### setting the Title
In this solution the title is set inside a constructor of a view.

```java
  public class View001() {
    UI current = UI.getCurrent();
    Locale locale = current.getLocale();
    current
        .getPage()
        .setTitle(Messages.get("global.app.name" , locale) 
                  + " | " 
                  + Messages.get("view001.title" , locale));
  }
````

Here are some points I would like to discuss.
The first is based on the assumption that your application is not only based on one view.
Are you sure you want to write this peace of code a few times? And how often your colleges 
in your team will forget this?
The next is, that here is an implicit definition of a format for the page title.
**xx + " | " + yy** 

We have to extract the repeating part..  This could lead to version 02...

## Solution 02 - A - the first quality increase

Noew we are extracting the formatting part, first. For this a class with the name **cc**
is created. Inside you can define the way, how the page title will be formatted.

```java
public class TitleFormatter {
  public String format(String key, Locale locale){
    return Messages.get("global.app.name" , locale)
           + " | "
           + Messages.get(key , locale);
  }
}
``` 

The goal that is achieved is the central place for the way, how to format the page title.
The next step is, to remove the invocation out of the constructor.
Vaadin provides an interface called **HasDynamicTitle**. implementing this, 
you can overwrite the method **getPageTitle()**. But again, you would implement this 
in every view...

```java 
@Route(View002.VIEW_002)
public class View002 extends Composite<Div> implements HasDynamicTitle {
  public static final String VIEW_002 = "view002";

  @Override
  public String getPageTitle() {
    UI current = UI.getCurrent();
    Locale locale = current.getLocale();
    return new TitleFormatter().format("view.title", locale);
  }
}
```  

One solution could be based on inheritance, packing this stuff in a parent class.
But how to get the actual **key** to resolve without implementing something in every child class?

## Solution 02 - B - the first quality increase
Vaadin will give you one other way to define the page title. The other solution is based on 
an annotation called **PageTitle**

```java
@PageTitle("My PapeTitle")
@Route(View002.VIEW_002)
public class View002 extends Composite<Div> {
  public static final String VIEW_002 = "view002";

}
```

The usage of this Annotation is XOR to the usage of the interface **HasDynamicTitle**.
So, make sure that there is nothing in your inheritance.

The challenge here is based on the fact, thet the annotation ony consumes static Strings.
I18N is not possible with this solution.

## Solution 03 - my favourite solution ;-)
After playing around with this solutions, I developed a 
a solution that could handle

* message bundles
* is not inside inheritence
* is based on Annotations
* is easy to extend
* can change the language during runtime

### The developer / user view
Mostly it is a good approach to develop a solution for a developer 
from the perspective of a developer.
Here it means, what should a developer see if he/she have to use your solution.

The developer will see this Annotation.
Here three things can be defined. 

* The message key that will be used to resolve the message based on the actual Locale
* A default value the will be used, if no corresponding resource key was found neither fallback language is provided 
* Definition of the message formatter, default Formatter will only return the translated key.


```java
@Retention(RetentionPolicy.RUNTIME)
public @interface I18NPageTitel {
  String messageKey() default "";
  String defaultValue() default "";
  Class< ? extends TitleFormatter> formatter() default DefaultTitleFormatter.class;
}
```

The default usage should look like the following one.

```java
@Route(View003.VIEW_003)
@I18NPageTitel(messageKey = "view.title")
public class View003 extends Composite<Div> implements HasLogger {
  public static final String VIEW_003 = "view003";
}
```

Now we need a way to resolve the final message and the right point in time to set the title.
Here we could use the following interfaces.

* VaadinServiceInitListener, 
* UIInitListener, 
* BeforeEnterListener

With this interfaces we are able to hook into the life cycle of a view. At this time slots
we have all information's we need. 
The Annotation to get the message key and the locale of the current request.

The class that is implementing all these interfaces is called **I18NPageTitleEngine**

```java
public class I18NPageTitleEngine 
        implements VaadinServiceInitListener, UIInitListener, BeforeEnterListener, HasLogger {


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
```
The method with the name **beforeEnter** is the important part. Here you can see how the key is resolved.
But there is one new thing...  let´s have a look ot the following lines.

```java
              final I18NProvider i18NProvider = VaadinService
                  .getCurrent()
                  .getInstantiator()
                  .getOrCreate(VaadinI18NProvider.class);
```

This few lines are introducing a new thing, that is available in Vaadin 10.
The interface **I18NProvider** is used to implement a mechanism for the internationalization 
of Vaadin applications.

The interface is simple and with onle two methods to implement.

```java
public interface I18NProvider extends Serializable {
    List<Locale> getProvidedLocales();
    String getTranslation(String key, Locale locale, Object... params);
}
```

The first one should give back the list of Locales that could be handled from this implementation.
The second method is used to translate the message key itself. 
In this method the handling of a default translation or better the switch into a default language 
should be handled. Missing keys can be handled differently. Some developers are throwing 
an exception, but I prefer to return the key itself, 
together with the locale from the original request. This information is mostly better to use as a stacktrace.


The solution that is bundled with this demo is able to handle the Locales EN ad DE, fallback will be the locale EN.
The implementation is not dealing with reloads of message bundles during runtime or other 
features that are needed for professional environments.

```java
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
            logger().info("missing ressource key (i18n) " + key);

          return (resourceBundle.containsKey(key)) ? resourceBundle.getString(key) : key;

        })
        .getOrElse(() -> key + " - " + locale);
  }
}
```
The Interface **I18NProvider** is implemented for example by the abstract class **Component**.
Having this in mind, we are now using the same 
mechanism for the page title as well as inside a Component. 

The last thing you should not forget is the activation of the **I18NProvider** implementation itself.
There are several ways you can use, I am using a simple approach inside the main method that will start
my app itself.

```setProperty("vaadin.i18n.provider", VaadinI18NProvider.class.getName());```



```java
public class BasicTestUIRunner {
  private BasicTestUIRunner() {
  }

  public static void main(String[] args) {
    setProperty("vaadin.i18n.provider", VaadinI18NProvider.class.getName());
    
    new Meecrowave(new Meecrowave.Builder() {
      {
//        randomHttpPort();
        setHttpPort(8080);
        setTomcatScanning(true);
        setTomcatAutoSetup(false);
        setHttp2(true);
      }
    })
        .bake()
        .await();
  }
}
```

The Vaadin documentation will give you more detailed information´s about this.

Last step for today, is the activation of our **I18NPageTitleEngine**
This is done inside the file with the name **com.vaadin.flow.server.VaadinServiceInitListener**
you have to create inside the folder  **META-INF/services** 
The only line we have to add is the full qualified name of our class.

```
org.rapidpm.vaadin.v10.tb.demo.views.v03.i18n.I18NPageTitleEngine
```

If you have questions or something to discuss..  ping me via
email [mailto::sven.ruppert@gmail.com](sven.ruppert@gmail.com)
or via Twitter : [https://twitter.com/SvenRuppert](@SvenRuppert)





