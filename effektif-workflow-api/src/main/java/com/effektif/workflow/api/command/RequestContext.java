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
package com.effektif.workflow.api.command;

import java.util.Map;


public class RequestContext {
  
  protected static ThreadLocal<RequestContext> current = new ThreadLocal<>();

  protected String autheticatedUserId;
  protected String organizationId;
  protected Map<Object,Object> userDefinedContext;

  public static void current(RequestContext requestContext) {
    current.set(requestContext);
  }

  public static RequestContext authenticate(String authenticatedUserId, String organizationId) {
    RequestContext requestContext = new RequestContext()
      .authenticatedUserId(authenticatedUserId)
      .organizationId(organizationId);
    current(requestContext);
    return requestContext;
  }
  
  public static RequestContext current() {
    return current.get();
  }

  public String getAuthenticatedUserId() {
    return this.autheticatedUserId;
  }
  public void setAutheticatedUserId(String authenticatedUserId) {
    this.autheticatedUserId = authenticatedUserId;
  }
  public RequestContext authenticatedUserId(String authenticatedUserId) {
    this.autheticatedUserId = authenticatedUserId;
    return this;
  }
  
  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public RequestContext organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public RequestContext set(Object key, Object value) {
    userDefinedContext.put(key, value);
    return this;
  }

  public RequestContext set(Object value) {
    if (value!=null) {
      set(value.getClass(), value);
    }
    return this;
  }
  
  protected void set(Class<?> type, Object value) {
    Class< ? > clazz = value.getClass();
    userDefinedContext.put(clazz, value);
    for (Class< ? > interf: clazz.getInterfaces()) {
      set(interf, value);
    }
    Class< ? > superclass = clazz.getSuperclass();
    if (superclass!=Object.class) {
      set(superclass, value);
    }
  }

  public Object get(Object key) {
    return userDefinedContext!=null ? userDefinedContext.get(key) : null;
  }

  public <T> T get(Class<T> type) {
    return userDefinedContext!=null ? (T) userDefinedContext.get(type) : null;
  }

  public static boolean hasOrganizationId(RequestContext requestContext) {
    return requestContext!=null ? requestContext.organizationId!=null : false;
  }
}