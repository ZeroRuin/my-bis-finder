package com.personalbis;
public final class MagicAuditLine {
 private final String text;
 public MagicAuditLine(String text){this.text=text==null?"":text;}
 public String getText(){return text;}
}
