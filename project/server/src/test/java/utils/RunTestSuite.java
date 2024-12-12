package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.school.analytics.JournalControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses(JournalControllerTest.class)
//@SelectPackages("ru")
@IncludeTags({"getInfo", "getInfoPers"})
public class RunTestSuite {}