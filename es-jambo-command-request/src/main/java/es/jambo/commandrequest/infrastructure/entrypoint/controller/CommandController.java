package es.jambo.commandrequest.infrastructure.entrypoint.controller;

import es.jambo.commandrequest.utils.CommandSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController()
@RequestMapping("/command")
class CommandController {

    private CommandSender commandSender;

    public CommandController(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @GetMapping("/")
    public String executeCommand() throws ExecutionException, InterruptedException {
        final var exampleMessage = "ping";
        final var responseRecord = commandSender.executeCommand(exampleMessage);
        return responseRecord.toString();
    }
}
