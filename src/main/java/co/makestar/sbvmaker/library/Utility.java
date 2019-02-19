package co.makestar.sbvmaker.library;

import co.makestar.sbvmaker.model.SubElement;
import co.makestar.sbvmaker.model.TimeCode;
import co.makestar.sbvmaker.model.enums.LocaleType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static co.makestar.sbvmaker.Runner.newLine;

/**
 * Created by bjk11 on 2017-06-08.
 */
@Slf4j
@Component
public class Utility {
  private String filePath = System.getProperty("user.dir");

  public String detectCap() {
    File dir = new File(filePath);
    FileFilter fileFilter = new WildcardFileFilter("*.sbv");
    File[] files = dir.listFiles(fileFilter);

    List<String> capList = new ArrayList<>();

    if (files != null) {
      for (File f : files) {

        String fileNamePattern = "sub_(en|ja|zh)_.*.sbv";
        Pattern p = Pattern.compile(fileNamePattern);
        Matcher m = p.matcher(f.getName());

        if (!m.find()) {
          capList.add(f.getName());
          
        }
      }
    }

    if (capList.size() > 0) {
      return capList.get(0);
    } else {
      return "";
    }
  }

  public String readFileToString(String fileName) throws IOException {
    File file = new File(filePath + "\\" + fileName);
    return FileUtils.readFileToString(file, "UTF-8");
  }

  public List<SubElement> readSubToList(String fileName) {

    List<SubElement> result = new ArrayList<>();
    File file = new File(filePath + "\\" + fileName);

    try {
      List<String> lines = FileUtils.readLines(file, "UTF-8");
      int count = (int) Math.floor(lines.size() / 3);

      for (int i = 0; i < count; i++) {
        SubElement ele = new SubElement();
        int lineCount = 3 * i;
        String[] times = lines.get(lineCount + 0).split(",");
        ele.setStartTime(getTimeCode(times[0]));
        ele.setEndTime(getTimeCode(times[1]));
        ele.setSubTextKo(lines.get(lineCount + 1));
        result.add(ele);

      }
    } catch (IOException e) {
      log.error("() : ()", e, e.getStackTrace());
    }

    return result;

  }

  public void addText(TextFlow textFlow, String text, String id, boolean isLineBreak) {
    Text t = new Text(text);
    t.setId(id);
    textFlow.getChildren().add(t);
    if (isLineBreak) {
      textFlow.getChildren().add(new Text(newLine));
    }
  }

  private TimeCode getTimeCode(String text) {
    TimeCode result = new TimeCode();
    String[] el = text.split(":");
    String[] sec = el[2].split("\\.");

    result.setHour(el[0]);
    result.setMin(el[1]);
    result.setSec(sec[0]);
    result.setMill(sec[1]);

    return result;

  }
}
