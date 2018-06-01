/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.collector.configuration;

import org.apache.skywalking.apm.collector.core.module.ModuleConfig;

/**
 * @author peng-yongsheng
 */
class ConfigurationModuleConfig extends ModuleConfig {

    private String namespace;
    private int applicationApdexThreshold;
    private double serviceErrorRateThreshold;
    private int serviceAverageResponseTimeThreshold;
    private double instanceErrorRateThreshold;
    private int instanceAverageResponseTimeThreshold;
    private double applicationErrorRateThreshold;
    private int applicationAverageResponseTimeThreshold;
    private int thermodynamicResponseTimeStep;
    private int thermodynamicCountOfResponseTimeSteps;
    private int workerCacheMaxSize;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getApplicationApdexThreshold() {
        return applicationApdexThreshold;
    }

    public void setApplicationApdexThreshold(int applicationApdexThreshold) {
        this.applicationApdexThreshold = applicationApdexThreshold;
    }

    public double getServiceErrorRateThreshold() {
        return serviceErrorRateThreshold;
    }

    public void setServiceErrorRateThreshold(double serviceErrorRateThreshold) {
        this.serviceErrorRateThreshold = serviceErrorRateThreshold;
    }

    public int getServiceAverageResponseTimeThreshold() {
        return serviceAverageResponseTimeThreshold;
    }

    public void setServiceAverageResponseTimeThreshold(int serviceAverageResponseTimeThreshold) {
        this.serviceAverageResponseTimeThreshold = serviceAverageResponseTimeThreshold;
    }

    public double getInstanceErrorRateThreshold() {
        return instanceErrorRateThreshold;
    }

    public void setInstanceErrorRateThreshold(double instanceErrorRateThreshold) {
        this.instanceErrorRateThreshold = instanceErrorRateThreshold;
    }

    public int getInstanceAverageResponseTimeThreshold() {
        return instanceAverageResponseTimeThreshold;
    }

    public void setInstanceAverageResponseTimeThreshold(int instanceAverageResponseTimeThreshold) {
        this.instanceAverageResponseTimeThreshold = instanceAverageResponseTimeThreshold;
    }

    public double getApplicationErrorRateThreshold() {
        return applicationErrorRateThreshold;
    }

    public void setApplicationErrorRateThreshold(double applicationErrorRateThreshold) {
        this.applicationErrorRateThreshold = applicationErrorRateThreshold;
    }

    public int getApplicationAverageResponseTimeThreshold() {
        return applicationAverageResponseTimeThreshold;
    }

    public void setApplicationAverageResponseTimeThreshold(int applicationAverageResponseTimeThreshold) {
        this.applicationAverageResponseTimeThreshold = applicationAverageResponseTimeThreshold;
    }

    public int getThermodynamicResponseTimeStep() {
        return thermodynamicResponseTimeStep;
    }

    public void setThermodynamicResponseTimeStep(int thermodynamicResponseTimeStep) {
        this.thermodynamicResponseTimeStep = thermodynamicResponseTimeStep;
    }

    public int getThermodynamicCountOfResponseTimeSteps() {
        return thermodynamicCountOfResponseTimeSteps;
    }

    public void setThermodynamicCountOfResponseTimeSteps(int thermodynamicCountOfResponseTimeSteps) {
        this.thermodynamicCountOfResponseTimeSteps = thermodynamicCountOfResponseTimeSteps;
    }

    public int getWorkerCacheMaxSize() {
        return workerCacheMaxSize;
    }

    public void setWorkerCacheMaxSize(int workerCacheMaxSize) {
        this.workerCacheMaxSize = workerCacheMaxSize;
    }
}
