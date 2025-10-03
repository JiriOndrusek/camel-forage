package org.apache.camel.forage.jdbc;

import org.apache.camel.builder.RouteBuilder;

public class PlatformHttpServer extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("platform-http:/hello?httpMethodRestrict=GET").setBody(simple("Hello2 ${header.name}"));
    }
}
