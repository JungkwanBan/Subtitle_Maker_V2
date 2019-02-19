package co.makestar.sbvmaker.model;

import lombok.Data;

@Data
public class SubElement {
  TimeCode startTime;
  TimeCode endTime;
  String subTextKo;
  String subTextJa;
  String subTextEn;
  String subTextZh;
  String subStyle;
}
