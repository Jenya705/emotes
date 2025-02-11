package io.github.kosmx.emotes.main.config;

import com.google.gson.*;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.server.config.ConfigSerializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;


public class ClientConfigSerializer extends ConfigSerializer {

    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ClientConfig config = (ClientConfig) super.deserialize(json, typeOfT, context);

        clientDeserialize(json.getAsJsonObject(), config);

        return config;
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = super.serialize(config, typeOfSrc, context).getAsJsonObject();

        if(config instanceof ClientConfig) clientSerialize((ClientConfig) config, node);

        return node;
    }

    @Override
    protected SerializableConfig newConfig() {
        return new ClientConfig();
    }




    private void clientDeserialize(JsonObject node, SerializableConfig sconfig) {
        ClientConfig config = (ClientConfig) sconfig;
        EmoteFixer emoteFixer = new EmoteFixer(config.configVersion);
        if(node.has("fastmenu")) fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config, emoteFixer);
        if(node.has("keys")) keyBindsDeserializer(node.get("keys").getAsJsonObject(), config, emoteFixer);
    }

    private void fastMenuDeserializer(JsonObject node, ClientConfig config, EmoteFixer fixer){
        for(int i = 0; i != 8; i++){
            if(node.has(Integer.toString(i))){
                config.fastMenuEmotes[i] = fixer.getEmoteID(node.get(Integer.toString(i)));
            }
        }
    }

    private void keyBindsDeserializer(JsonObject node, ClientConfig config, EmoteFixer fixer){
        if(config.configVersion < 4){
            oldKeyBindsSerializer(node.getAsJsonArray(), config, fixer);
        }
        for(Map.Entry<String, JsonElement> element : node.entrySet()){
            config.emoteKeyMap.put(UUID.fromString(element.getKey()), EmoteInstance.instance.getDefaults().getKeyFromString(element.getValue().getAsString()));
            //config.emotesWithHash.add(new Pair<>(fixer.getEmoteID(n.get("id")), n.get("key").getAsString()));
        }
    }

    private void oldKeyBindsSerializer(JsonArray node, ClientConfig config, EmoteFixer fixer){
        for(JsonElement jsonElement : node){
            JsonObject n = jsonElement.getAsJsonObject();
            config.emoteKeyMap.add(new Pair<>(fixer.getEmoteID(n.get("id")), EmoteInstance.instance.getDefaults().getKeyFromString(n.get("key").getAsString())));
        }
    }

    private void clientSerialize(ClientConfig config, JsonObject node){
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }

    private JsonObject fastMenuSerializer(ClientConfig config){
        JsonObject node = new JsonObject();
        for(int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                node.addProperty(Integer.toString(i), config.fastMenuEmotes[i].toString());
            }
        }
        return node;
    }

    private JsonObject keyBindsSerializer(ClientConfig config){
        JsonObject array = new JsonObject();
        for(Pair<UUID, InputKey> emote : config.emoteKeyMap){
            array.addProperty(emote.getLeft().toString(), emote.getRight().getTranslationKey());
        }
        return array;
    }
}
