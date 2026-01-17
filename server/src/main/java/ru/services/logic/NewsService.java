package ru.services.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.controllers.NewsController;
import ru.data.DAO.News;
import ru.data.DAO.Syst;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.news.NewsInnerDTO;
import ru.data.DTO.controller.news.NewsOutBodyDTO;
import ru.data.DTO.controller.news.NewsOutDTO;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.school.SchoolRepository;
import ru.services.interfaces.IPushService;
import ru.services.interfaces.logic.INewsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** RU: сервис для контроллера
 * @see NewsController */
@Service
@RequiredArgsConstructor
public class NewsService implements INewsService {
    private final SystRepository systRepository;
    private final NewsRepository newsRepository;
    private final SchoolRepository schoolRepository;
    private final IPushService pushService;

    @Override
    public NewsOutDTO deleteNews(NewsInnerDTO body, SubscriberDTO sub, Syst syst, News news) {
        final NewsOutDTO.NewsOutDTOBuilder dtoBuilder = NewsOutDTO.builder();

        if (Objects.equals(sub.getLvlMore2(), "Por") && syst != null && !ObjectUtils.isEmpty(syst.getNews())) {
            syst.getNews().remove(news);
            systRepository.saveAndFlush(syst);
        }
        newsRepository.delete(news);
        dtoBuilder.id(body.id);
        return dtoBuilder.build();
    }

    @Override
    public NewsOutDTO changeNews(NewsInnerDTO body, News news) {
        final NewsOutDTO.NewsOutDTOBuilder dtoBuilder = NewsOutDTO.builder();

        switch (body.type) {
            case "title" -> news.setTitle(body.val);
            case "date" -> news.setDate(body.val);
            case "img_url" -> news.setImg_url(body.val);
            case "text" -> news.setText(body.val);
            default -> {}
        }
        newsRepository.saveAndFlush(news);
        dtoBuilder.id(body.id)
            .type(body.type)
            .val(body.val);
        return dtoBuilder.build();
    }

    @Override
    public NewsOutDTO addNewsYO(NewsInnerDTO body, School school) {
        final News news = newsRepository.saveAndFlush(new News(body.title, body.date, body.img_url, body.text));
        final NewsOutDTO.NewsOutDTOBuilder dtoBuilder = NewsOutDTO.builder();

        school.getNews().add(news);
        schoolRepository.saveAndFlush(school);
        dtoBuilder.body(writeNews(news))
            .id(news.getId());
        pushService.send(school.getId()+"News", "Новые объявления!",
            "В вашей школе новое объявление!\nУведомления можно регулировать на странице 'Настройки'",
            "/DipvLom/static/media/info.jpg");
        return dtoBuilder.build();
    }

    private NewsOutBodyDTO writeNews(News news) {
        final NewsOutBodyDTO.NewsOutBodyDTOBuilder dtoBuilder = NewsOutBodyDTO.builder();

        dtoBuilder.title(news.getTitle())
            .date(news.getDate())
            .img_url(news.getImg_url())
            .text(news.getText());
        return dtoBuilder.build();
    }

    @Override
    public NewsOutDTO addNewsPortal(NewsInnerDTO body, Syst syst) {
        final NewsOutDTO.NewsOutDTOBuilder dtoBuilder = NewsOutDTO.builder();
        final News news = newsRepository.saveAndFlush(new News(body.title, body.date, body.img_url, body.text));

        syst.getNews().add(news);
        systRepository.saveAndFlush(syst);
        dtoBuilder.body(writeNews(news))
            .id(news.getId());
        pushService.send("news", "Новые объявления!",
            "На портале появилось новое объявление!\nУведомления можно регулировать на странице 'Настройки'",
            "/DipvLom/static/media/info.jpg");
        return dtoBuilder.build();
    }

    @Override
    public Map<Long, NewsOutBodyDTO> prepareNews(List<News> list) {
        final Map<Long, NewsOutBodyDTO> newsById = new HashMap<>();

        for (News newsU : list) {
            newsById.put(newsU.getId(), writeNews(newsU));
        }
        return newsById;
    }
}
