package ru.leonaugust.link_aggregator.build_tools.mapstruct;

import java.beans.Introspector;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;

public class CustomAccessorNamingStrategy extends DefaultAccessorNamingStrategy {

  @Override
  public boolean isGetterMethod(ExecutableElement method) {
    if (method.getSimpleName().toString().startsWith("with")) {
      return false;
    }
    return super.isGetterMethod(method);
  }

  @Override
  public boolean isSetterMethod(ExecutableElement method) {
    if (method.getSimpleName().toString().startsWith("with")) {
      return false;
    }
    return super.isSetterMethod(method);
  }
}