package cucumbreTests;

import org.junit.platform.suite.api.*;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;


//@CucumberOptions(
//            //plugin = {"pretty"},
//            features = {"src/test/resources/features"}, //path to .feature files
//            glue={"cucumber"} //packages names
//    )
@Suite
//@IncludeEngines("cucumber")
@SelectPackages("cucumbreTests")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "cucumbreTests"),
        @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
})
public class CucumberTest {
    public CucumberTest(){
        System.out.println("test");
    }
    }
