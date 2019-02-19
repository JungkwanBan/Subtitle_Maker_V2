package co.makestar.sbvmaker.model;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by bjk11 on 2017-06-08.
 */
@Data
@Component
public class CrawlInfo {
  String fileName;
  String sheetId;
  String sheetName;
  String subType;
}
