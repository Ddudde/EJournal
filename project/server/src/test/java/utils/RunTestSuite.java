package utils;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.main.ProfileControllerTest;
import ru.controllers.people.HTeachersControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses({ProfileControllerTest.class, HTeachersControllerTest.class})
//@SelectPackages("ru")
//@IncludeTags({"auth"})
public class RunTestSuite {}