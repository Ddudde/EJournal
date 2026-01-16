package ru.security.user;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Roles {
    ANONYMOUS(-1), KID(0), PARENT(1), TEACHER(2),
        HTEACHER(3), ADMIN(4);

    public final int i;

    /** RU: даёт роль по переменной i, +1, чтобы не учитывать Анонима*/
    public static Roles roleByI(int i) {
        return Roles.values()[i+1];
    }
}
