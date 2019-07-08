/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.example;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class SenderResource {

    private static Logger LOG = LoggerFactory.getLogger(SenderResource.class);

    @Inject
    SenderService service;

    @POST
    @Path("/send")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String send(String string) {
        return service.send(string);
    }

    @Produces
    @ApplicationScoped
    @Liveness
    HealthCheck isLive() {
        LOG.info("SENDER: Checking liveness status");
        return () -> HealthCheckResponse.named("successful-live").up().build();
    }

    @Produces
    @ApplicationScoped
    @Readiness
    HealthCheck isReady() {
        LOG.info("SENDER: Checking readiness status");
        return () -> {
            if (service.isReady()) {
                return HealthCheckResponse.named("sender-ready").up().build();
            } else {
                return HealthCheckResponse.named("sender-initializing").down().build();
            }
        };
    }
}
