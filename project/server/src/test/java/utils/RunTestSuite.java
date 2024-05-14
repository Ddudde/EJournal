package utils;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ru.controllers.people.AdminsControllerTest;

@Suite
@SelectClasses(AdminsControllerTest.class)
//@SelectPackages("ru.mirea")
@IncludeTags({"chPep", "remPep"})
public class RunTestSuite {}