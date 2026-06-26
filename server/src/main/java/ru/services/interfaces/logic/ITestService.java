package ru.services.interfaces.logic;

import ru.data.DTO.controller.test.TestInnerDTO;
import ru.data.DTO.controller.test.TestOutDTO;

public interface ITestService {
    TestOutDTO changeTests(TestInnerDTO body);

    TestOutDTO getTestInfo();

    TestOutDTO prepareInfo();
}
