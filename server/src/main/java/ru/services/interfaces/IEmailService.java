package ru.services.interfaces;

public interface IEmailService {
    @SuppressWarnings("JavadocReference")
    void sendRegCode(String to, String code);

    @SuppressWarnings("JavadocReference")
    void sendRecCode(String to, String code, String title);
}
