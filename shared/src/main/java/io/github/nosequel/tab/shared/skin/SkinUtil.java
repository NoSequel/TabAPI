package io.github.nosequel.tab.shared.skin;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinUtil {

    private final static Map<UUID, String[]> cache = new HashMap<>();

    /**
     * Get the skin data by a player's unique identifier
     *
     * @param uuid the unique identifier to get the skin data by
     * @return the skin data
     * @throws UnirestException thrown if something went wrong while fetching from the mojang api
     */
    public static String[] getSkinData(UUID uuid) throws UnirestException {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        final HttpResponse<JsonNode> response = Unirest.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "")).asJson();
        final JSONObject body = response.getBody().getObject();

        if (!body.has("value") || !body.has("signature")) {
            throw new IllegalArgumentException("Unable to find profile by UUID " + uuid.toString());
        }

        return cache.put(uuid, new String[]{
                body.getString("value"),
                body.getString("signature")
        });
    }
}