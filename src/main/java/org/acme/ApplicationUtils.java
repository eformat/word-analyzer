package org.acme;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class ApplicationUtils {

    private static final Logger log = LoggerFactory.getLogger(ApplicationUtils.class);

    private static HashMap<String, String> fileCache = new HashMap<>();

    public static String readFile(String fileName) {
        String contents = null;
        if (fileCache.containsKey(fileName)) {
            return fileCache.get(fileName);
        }
        try (InputStream inputStream = ApplicationUtils.class.getClassLoader().getResourceAsStream(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            contents = reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            log.warn(">>> Caught exception whilst reading file (" + fileName + ") " + e);
        }
        fileCache.put(fileName, contents);
        return contents;
    }

    // _addJson(jsonObject, "ab.g", "foo2");
    public static void addJson(JsonObject jsonObject, String key, String value) {
        if (key.contains(".")) {
            String innerKey = key.substring(0, key.indexOf("."));
            String remaining = key.substring(key.indexOf(".") + 1);

            if (jsonObject.containsKey(innerKey)) {
                addJson(jsonObject.getJsonObject(innerKey), remaining, value);
            } else {
                JsonObject innerJson = new JsonObject();
                jsonObject.put(innerKey, innerJson);
                addJson(innerJson, remaining, value);
            }
        } else {
            jsonObject.put(key, value);
        }
    }
}
