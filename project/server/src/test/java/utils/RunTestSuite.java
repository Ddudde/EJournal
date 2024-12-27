package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.AuthControllerTest;

/** RU: Запускает тесты при помощи junit-platform-suite */
@Suite
@SelectClasses(AuthControllerTest.class)
//@SelectPackages("ru")
@IncludeTags({"auth"})
public class RunTestSuite {}