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

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class ReceiverResource {

    private static Logger LOG = LoggerFactory.getLogger(ReceiverResource.class);

    @Inject
    ReceiverService service;

    @GET
    @Path("/receive")
    @Produces("text/plain")
    public String receive() {
        LOG.info("RECEIVER: Receive endpoint invoked");
        return Optional.ofNullable(service.poll()).orElse("");
    }

    @GET
    @Path("/ready")
    @Produces("text/plain")
    public String ready() {
        LOG.info("RECEIVER: I am ready!");
        return "OK\n";
    }
}
