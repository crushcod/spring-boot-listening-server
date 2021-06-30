package com.crushoncod.listeningserver.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthResource {

    @PostMapping("")
    public ResponseEntity<String> healthCheck() {
        log.info("Health Check ok");
        return ResponseEntity.ok("ok");
    }
    @GetMapping("/not-ok")
    public ResponseEntity<String> notOk() throws Exception {
        log.info("Example not ok");
        throw new Exception("example not ok");
    }
}
