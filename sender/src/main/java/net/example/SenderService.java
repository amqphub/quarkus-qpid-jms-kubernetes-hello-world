package net.example;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.jms.CompletionListener;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
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
public class SenderService implements HealthCheck {

    private static Logger LOG = LoggerFactory.getLogger(SenderService.class);

    @ConfigProperty(name = "connection.uri", defaultValue = "failover:(amqp://localhost:5672)")
    String connectionUri;
    @ConfigProperty(name = "connection.username", defaultValue = "example")
    String username;
    @ConfigProperty(name = "connection.password", defaultValue = "example")
    String password;

    private JMSContext jmsContext;
    private JMSProducer jmsProducer;

    void onStart(@Observes StartupEvent ev) throws Exception {
        LOG.info("JMS Sender starting");

        final ConnectionFactory factory = new JmsConnectionFactory(connectionUri);

        jmsContext = factory.createContext(username, password);
        jmsProducer = jmsContext.createProducer();
    }

    void onStop(@Observes ShutdownEvent ev) throws Exception {
        LOG.debug("Shutting down");
        if (jmsContext != null) {
            jmsContext.close();
        }
    }

    public String send(String string) {
        synchronized (jmsContext) {
            Topic topic = jmsContext.createTopic("example/strings");

            jmsProducer.setAsync(new CompletionListener() {
                @Override
                public void onCompletion(Message message) {
                    try {
                        LOG.info("SENDER: Receiver accepted '{}'", message.getBody(String.class));
                    } catch (JMSException e) {
                        LOG.error("Message access error", e);
                    }
                }

                @Override
                public void onException(Message message, Exception e) {
                    LOG.info("SENDER: Send failed: {}", e.toString());
                }
            });

            jmsProducer.send(topic, string);
        }

        LOG.info("SENDER: Sent message '{}'", string);

        return "OK\n";
    }

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("Health check for Sender Service:").up().build();
    }
}