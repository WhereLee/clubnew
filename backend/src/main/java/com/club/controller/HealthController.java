package com.club.controller;

import com.club.common.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public R<Void> health() {
        return R.success();
    }
}
