package cucumbreTests;
import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

//@CucumberOptions(
//            //plugin = {"pretty"},
//            features = {"src/test/resources/features"}, //path to .feature files
//            glue={"cucumber"} //packages names
//    )
@Suite
@IncludeEngines("cucumber")
@SelectPackages("cucumbreTests")
@ConfigurationParameter(
        key = Constants.FEATURES_PROPERTY_NAME,value = "src/test/resources/features")
//@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,value = "src/test/resources/features")
//@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME,value = "@googleSearch")
//@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,value = "false")
//@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,value = "pretty, html:target/cucumber-report/cucumber.html")

public class CucumberTest {
    }
