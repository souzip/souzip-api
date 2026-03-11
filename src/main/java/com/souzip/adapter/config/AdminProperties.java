package com.souzip.adapter.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AdminProperties {

    @Value("${admin.initial.username}")
    private String username;

    @Value("${admin.initial.password}")
    private String password;
}