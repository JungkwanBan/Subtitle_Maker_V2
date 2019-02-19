package co.makestar.sbvmaker.library;

import co.makestar.sbvmaker.model.CrawlInfo;
import co.makestar.sbvmaker.model.SubElement;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static co.makestar.sbvmaker.Runner.newLine;

/**
 * Created by bjk11 on 2016-08-26.
 */
@Slf4j
@Component
public class SubMaker {

  //  private final ApachePoi apachePoi = new ApachePoi();
  private final Utility utility = new Utility();

  public void setSubToGSheet(CrawlInfo crawlInfo, TextFlow message, List<SubElement> subs) {
    try {
      Sheets service = GoogleApi.getSheetService();
      ValueRange response = service.spreadsheets().values().get(crawlInfo.getSheetId(), crawlInfo.getSheetName()).execute();

      boolean isSubExits = false;

      if (response.getValues() != null) {
        List<List<Object>> values = response.getValues();
        for (List<Object> cells : values) {
          String cell = cells.size() != 0 ? (String) cells.get(0) : "";
          if (cell.equals("script_start")) {
            isSubExits = true;
          }
        }
      }

      log.debug("test point");

      if (!isSubExits) {
        List<List<Object>> subDataList = subSheetHeader();

        for (List<Object> subData : subDataList) {
          response.getValues().add(subData);
        }

        for (int i = 0; i < subs.size(); i++) {
          List<Object> row = new ArrayList<>();
          row.add(i + 1);
          row.add(subs.get(i).getSubTextKo());
          response.getValues().add(row);
        }

        // 자막입력 종료 후 script_end 삽입
        List<Object> row = new ArrayList<>();
        row.add("script_end");
        response.getValues().add(row);

        ValueRange body = new ValueRange()
                .setValues(response.getValues());

        UpdateValuesResponse result =
                service.spreadsheets().values().update(crawlInfo.getSheetId(), crawlInfo.getSheetName() + "!A1", body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
        log.debug("%d cells updated.", result.getUpdatedCells());

        utility.addText(message, "[PROGRESS]", "header", false);
        utility.addText(message, " 자막 업로드 완료", "progress", true);
        utility.addText(message, "한글 자막 업로드가 완료되었습니다. 번역팀에 번역 요청해주세요.", "normal", true);

        utility.addText(message, "[LOG]", "header", false);
        utility.addText(message, " 업로드 결과", "progress", true);
        utility.addText(message, "파일명 :", "subHeader", false);
        utility.addText(message, crawlInfo.getFileName(), "normal", true);
        utility.addText(message, "시트ID :", "subHeader", false);
        utility.addText(message, crawlInfo.getSheetId(), "normal", true);
        utility.addText(message, "시트명 :", "subHeader", false);
        utility.addText(message, crawlInfo.getSheetName(), "normal", true);
        utility.addText(message, "자막 줄 수 :", "subHeader", false);
        utility.addText(message, String.format("%d 줄", subs.size()), "normal", true);
      } else {
        utility.addText(message, "[ERROR]", "header", false);
        utility.addText(message, " 자막 데이터 존재", "error", true);
        utility.addText(message, "해당 시트에 자막 데이터가 이미 존재합니다. script_start ~ script_end 까지의 내용을 지우고 다시 업로드 해 주세요", "normal", true);
      }
    } catch (Exception e) {
      utility.addText(message, "[ERROR]", "header", false);
      utility.addText(message, "업로드 중 에러가 발생하였습니다.", "error", true);
      utility.addText(message, e.getStackTrace().toString(), "normal", true);
      utility.addText(message, String.format("%s : %s", e, e.getMessage()), "normal", true);
    }
  }

  public List<SubElement> getSubRawData(CrawlInfo crawlInfo, TextFlow message) {
    try {

      List<SubElement> subs = utility.readSubToList(crawlInfo.getFileName());

      Sheets service = GoogleApi.getSheetService();
      ValueRange response = service.spreadsheets().values().get(crawlInfo.getSheetId(), crawlInfo.getSheetName()).execute();
      List<List<Object>> values = response.getValues();

      int startNo = 0;
      int lastNo = 0;
      for (int i = 0; i < values.size(); i++) {
        String cell = values.get(i).size() != 0 ? (String) values.get(i).get(0) : "";

        if (cell.equals("script_start")) {
          startNo = i + 2;
        } else if (cell.equals("script_end")) {
          lastNo = i;
        }
      }

      log.debug("text point");

      List<List<Object>> subsList = values.subList(startNo, lastNo);

      log.debug("text point");

      if (subsList.size() != subs.size()) {
        utility.addText(message, "[ERROR]", "header", false);
        utility.addText(message, "자막 파일과 시트의 줄 수가 일치하지 않습니다.", "error", true);
        throw new Exception();
      } else {
        for (int i = 0; i < subs.size(); i++) {
          subs.get(i).setSubTextZh((String) subsList.get(i).get(2));
          subs.get(i).setSubTextJa((String) subsList.get(i).get(3));
          subs.get(i).setSubTextEn((String) subsList.get(i).get(4));
        }
      }

      log.debug("text point");
      return subs;

    } catch (
            Exception e) {
      utility.addText(message, "[ERROR]", "header", false);
      utility.addText(message, "자막생성 중 에러가 발생하였습니다.", "error", true);
      utility.addText(message, e.getStackTrace().toString(), "normal", true);
      utility.addText(message, String.format("%s : %s", e, e.getMessage()), "normal", true);
    }
    return null;
  }

  private List<List<Object>> subSheetHeader() {
    List<List<Object>> result = new ArrayList<>();
    List<Object> blank = new ArrayList<>();
    result.add(blank);

    List<Object> firstRow = new ArrayList<>(1);
    firstRow.add("script_start");
    result.add(firstRow);

    List<Object> secondRow = new ArrayList<>(5);
    secondRow.add("프리뷰");
    secondRow.add("ko");
    secondRow.add("zh");
    secondRow.add("ja");
    secondRow.add("en");

    result.add(secondRow);
    return result;
  }

  public List<String> buildSrt(List<SubElement> subs) {
    List<String> result = new ArrayList<>();
    StringBuilder subZh = new StringBuilder();
    StringBuilder subJa = new StringBuilder();
    StringBuilder subEn = new StringBuilder();

    for (int i = 0; i < subs.size(); i++) {
      String startTimeHour = subs.get(i).getStartTime().getHour();
      Matcher mStart = Pattern.compile("\\S").matcher(startTimeHour);
      String startHour = (mStart.find()) ? "0" + startTimeHour : startTimeHour;
      String endTimeHour = subs.get(i).getEndTime().getHour();
      Matcher mEnd = Pattern.compile("\\S").matcher(endTimeHour);
      String endHour = (mEnd.find()) ? "0" + endTimeHour : endTimeHour;

      String timeCode = String.format("%s:%s:%s,%s --> %s:%s:%s,%s",
              startHour,
              subs.get(i).getStartTime().getMin(),
              subs.get(i).getStartTime().getSec(),
              subs.get(i).getStartTime().getMill(),
              endHour,
              subs.get(i).getEndTime().getMin(),
              subs.get(i).getEndTime().getSec(),
              subs.get(i).getEndTime().getMill());

      subZh.append(i + 1).append(newLine);
      subZh.append(timeCode).append(newLine);
      subZh.append(subs.get(i).getSubTextZh()).append(newLine);
      addNewLine(i, subs, subZh);
      subJa.append(i + 1).append(newLine);
      subJa.append(timeCode).append(newLine);
      subJa.append(subs.get(i).getSubTextJa()).append(newLine);
      addNewLine(i, subs, subJa);
      subEn.append(i + 1).append(newLine);
      subEn.append(timeCode).append(newLine);
      subEn.append(subs.get(i).getSubTextEn()).append(newLine);
      addNewLine(i, subs, subEn);
    }

    result.add(subZh.toString());
    result.add(subJa.toString());
    result.add(subEn.toString());

    return result;
  }

  public List<String> buildSbv(List<SubElement> subs) {
    List<String> result = new ArrayList<>();

    StringBuilder subZh = new StringBuilder();
    StringBuilder subJa = new StringBuilder();
    StringBuilder subEn = new StringBuilder();

    for (int i = 0; i < subs.size(); i++) {
      String timeCode = String.format("%s:%s:%s.%s,%s:%s:%s.%s",
              subs.get(i).getStartTime().getHour(),
              subs.get(i).getStartTime().getMin(),
              subs.get(i).getStartTime().getSec(),
              subs.get(i).getStartTime().getMill(),
              subs.get(i).getEndTime().getHour(),
              subs.get(i).getEndTime().getMin(),
              subs.get(i).getEndTime().getSec(),
              subs.get(i).getEndTime().getMill());

      subZh.append(timeCode).append(newLine);
      subZh.append(subs.get(i).getSubTextZh()).append(newLine);
      addNewLine(i, subs, subZh);
      subJa.append(timeCode).append(newLine);
      subJa.append(subs.get(i).getSubTextJa()).append(newLine);
      addNewLine(i, subs, subJa);
      subEn.append(timeCode).append(newLine);
      subEn.append(subs.get(i).getSubTextEn()).append(newLine);
      addNewLine(i, subs, subEn);

    }
    result.add(subZh.toString());
    result.add(subJa.toString());
    result.add(subEn.toString());

    return result;
  }

  private StringBuilder addNewLine(int i, List<SubElement> subs, StringBuilder stringBuilder) {
    if (i < subs.size()) {
      return stringBuilder.append(newLine);
    } else {
      return stringBuilder.append(newLine).append(newLine);
    }
  }

}
