package de.oglimmer.picz.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {

    @Operation(
            summary = "Unknown"
    )
    @GetMapping("/error")
    public void error() {
        System.out.println("????");
    }

}
