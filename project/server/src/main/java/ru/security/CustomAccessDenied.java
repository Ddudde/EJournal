package ru.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service(value = "code401")
public class CustomAccessDenied {

    public boolean check(boolean permitted) {
        if (!permitted){
            throw new AccessDeniedException("is not Authenticated");
        }
        return true;
    }
}
