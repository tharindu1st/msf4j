/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.sample.httpmonitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitoringInterceptor;
import org.wso2.msf4j.analytics.metrics.MetricsInterceptor;
import org.wso2.msf4j.sample.httpmonitoring.service.StudentService;

/**
 * Main Application Class.
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Starting the Microservice with HTTP Monitoring");
        new MicroservicesRunner()
                .addInterceptor(new HTTPMonitoringInterceptor())
                .addInterceptor(new MetricsInterceptor())
                .deploy(new StudentService())
                .start();
    }
}
