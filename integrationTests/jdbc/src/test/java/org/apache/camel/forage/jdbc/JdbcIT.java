package org.apache.camel.forage.jdbc;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

import org.citrusframework.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@CitrusSupport
public class JdbcIT implements TestActionSupport {

    @CitrusResource
    TestCaseRunner t;

    @Disabled
    @Test
    @CitrusTest
    public void testCamelRoute() {
        t.given(createVariables().variable("username", "Christoph"));

        t.given(camel().jbang()
                .run()
                .integrationName("platform-http-server")
                .integration(Resources.create("PlatformHttpServer.java")));

        t.when(http().client("http://localhost:8080").send().get("/hello").queryParam("name", "${username}"));

        t.then(http().client("http://localhost:8080")
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("Hello ${username}"));
    }

    @Test
    @CitrusTest(name = "CamelRoute_01_IT")
    public void camelRoute01IT() {
        t.given(camel().jbang()
                .run()
                .integrationName("platform-http-server")
                .integration(Resources.create("PlatformHttpServer.java")));
        System.out.println("server started");

        t.when(send("inRouteEndpoint")
                .fork(true)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));

        t.then(receive("defaultRouteEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));
    }
}
