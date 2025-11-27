package org.apache.camel.quarkus.messaging.jms;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

@ApplicationScoped
public class JmsRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {


        from("jms:queue:input.queue")
//        from("jms:queue:input.queue?transactionManager=#jtaTransactionManager")
                .routeId("xaConsumer")
                .log("Received message ${body}");

        from("timer:java?period=1000")
                .routeId("xa")
                .transacted()
                .to("jms:queue:input.queue?disableReplyTo=true")
                .log("Message added: ${body}");
    }

}