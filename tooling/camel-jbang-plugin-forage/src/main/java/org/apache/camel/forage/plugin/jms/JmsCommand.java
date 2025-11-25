package org.apache.camel.forage.plugin.jms;

import org.apache.camel.dsl.jbang.core.commands.CamelCommand;
import org.apache.camel.dsl.jbang.core.commands.CamelJBangMain;
import picocli.CommandLine;

@CommandLine.Command(name = "jms", description = "Camel Forage JMS")
public class JmsCommand extends CamelCommand {

    public JmsCommand(CamelJBangMain main) {
        super(main);
    }

    @Override
    public Integer doCall() throws Exception {
        new CommandLine(this).execute("--help");
        return 0;
    }
}
