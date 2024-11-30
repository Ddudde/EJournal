package utils;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.people.HTeachersControllerTest;
import ru.controllers.school.DnevnikControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses({HTeachersControllerTest.class, DnevnikControllerTest.class})
//@SelectPackages("ru")
//@IncludeTags({"remGroup"})
public class RunTestSuite {}