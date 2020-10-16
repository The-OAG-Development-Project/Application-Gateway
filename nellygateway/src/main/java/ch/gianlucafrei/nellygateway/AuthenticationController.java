package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.filters.SimpleFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @GetMapping("/")
    public String index(){
        return "This is Nelly";
    }

    @GetMapping("/login")
    public String login(){
        return "This is Nelly";
    }



}
