package com.limechain.utils.json;

import com.limechain.teavm.HttpRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    public static Object parseJson(String jsonString) {
        return new JsonParser(jsonString).parse();
    }

    public static String stringify(Object object) {
        return JsonSerializer.serializeToJson(object);
    }

    public static String readJsonFromFile(String filePath) {
        return HttpRequest.sendHttpRequest("GET", filePath, null);
    }
}
