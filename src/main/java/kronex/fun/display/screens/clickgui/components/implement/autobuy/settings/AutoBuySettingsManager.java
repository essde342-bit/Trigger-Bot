package kronex.fun.display.screens.clickgui.components.implement.autobuy.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class AutoBuySettingsManager {
    private static AutoBuySettingsManager instance;
    private final Map<String, SettingsData> settingsMap = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private AutoBuySettingsManager() {}
    public static AutoBuySettingsManager getInstance() { if (instance == null) instance = new AutoBuySettingsManager(); return instance; }

    public void saveSettings(String itemName, AutoBuyItemSettings settings) {
        settingsMap.put(itemName.toLowerCase(), new SettingsData(settings.getBuyBelow(), settings.getSellAbove(), settings.getMinQuantity()));
    }
    public void loadSettings(String itemName, AutoBuyItemSettings settings) {
        SettingsData data=settingsMap.get(itemName.toLowerCase());
        if(data!=null){settings.setBuyBelow(data.buyBelow);settings.setSellAbove(data.sellAbove);settings.setMinQuantity(data.minQuantity);}
    }
    public boolean hasSettings(String itemName){return settingsMap.containsKey(itemName.toLowerCase());}
    public JsonObject saveToJson(){JsonObject json=new JsonObject();for(Map.Entry<String,SettingsData> e:settingsMap.entrySet()){JsonObject item=new JsonObject();item.addProperty("buyBelow",e.getValue().buyBelow);item.addProperty("sellAbove",e.getValue().sellAbove);item.addProperty("minQuantity",e.getValue().minQuantity);json.add(e.getKey(),item);}return json;}
    public void loadFromJson(JsonObject json){if(json!=null)for(String key:json.keySet()){JsonObject item=json.getAsJsonObject(key);settingsMap.put(key.toLowerCase(),new SettingsData(item.get("buyBelow").getAsInt(),item.get("sellAbove").getAsInt(),item.get("minQuantity").getAsInt()));}}
    private static class SettingsData { int buyBelow,sellAbove,minQuantity; SettingsData(int b,int s,int q){buyBelow=b;sellAbove=s;minQuantity=q;} }
}
