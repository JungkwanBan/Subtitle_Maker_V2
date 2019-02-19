package co.makestar.sbvmaker.library;

import com.google.api.services.drive.Drive;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Created by bjk11 on 2016-06-29.
 */
@Slf4j
@Component
public class ApachePoi {
  Workbook getWorkbookFromDrive(String fileId) throws Exception {
    Drive driveService = GoogleApi.getDriveService();
    InputStream inputStream = driveService.files()
            .export(fileId, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .executeMediaAsInputStream();
    return WorkbookFactory.create(inputStream);
  }
}
