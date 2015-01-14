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
package com.effektif.workflow.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.CloseAction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.command.MessageCommand;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.ScopeInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.task.Task;
import com.effektif.workflow.impl.task.TaskQuery;
import com.effektif.workflow.impl.task.TaskService;


/** Base class that allows to reuse tests and run them on different process engines. */
public class WorkflowTest {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowTest.class);
  
  protected static WorkflowEngine cachedDefaultWorkflowEngine = null;
  
  protected WorkflowEngine workflowEngine = null;
  
  @Before
  public void initializeWorkflowEngine() {
    if (cachedDefaultWorkflowEngine==null) {
      cachedDefaultWorkflowEngine = new TestWorkflowEngineConfiguration()
        .buildWorkflowEngine(); 
    }
    workflowEngine = cachedDefaultWorkflowEngine;
  }
  
  @After
  public void after() {
    logWorkflowEngineContents();
    deleteWorkflowEngineContents();
  }

  public Workflow deploy(Workflow workflow) {
    return workflowEngine.deployWorkflow(workflow);
  }

  public WorkflowInstance start(Workflow workflow) {
    return workflowEngine.startWorkflowInstance(new StartCommand()
      .workflowId(workflow.getId()));
  }
  
  public WorkflowInstance sendMessage(WorkflowInstance workflowInstance, String activityInstanceId) {
    return workflowEngine.sendMessage(new MessageCommand()
      .workflowInstanceId(workflowInstance.getId())
      .activityInstanceId(activityInstanceId));
  }
  
  public static void assertOpen(WorkflowInstance workflowInstance, String... expectedActivityNames) {
    Map<String,Integer> expectedActivityCounts = new HashMap<String, Integer>();
    if (expectedActivityNames!=null) {
      for (String expectedActivityName: expectedActivityNames) {
        Integer count = expectedActivityCounts.get(expectedActivityName);
        expectedActivityCounts.put(expectedActivityName, count!=null ? count+1 : 1);
      }
    }
    Map<String,Integer> activityCounts = new HashMap<String, Integer>();
    scanActivityCounts(workflowInstance, activityCounts);
    assertEquals(expectedActivityCounts, activityCounts);
  }
  
  static void scanActivityCounts(ScopeInstance scopeInstance, Map<String, Integer> activityCounts) {
    List< ? extends ActivityInstance> activityInstances = scopeInstance.getActivityInstances();
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance : activityInstances) {
        if (!activityInstance.isEnded()) {
          Object activityId = activityInstance.getActivityId();
          Integer count = activityCounts.get(activityId);
          activityCounts.put(activityId.toString(), count != null ? count + 1 : 1);
          scanActivityCounts(activityInstance, activityCounts);
        }
      }
    }
  }
  
  public static String getActivityInstanceId(WorkflowInstance workflowInstance, String activityId) {
    ActivityInstance activityInstance = workflowInstance.findOpenActivityInstance(activityId);
    Assert.assertNotNull("No open activity instance found "+activityId+" not found", activityInstance);
    return activityInstance.getId();
  }
  
  public WorkflowInstance endTask(WorkflowInstance workflowInstance, String activityId) {
    ActivityInstance activityInstance = workflowInstance.findOpenActivityInstance(activityId);
    assertNotNull("Activity '"+activityId+"' not in workflow instance", activityInstance);
    return workflowEngine.sendMessage(new MessageCommand()
      .workflowInstanceId(workflowInstance.getId())
      .activityInstanceId(activityInstance.getId()));
  }

  protected void logWorkflowEngineContents() {
    log.debug("\n\n###### Test ended, logging workflow engine contents ######################################################## \n");
    
    WorkflowEngineImpl workflowEngineImpl = (WorkflowEngineImpl) workflowEngine;
    JsonService jsonService = workflowEngineImpl.getServiceRegistry().getService(JsonService.class);
    TaskService taskService = workflowEngineImpl.getServiceRegistry().getService(TaskService.class);

    StringBuilder cleanLog = new StringBuilder();
    cleanLog.append("Workflow engine contents\n");
    
//    List<Job> jobs = jobService.newJobQuery().asList();
//    if (jobs != null && !jobs.isEmpty()) {
//      int i = 0;
//      cleanLog.append("\n### jobs ######################################################## \n");
//      for (Job job : jobs) {
//        jobService.deleteJob(job.getId());
//        cleanLog.append("--- Deleted job ");
//        cleanLog.append(i);
//        cleanLog.append(" ---\n");
//        cleanLog.append(jsonService.objectToJsonStringPretty(job));
//        cleanLog.append("\n");
//        i++;
//      }
//    }

    List<Task> tasks = taskService.findTasks(new TaskQuery());
    if (tasks != null && !tasks.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### tasks ######################################################## \n");
      for (Task task : tasks) {
        cleanLog.append("--- Deleted task ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(task));
        cleanLog.append("\n");
        i++;
      }
    }

    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery());
    if (workflowInstances != null && !workflowInstances.isEmpty()) {
      int i = 0;
      cleanLog.append("\n\n### workflowInstances ################################################ \n");
      for (WorkflowInstance workflowInstance : workflowInstances) {
        cleanLog.append("--- Deleted workflow instance ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(workflowInstance));
        cleanLog.append("\n");
        i++;
      }
    }
    List< ? extends Workflow> workflows = workflowEngine.findWorkflows(new WorkflowQuery());
    if (workflows != null && !workflows.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### workflows ######################################################## \n");
      for (Workflow workflow : workflows) {
        cleanLog.append("--- Deleted workflow ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(workflow));
        cleanLog.append("\n");
        i++;
      }
    }
    log.debug(cleanLog.toString());
  }
  
  protected void deleteWorkflowEngineContents() {
    workflowEngine.deleteWorkflows(new WorkflowQuery());
    workflowEngine.deleteWorkflowInstances(new WorkflowInstanceQuery());
    
    WorkflowEngineImpl workflowEngineImpl = (WorkflowEngineImpl) workflowEngine;
    TaskService taskService = workflowEngineImpl.getServiceRegistry().getService(TaskService.class);
    taskService.deleteTask(new TaskQuery());
  }
}