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

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Topic;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class ReceiverService {

    private static Logger LOG = LoggerFactory.getLogger(ReceiverService.class);

    @ConfigProperty(name = "connection.uri", defaultValue = "failover:(amqp://localhost:5672)")
    String connectionUri;
    @ConfigProperty(name = "connection.username", defaultValue = "example")
    String username;
    @ConfigProperty(name = "connection.password", defaultValue = "example")
    String password;

    private JMSContext jmsContext;
    private ConcurrentLinkedQueue<String> strings = new ConcurrentLinkedQueue<>();

    void onStart(@Observes StartupEvent ev) throws Exception {
        LOG.info("JMS Receiver service starting");

        final ConnectionFactory factory = new JmsConnectionFactory(connectionUri);

        jmsContext = factory.createContext(username, password);

        Topic topic = jmsContext.createTopic("example/strings");
        JMSConsumer consumer = jmsContext.createConsumer(topic);

        consumer.setMessageListener((message) -> {
            String string;

            try {
                string = message.getBody(String.class);
            } catch (JMSException e) {
                LOG.error("Message access error", e);
                return;
            }

            LOG.info("RECEIVER: Received message '{}'", string);

            strings.add(string);
        });
    }

    void onStop(@Observes ShutdownEvent ev) throws Exception {
        LOG.debug("Shutting down");
        if (jmsContext != null) {
            jmsContext.close();
        }
    }

    public String poll() {
        return strings.poll();
    }

    public boolean isReady() {
        return jmsContext != null;
    }
}
