package uk.gov.hmcts.reform.waworkflowapi.config;

import org.junit.jupiter.api.Test;
import utils.GetFileFromRepo;

import java.io.IOException;

public class GetFileFromRepoTest {

    @Test
    public void get_file_from_github_and_create_table_in_resouces_test() throws IOException, InterruptedException {
        GetFileFromRepo.getFile("https://raw.githubusercontent.com/hmcts/wa-workflow-resources/master/wa/teams/create_task.bpmn");
    }
}
