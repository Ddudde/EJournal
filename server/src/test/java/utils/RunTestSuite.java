package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.SSE.SSEControllerTest;
import ru.controllers.main.ProfileControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses({SSEControllerTest.class, ProfileControllerTest.class})
//@SelectPackages("ru.controllers.people")
@IncludeTags({"getProfile", "start"})
public class RunTestSuite {}