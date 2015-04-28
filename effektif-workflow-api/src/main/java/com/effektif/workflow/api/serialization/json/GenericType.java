/* Copyright (c) 2014, Effektif GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.effektif.workflow.api.serialization.json;

import java.lang.reflect.Type;


/**
 * A container for type information that is used by JSON deserialisation.
 *
 * @author Tom Baeyens
 */
public class GenericType implements Type {
  
  protected Class<?> baseType;
  protected Type[] typeArgs;
  
  public GenericType(Class< ? > baseType, Type... typeArgs) {
    this.baseType = baseType;
    this.typeArgs = typeArgs;
  }

  public Class<?> getBaseType() {
    return this.baseType;
  }
  
  public Type[] getTypeArgs() {
    return this.typeArgs;
  }
}