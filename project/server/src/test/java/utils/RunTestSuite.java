package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.people.HTeachersControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses(HTeachersControllerTest.class)
//@SelectPackages("ru")
@IncludeTags({"getInfo", "remSch"})
public class RunTestSuite {}