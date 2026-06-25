package ru.data.DTO.controller.auth;

import lombok.Builder;

import java.util.Map;

@Builder
public class AuthServiceDTO {
    public final Long kid;
    public final Map<Long, String> kids;
    public final AuthOutDTO bodyAuth;
    public final String token;
    public transient final String cookie;
}
