package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.school.PJournalControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses({PJournalControllerTest.class})
//@SelectPackages("ru")
@IncludeTags({"addHomework"})
public class RunTestSuite {}