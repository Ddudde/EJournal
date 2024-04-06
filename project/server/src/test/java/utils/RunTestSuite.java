package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.mirea.controllers.main.SettingsControllerTest;

@Suite
@SelectClasses(SettingsControllerTest.class)
//@SelectPackages("ru.mirea")
@IncludeTags({"addNews"})
public class RunTestSuite {}