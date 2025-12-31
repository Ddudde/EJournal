package utils;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
//@SelectClasses({RequestControllerTest.class})
@SelectPackages("ru.controllers.people")
//@IncludeTags({"getRequests", "chText"})
public class RunTestSuite {}