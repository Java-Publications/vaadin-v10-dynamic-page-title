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
  public View001() {
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
Mostly it is a good aproach to develop a solution for a developer 
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

*  

