package ru.services;

public interface IEmailService {
    @SuppressWarnings("JavadocReference")
    void sendRegCode(String to, String code);

    @SuppressWarnings("JavadocReference")
    void sendRecCode(String to, String code, String title);
}
