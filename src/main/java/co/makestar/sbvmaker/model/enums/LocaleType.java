package co.makestar.sbvmaker.model.enums;

import lombok.Getter;

/**
 * Created by bjk11 on 2016-06-09.
 */
public enum LocaleType {
  ko("한국어"),
  ja("일본어"),
  en("영어"),
  zh("중국어");

  @Getter
  private String description;
  LocaleType(String description) {
    this.description = description;
  }

}
