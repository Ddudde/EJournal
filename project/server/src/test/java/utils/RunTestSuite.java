package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.people.ParentsControllerTest;
import ru.services.db.IniDBServiceTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses({ParentsControllerTest.class, IniDBServiceTest.class})
//@SelectPackages("ru")
@IncludeTags({"addPar", "getTestInfo"})
public class RunTestSuite {}