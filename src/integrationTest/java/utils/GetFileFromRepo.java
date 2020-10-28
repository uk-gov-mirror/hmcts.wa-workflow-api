package utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public class GetFileFromRepo {
    static final Path PATH_TO_RESOURCES = Path.of("src/integrationTest/resources");

    private GetFileFromRepo() {
        //private constructor
    }

    public static int getFile(String url) throws IOException {
        InputStream inputStream = new URL(url).openStream();
        String tableName =  url.split("/")[url.split("/").length - 1];
        FileOutputStream fileOS = new FileOutputStream(PATH_TO_RESOURCES + "/" + tableName);
        IOUtils.copy(inputStream, fileOS);
        return 1;


    }

    public static int deleteFile() {
        File file = new File(PATH_TO_RESOURCES + "/" + "create_task.bpmn");
        if(file.delete()) {
            return 1;
        } else {
            return 0;
        }
    }
}

