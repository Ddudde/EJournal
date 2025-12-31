package ru.services.logic;

import ru.data.DTO.controller.test.TestInnerDTO;
import ru.data.DTO.controller.test.TestOutDTO;
import ru.data.DTO.service.data.initDB.InitDBServiceDTO;

public interface ITestService {
    InitDBServiceDTO changeTests(TestInnerDTO body);

    TestOutDTO prepareInfo();
}
