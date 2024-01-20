package es.jambo.commandrequest.infrastructure.entrypoint.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class WelcomeController {

    @Value("${nodename}")
    private String nodeName;

    @GetMapping("/")
    public String index() {
        return String.format("%s: I'm ready!", nodeName);
    }
}
