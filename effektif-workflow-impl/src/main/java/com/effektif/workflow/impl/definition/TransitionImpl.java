/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.definition;

import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.script.Script;
import com.effektif.workflow.impl.script.ScriptService;


/**
 * @author Walter White
 */
public class TransitionImpl extends BaseImpl {

  public Transition apiTransition;
  public ActivityImpl from;
  public ActivityImpl to;
  public ScopeImpl parent;
  public Script conditionScript;

  public TransitionImpl(Transition apiTransition) {
    super(apiTransition);
    this.apiTransition = apiTransition;
  }
  
  public void validate(WorkflowValidator validator) {
    String fromId = apiTransition.getFrom();
    if (fromId==null) {
      validator.addWarning("Transition has no 'from' specified");
    } else {
      this.from = parent.activities.get(fromId);
      if (this.from!=null) {
        this.from.addOutgoingTransition(this);
      } else {
        validator.addError("Transition has an invalid value for 'from' (%s) : %s", fromId, validator.getExistingActivityIdsText(parent));
      }
    }
    String toId = apiTransition.getTo();
    if (toId==null) {
      validator.addWarning("Transition has no 'to' specified");
    } else {
      this.to = parent.activities.get(toId);
      if (this.to!=null) {
        this.to.addIncomingTransition(this);
      } else {
        validator.addError("Transition has an invalid value for 'to' (%s) : %s", toId, validator.getExistingActivityIdsText(parent));
      }
    }
    if (apiTransition.getCondition()!=null) {
      try {
          this.conditionScript = workflowEngine
            .getServiceRegistry()
            .getService(ScriptService.class)
            .compile(apiTransition.getCondition());
      } catch (Exception e) {
        validator.addError("Transition (%s)--%s>(%s) has an invalid condition expression '%s' : %s", 
                fromId, (id!=null ? id+"--" : ""),
                toId, apiTransition.getCondition(), e.getMessage());
      }
    }
  }
}
