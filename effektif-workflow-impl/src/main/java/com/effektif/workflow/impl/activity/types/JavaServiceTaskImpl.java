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
package com.effektif.workflow.impl.activity.types;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/** TODO
 * A service task implemented in Java.
 *
 * BPMN XML: {@code <serviceTask id="sendMail" effektif:type="java">}
 *
 * @author Tom Baeyens
 */
public class JavaServiceTaskImpl extends AbstractActivityType<JavaServiceTask> {
  
  Method staticMethod;
  BindingImpl[] argBindings;

  public JavaServiceTaskImpl() {
    super(JavaServiceTask.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, JavaServiceTask activity, WorkflowParser parser) {
    super.parse(activityImpl, activity, parser);
    List<BindingImpl<?>> argBindingsList = parser.parseBindings((List)activity.getArgBindings(), "argBindings");
    if (argBindingsList!=null) {
      this.argBindings = argBindingsList.toArray(new BindingImpl[argBindingsList.size()]);
    }
    
    
    // TODO add parse warnings if not exactly 1 is specified of : beanName or clazz
    // TODO add parse warnings if no methodName is specified
    
    if (activity.getJavaClass()!=null && activity.getMethodName()!=null) {
      for (Method method: activity.getJavaClass().getDeclaredMethods()) {
        if (method.getName().equals(activity.getMethodName())
             && Modifier.isStatic(method.getModifiers())) {
          staticMethod = method;
        }
      }
    }
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    Object[] args = null;
    Object bean = null;
    Method method = null;
    
    if (argBindings!=null) {
      args = new Object[argBindings.length];
      for (int i=0; i<argBindings.length; i++) {
        args[i] = activityInstance.getValue(argBindings[i]);
      }
    }
    
    if (staticMethod!=null) {
      method = staticMethod;
    } else if (bean!=null) {
      // method = Reflection.findMethod(bean.getClass(), activity.getMethodName(), args);
    }
    
    try {
      method.invoke(bean, args);
    } catch (Exception e) {
      // TODO handle the exception
    }
  }
}
