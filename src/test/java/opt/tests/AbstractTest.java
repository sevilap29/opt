package opt.tests;

import opt.data.FreewayScenario;
import opt.data.ProjectFactory;
import opt.data.Project;

import java.io.File;

import static org.junit.Assert.fail;

public abstract class AbstractTest {

    static protected String project_name = "projectB.opt";

    static protected String get_test_fullpath(String testname){
        return (new File("src/test/resources/" + testname)).getAbsolutePath();
    }

    public static class TestData {
        Project project;
        FreewayScenario scenario;
        public TestData(){
            try {
                project = ProjectFactory.load_project(get_test_fullpath(project_name),true);
                scenario = project.get_scenario_with_name("scenarioC");
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

}