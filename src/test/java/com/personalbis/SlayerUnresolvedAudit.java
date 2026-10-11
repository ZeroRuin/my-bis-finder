package com.personalbis;
import com.google.gson.*;
import com.personalbis.wiki.WikiDataPack;
import java.nio.file.*;
import java.util.*;
public class SlayerUnresolvedAudit {
 public static void main(String[] a)throws Exception {
 Gson g=new GsonBuilder().setPrettyPrinting().create();
 JsonObject scope=g.fromJson(Files.readString(Paths.get("src/main/resources/slayer-data/restricted-task-scope.json")),JsonObject.class);
 MonsterDatabase db=new MonsterDatabase(null,new WikiDataPack(g));db.rebuild();
 List<Map<String,Object>> rows=new ArrayList<>();
 for(Map.Entry<String,JsonElement> master:scope.getAsJsonObject("masters").entrySet())for(JsonElement t:master.getValue().getAsJsonArray()) {
 String task=t.getAsString();JsonArray areas=master.getKey().equals("Krystilia")?g.fromJson("[\"Wilderness\"]",JsonArray.class):scope.getAsJsonObject("konarLocations").getAsJsonArray(task);
 if(areas==null)continue;
 for(MonsterDefinition m:db.findTaskVariants(task)) {
 if(!SlayerTargetRequirements.allowed(m,99))continue;
 List<String> unknown=new ArrayList<>(),confirmed=new ArrayList<>(),excluded=new ArrayList<>();
 for(JsonElement ar:areas){String area=ar.getAsString();switch(db.taskLocationEligibility(task,m,area)){case UNKNOWN:unknown.add(area);break;case ELIGIBLE:confirmed.add(area);break;default:excluded.add(area);}}
 if(unknown.isEmpty())continue;
 Map<String,Object> r=new LinkedHashMap<>();r.put("master",master.getKey());r.put("task",task);r.put("name",m.getName());r.put("level",m.getCombatLevel());r.put("version",m.getVersion());r.put("id",m.getId());r.put("unknown",unknown);r.put("confirmed",confirmed);r.put("excluded",excluded);rows.add(r);
 }}
 Files.writeString(Paths.get(a.length == 0 ? "SLAYER-UNRESOLVED-VARIANTS.json" : a[0]),g.toJson(rows));System.out.println(rows.size()+" unresolved task/variant rows");
 }
}
