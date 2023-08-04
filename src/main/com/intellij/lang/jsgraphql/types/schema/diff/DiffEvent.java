/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.schema.diff;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.TypeKind;
import com.intellij.util.containers.ContainerUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents the events that the {@link SchemaDiff} outputs.
 */
@PublicApi
public class DiffEvent {

  private final DiffLevel level;
  private final DiffCategory category;
  private final TypeKind typeOfType;
  private final String typeName;
  private final String fieldName;
  private final String reasonMsg;
  private final List<String> components;

  DiffEvent(DiffLevel level,
            DiffCategory category,
            String typeName,
            String fieldName,
            TypeKind typeOfType,
            String reasonMsg,
            List<String> components) {
    this.level = level;
    this.category = category;
    this.typeName = typeName;
    this.fieldName = fieldName;
    this.typeOfType = typeOfType;
    this.reasonMsg = reasonMsg;
    this.components = components;
  }

  public String getTypeName() {
    return typeName;
  }

  public TypeKind getTypeKind() {
    return typeOfType;
  }

  public String getReasonMsg() {
    return reasonMsg;
  }

  public DiffLevel getLevel() {
    return level;
  }

  public String getFieldName() {
    return fieldName;
  }

  public DiffCategory getCategory() {
    return category;
  }

  public List<String> getComponents() {
    return new ArrayList<>(components);
  }

  @Override
  public String toString() {
    return "DifferenceEvent{" +
           " reasonMsg='" + reasonMsg + '\'' +
           ", level=" + level +
           ", category=" + category +
           ", typeName='" + typeName + '\'' +
           ", typeKind=" + typeOfType +
           ", fieldName=" + fieldName +
           '}';
  }

  /**
   * @return a Builder of Info level diff events
   * @deprecated use {@link DiffEvent#apiInfo()} instead
   */
  @Deprecated
  public static Builder newInfo() {
    return new Builder().level(DiffLevel.INFO);
  }

  public static Builder apiInfo() {
    return new Builder().level(DiffLevel.INFO);
  }

  public static Builder apiDanger() {
    return new Builder().level(DiffLevel.DANGEROUS);
  }

  public static Builder apiBreakage() {
    return new Builder().level(DiffLevel.BREAKING);
  }


  public static class Builder {

    DiffCategory category;
    DiffLevel level;
    String typeName;
    TypeKind typeOfType;
    String reasonMsg;
    String fieldName;
    final List<String> components = new ArrayList<>();

    public Builder level(DiffLevel level) {
      this.level = level;
      return this;
    }


    public Builder typeName(String typeName) {
      this.typeName = typeName;
      return this;
    }

    public Builder fieldName(String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public Builder typeKind(TypeKind typeOfType) {
      this.typeOfType = typeOfType;
      return this;
    }

    public Builder category(DiffCategory category) {
      this.category = category;
      return this;
    }

    public Builder reasonMsg(String format, Object... args) {
      this.reasonMsg = String.format(format, args);
      return this;
    }

    public Builder components(Object... args) {
      components.addAll(ContainerUtil.map(args, String::valueOf));
      return this;
    }

    public DiffEvent build() {
      return new DiffEvent(level, category, typeName, fieldName, typeOfType, reasonMsg, components);
    }
  }
}
