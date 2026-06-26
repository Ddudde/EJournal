package ru.services.interfaces.logic;

import ru.data.DAO.News;
import ru.data.DAO.Syst;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.news.NewsInnerDTO;
import ru.data.DTO.controller.news.NewsOutBodyDTO;
import ru.data.DTO.controller.news.NewsOutDTO;

import java.util.List;
import java.util.Map;

public interface INewsService {
    NewsOutDTO deleteNews(NewsInnerDTO body, String SSEchannel, Syst syst, News news);

    NewsOutDTO changeNews(NewsInnerDTO body, News news);

    NewsOutDTO addNewsYO(NewsInnerDTO body, School school);

    NewsOutDTO addNewsPortal(NewsInnerDTO body, Syst syst);

    Map<Long, NewsOutBodyDTO> prepareNews(List<News> list);
}
