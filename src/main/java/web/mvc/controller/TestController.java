package web.mvc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))  //
                .body("CI/CD GitHub Action - spring Security OK 시작! - react와");
    }
}
