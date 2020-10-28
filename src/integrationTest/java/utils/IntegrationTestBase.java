package utils;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;

public class IntegrationTestBase {
    public static final String CREATE_TASK = "https://raw.githubusercontent.com/hmcts/wa-workflow-resources/master/wa/teams/create_task.bpmn";

    @Before
    public void setup_files_for_tests() throws IOException {

        GetFileFromRepo.getFile(CREATE_TASK);
    }

    //@After
    //public void teardown() {
    //    GetFileFromRepo.deleteFile();
    //}

}
