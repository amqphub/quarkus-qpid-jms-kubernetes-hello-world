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
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class ReceiverService implements HealthCheck {

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

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("Health check for Receiver Service:").up().build();
    }
}
