package ru.services.logic.school.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.Main;
import ru.configs.AppConfig;
import ru.controllers.school.analytics.KidJournalController;
import ru.controllers.school.analytics.PeriodController;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.school.analytics.period.PeriodOutBodyDTO;
import ru.data.DTO.controller.school.analytics.period.PeriodOutDTO;
import ru.data.DTO.controller.school.analytics.period.PeriodsInnerDTO;
import ru.data.reps.school.PeriodRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.DAYS;

/** RU: сервис для контроллера
 * @see PeriodController */
@Service
@Slf4j
@RequiredArgsConstructor
public class PeriodService implements IPeriodService {
    private final PeriodRepository periodRepository;
    private final SchoolRepository schoolRepository;

    @Override
    public PeriodOutDTO addPeriod(PeriodsInnerDTO body, School school) {
        final PeriodOutDTO.PeriodOutDTOBuilder dtoBuilder = PeriodOutDTO.builder();
        final PeriodOutBodyDTO.PeriodOutBodyDTOBuilder bodyDTOBuilder = PeriodOutBodyDTO.builder();

        final Period period = new Period();
        period.setName(body.name);
        period.setDateN(body.perN);
        period.setDateK(body.perK);
        periodRepository.saveAndFlush(period);
        school.getPeriods().add(period);
        schoolRepository.saveAndFlush(school);
        bodyDTOBuilder.name(period.getName())
            .perN(period.getDateN())
            .perK(period.getDateK());
        dtoBuilder.body(bodyDTOBuilder.build());
        return dtoBuilder.build();
    }

    @Override
    public PeriodOutDTO prepareInfo(List<Period> periods) {
        final PeriodOutDTO.PeriodOutDTOBuilder dtoBuilder = PeriodOutDTO.builder();
        final Map<Integer, PeriodOutBodyDTO> periodByNum = new HashMap<>();

        log.trace(periods + "");
        int i = 0;
        for (Period period : periods) {
            final PeriodOutBodyDTO.PeriodOutBodyDTOBuilder bodyDTOBuilder = PeriodOutBodyDTO.builder();
            if(period == null) continue;

            bodyDTOBuilder.name(period.getName())
                .perN(period.getDateN())
                .perK(period.getDateK());
            periodByNum.put(i, bodyDTOBuilder.build());
            i++;
        }
        dtoBuilder.bodyP(periodByNum);
        return dtoBuilder.build();
    }

    /** RU: исходя из заданных периодов в школе и актуальной даты
     * выбирается активный период
     * @see KidJournalController#getInfo(SubscriberDTO, CustomToken)  Пример использования */
    @Override
    public Period getActualPeriodBySchool(School school) {
        try {
            long now = DAYS.toMillis(LocalDate.now().toEpochDay());
            for (Period per : school.getPeriods()) {
                if (now >= AppConfig.dataFormat.parse(per.getDateN()).getTime() && now <= AppConfig.dataFormat.parse(per.getDateK()).getTime()) {
                    return per;
                }
            }
        } catch (ParseException e) {
            Main.excp(e);
        }
        return null;
    }
}
