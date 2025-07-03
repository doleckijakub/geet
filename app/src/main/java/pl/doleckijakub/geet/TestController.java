package pl.doleckijakub.geet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String root() {
        return "Hello, Geet user!";
    }

    @GetMapping("/geet")
    public String geet() {
        return "Geet!";
    }

}