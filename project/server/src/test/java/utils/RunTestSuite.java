package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.school.analytics.ScheduleControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses(ScheduleControllerTest.class)
//@SelectPackages("ru")
@IncludeTags({"getSchedule"})
public class RunTestSuite {}