package ru.data.DTO.controller.test;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.TestController;

/**
 * RU: Данные клиента используемые TestController в методах
 *
 * @see TestController
 */
@ToString
@RequiredArgsConstructor
public final class TestInnerDTO {
    public final String id;
    public final boolean val;
}
