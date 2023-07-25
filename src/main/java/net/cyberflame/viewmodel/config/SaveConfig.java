package net.cyberflame.viewmodel.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cyberflame.viewmodel.Viewmodel;
import net.cyberflame.viewmodel.settings.Setting;
import net.cyberflame.viewmodel.util.Stopwatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author  ChiquitaV2
 */
public class SaveConfig {

    private Gson gson;
    private static Stopwatch saveTimer;

    public SaveConfig() {
        super();
        try {
            gson = new GsonBuilder().setPrettyPrinting().create();
            saveConfig();
            saveAllSettings();
            saveTimer = new Stopwatch();
            timedSave();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    static final String folderName = "Viewmodel/";

    private static void saveConfig() throws IOException {
        if (!Files.exists(Paths.get(folderName))) {
            Files.createDirectories(Paths.get(folderName));
        }
    }

    public static void saveAllSettings() {
        try {
            makeFile(null, "Viewmodel");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            OutputStreamWriter fileOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(folderName + Viewmodel.VIEWMODEL_JSON), StandardCharsets.UTF_8);
            JsonObject viewmodelObj = new JsonObject();

            for (Setting value : Viewmodel.getSettings()) {
                viewmodelObj.add(value.getName(), value.toJson());
            }


            String jsonString = gson.toJson(JsonParser.parseString(viewmodelObj.toString()));
            fileOutputStreamWriter.write(jsonString);
            fileOutputStreamWriter.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void makeFile(String location, String name) throws IOException {
        if (null != location) {
            Path path = Paths.get(folderName + location + name + ".json");
            if (Files.exists(path)) {
                File file = new File(folderName + location + name + ".json");

                if (file.delete()) {
                    Files.createFile(path);
                }
            } else {
                Files.createFile(path);
            }
        } else {
            Path path = Paths.get(folderName + name + ".json");
            if (Files.exists(path)) {
                File file = new File(folderName + name + ".json");

                file.delete();
            }
            Files.createFile(path);
        }

    }

    private static void timedSave() {
        if (saveTimer.passed(5000)) {
            saveAllSettings();
            saveTimer.reset();
        }
    }
}
