package com.personalbis;
public final class PanelLayoutPolicy {
 private PanelLayoutPolicy(){}
 public static int contentWidth(int panelWidth){return Math.max(1,panelWidth);}
 public static int slayerRowWidth(int panelWidth){return Math.max(1,panelWidth-8);}
}
