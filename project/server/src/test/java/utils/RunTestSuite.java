package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import ru.controllers.people.ParentsControllerTest;
import ru.controllers.school.RequestControllerTest;
import ru.services.db.IniDBServiceTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
//@SelectClasses({RequestControllerTest.class})
@SelectPackages("ru.controllers.school")
//@IncludeTags({"getRequests", "chText"})
public class RunTestSuite {}