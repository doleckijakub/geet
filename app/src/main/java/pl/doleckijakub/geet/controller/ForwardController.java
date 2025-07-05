package pl.doleckijakub.geet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardController {
    @RequestMapping({
            "/",
            "/{path:^(?!api|.*\\..*$).*$}",
            "/{path:^(?!api|.*\\..*$).*$}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
