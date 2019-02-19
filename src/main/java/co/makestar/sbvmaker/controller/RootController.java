package co.makestar.sbvmaker.controller;

import co.makestar.sbvmaker.library.SubMaker;
import co.makestar.sbvmaker.library.Utility;
import co.makestar.sbvmaker.model.CrawlInfo;
import co.makestar.sbvmaker.model.SubElement;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.felixroske.jfxsupport.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AutoConfigurationPackage
@FXMLController
public class RootController<ｐ> {
  @FXML
  private Button subBuilderButton, subUploadButton;
  @FXML
  private TextField fileName, sheetId, sheetName;
  @FXML
  private TextFlow message;
  @FXML
  private ToggleGroup subFormat;

  @FXML
  private ScrollPane messageScroll;

  private CrawlInfo crawlInfo = new CrawlInfo();

  private Utility utility = new Utility();

  private SubMaker subMaker = new SubMaker();

  private ObjectMapper mapper = new ObjectMapper();

  private boolean isFileNameExist = false;
  private boolean isSheetIdExist = false;
  private boolean isSheetNameExist = false;

  @FXML
  public void initialize() {

    try {

      // 경로내에 sbv 파일이 있을 경우 경로 자동으로 불러옴
      String defaultFileName = utility.detectCap();
      fileName.setText(defaultFileName);

      // receltValue.txt 파일에서 내용
      String recentValuesString = utility.readFileToString("recentValue.txt");
      if (!recentValuesString.isEmpty()) {
        crawlInfo = mapper.readValue(recentValuesString, CrawlInfo.class);
        sheetName.setText(crawlInfo.getSheetName());
        sheetId.setText(crawlInfo.getSheetId());
        if (fileName.getText().equals("")) {
          fileName.setText(crawlInfo.getFileName());
        }
      }

      subFormat.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
        log.debug(newValue.getUserData().toString());
        crawlInfo.setSubType(newValue.getUserData().toString());
      });

      subBuilderButton.setOnAction(this::handleSubBuilderButtonAction);
      subUploadButton.setOnAction(this::handleSubUploadButtonAction);


    } catch (Exception e) {
      log.error(e.getMessage(), e);
      message.getChildren().addAll(new Text(e.getMessage()), new Text(e.toString()));
    }
  }

  @FXML
  private void handleSubBuilderButtonAction(ActionEvent event) {
    checkInputText();

    log.debug("test point");

    try {
      if (isFileNameExist && isSheetIdExist && isSheetNameExist) {
        messageBoxClear("자막 변환 시작");

        crawlInfo.setFileName(fileName.getText());
        crawlInfo.setSheetId(sheetId.getText());
        crawlInfo.setSheetName(sheetName.getText());

        updateHistory();

        List<SubElement> subs = subMaker.getSubRawData(crawlInfo, message);

        switch (crawlInfo.getSubType()){
          case "sbv":
            List<String> subString = subMaker.buildSbv(subs);
            FileUtils.write(
                    new File(String.format("%s\\sub_zh_%d.sbv",
                            System.getProperty("user.dir"),
                            System.currentTimeMillis())),subString.get(0),"UTF-8");
            FileUtils.write(
                    new File(String.format("%s\\sub_ja_%d.sbv",
                            System.getProperty("user.dir"),
                            System.currentTimeMillis())),subString.get(1),"UTF-8");
            FileUtils.write(
                    new File(String.format("%s\\sub_en_%d.sbv",
                            System.getProperty("user.dir"),
                            System.currentTimeMillis())),subString.get(2),"UTF-8");
            break;
          case "srt":
            List<String> subStringSrt = subMaker.buildSrt(subs);
            FileUtils.write(
                    new File(String.format("%s\\sub_zh_%d.srt",
                            System.getProperty("user.dir"),
                            System.currentTimeMillis())),subStringSrt.get(0),"UTF-8");
            FileUtils.write(
                    new File(String.format("%s\\sub_ja_%d.srt",
                            System.getProperty("user.dir"),
                            System.currentTimeMillis())),subStringSrt.get(1),"UTF-8");
            FileUtils.write(
                    new File(String.format("%s\\sub_en_%d.srt",
                            System.getProperty("user.dir"),
                            System.currentTimeMillis())),subStringSrt.get(2),"UTF-8");
            break;
          case "ass":
            break;
          default:
            break;
        }



      } else {
        throw new Exception();
      }

    } catch (Exception e) {
      utility.addText(message, "에러가 발생했습니다.", "error", true);
      utility.addText(message, String.format("%s : %s", e, e.getMessage()), "error", true);
    }
  }

  @FXML
  private void handleSubUploadButtonAction(ActionEvent event) {
    checkInputText();

    log.debug("test point");

    try {
      if (isFileNameExist && isSheetIdExist && isSheetNameExist) {
        messageBoxClear("파일 업로드 시작");

        crawlInfo.setFileName(fileName.getText());
        crawlInfo.setSheetId(sheetId.getText());
        crawlInfo.setSheetName(sheetName.getText());

        updateHistory();

        List<SubElement> subs = utility.readSubToList(fileName.getText());
        subMaker.setSubToGSheet(crawlInfo, message, subs);
      } else {
        throw new Exception();
      }

    } catch (Exception e) {
      utility.addText(message, "에러가 발생했습니다.", "error", true);
      utility.addText(message, String.format("%s : %s", e, e.getMessage()), "error", true);
    }
  }

  private void messageBoxClear(String startMessage) {
    message.getChildren().clear();
    utility.addText(message, startMessage + " : ", "header", false);
    utility.addText(message, "  " + LocalDateTime.now().toString(), "normal", true);
  }

  private void checkInputText() {
    crawlInfo.setSubType("sbv"); // 기본 출력포맷 : sbv 로 지정
    if (!fileName.getText().isEmpty()) {
      isFileNameExist = true;
      utility.addText(message, "파일명 : ", "subHeader", false);
      utility.addText(message, "  " + fileName.getText(), "normal", true);
    } else {
      utility.addText(message, "파일명을 입력해주세요", "error", true);
    }

    if (!sheetId.getText().isEmpty()) {
      isSheetIdExist = true;
      utility.addText(message, "시트ID : ", "subHeader", false);
      utility.addText(message, "  " + sheetId.getText(), "normal", true);
    } else {
      utility.addText(message, "시트ID를 입력해주세요", "error", true);
    }

    if (!sheetName.getText().isEmpty()) {
      isSheetNameExist = true;
      utility.addText(message, "시트명 : ", "subHeader", false);
      utility.addText(message, "  " + sheetName.getText(), "normal", true);
    } else {
      utility.addText(message, "시트명을 입력해주세요", "error", true);
    }
  }

  private void updateHistory() throws Exception {
    String JsonValue = mapper.writeValueAsString(crawlInfo);
    FileUtils.write(new File("recentValue.txt"), JsonValue, "UTF-8");
  }
}

