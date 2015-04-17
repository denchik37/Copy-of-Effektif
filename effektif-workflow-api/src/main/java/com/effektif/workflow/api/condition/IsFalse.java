/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.api.condition;

import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * A {@link Condition} based on a boolean value.
 *
 * @author Tom Baeyens
 */
@JsonTypeName("isFalse")
public class IsFalse extends SingleBindingCondition {
  
  @Override
  public String toString() {
    return "("+toString(left)+" is false)";
  }

  @Override
  public IsFalse left(Binding< ? > left) {
    super.left(left);
    return this;
  }
}
