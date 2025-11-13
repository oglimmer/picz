/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.util.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes exactly one bean.method invocation: - beanName: the Spring bean’s name (or fully
 * qualified class name if you prefer) - methodName: the name of the method to call -
 * parameterTypeNames: fully‐qualified class names for each parameter (in order) - args: array of
 * argument values (must be JSON‐serializable)
 *
 * <p>Example JSON: { "beanName": "myUserService", "methodName": "createUser", "parameterTypeNames":
 * ["java.lang.String","java.lang.Integer"], "args": ["Alice", 25] }
 */
public record MethodInvocationPayload(
    String beanName, String methodName, String[] parameterTypeNames, Object[] args) {

  @JsonCreator
  public MethodInvocationPayload(
      @JsonProperty("beanName") String beanName,
      @JsonProperty("methodName") String methodName,
      @JsonProperty("parameterTypeNames") String[] parameterTypeNames,
      @JsonProperty("args") Object[] args) {
    this.beanName = beanName;
    this.methodName = methodName;
    this.parameterTypeNames = parameterTypeNames != null ? parameterTypeNames : new String[0];
    this.args = args != null ? args : new Object[0];
  }
}
