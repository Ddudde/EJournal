package ru.data.DTO.controller;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.NewsController;

/**
 * RU: Данные клиента используемые NewsController в методах
 *
 * @see NewsController
 */
@ToString
@RequiredArgsConstructor
public final class NewsInnerDTO {
    public final String type, title, date, img_url, text, val;
    public final Long id;
}
