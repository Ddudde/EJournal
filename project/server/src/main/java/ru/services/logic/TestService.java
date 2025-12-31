package ru.services.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.configs.AppConfig;
import ru.controllers.TestController;
import ru.data.DTO.controller.test.TestBodyDTO;
import ru.data.DTO.controller.test.TestInnerDTO;
import ru.data.DTO.controller.test.TestOutDTO;
import ru.data.DTO.service.data.initDB.InitDBServiceDTO;
import ru.services.db.InitDBService;

/** RU: сервис для контроллера
 * @see TestController */
@Service
@RequiredArgsConstructor
public class TestService implements ITestService {
    private final InitDBService initDBService;

    @Override
    public InitDBServiceDTO changeTests(TestInnerDTO body) {
        InitDBServiceDTO outDTO = null;
        switch (body.id) {
            case "checkbox_debug" -> AppConfig.DEBUG = body.val;
            case "checkbox_test" -> {
                AppConfig.TEST = body.val;
                if(AppConfig.TEST) {
                    initDBService.testOn();
                } else {
                    initDBService.testOff();
                }
                outDTO = initDBService.getTestInfo();
            }
            default -> {}
        }
        return outDTO;
    }

    @Override
    public TestOutDTO prepareInfo() {
        final var dtoBuilder = TestOutDTO.builder();
        final var bodyDTOBuilder = TestBodyDTO.builder();

        bodyDTOBuilder.checkbox_debug(AppConfig.DEBUG)
            .checkbox_test(AppConfig.TEST);
        dtoBuilder.bodyS(bodyDTOBuilder.build())
            .bodyT(initDBService.getTestInfo().bodyT());
        return dtoBuilder.build();
    }
}
