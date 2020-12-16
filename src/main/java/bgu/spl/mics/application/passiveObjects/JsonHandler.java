package bgu.spl.mics.application.passiveObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonHandler {
    /**
     * deserialize a json file to an object
     * @param path of the json file
     * @return FlowData object with all the information from the json
     * @throws IOException in case of wrong path
     */
    public static FlowData deserialize(String path) throws IOException {
        Gson gson = new Gson();
        try(Reader reader = Files.newBufferedReader(Paths.get(path))){
            // convert json file to flowData object
            FlowData flowData = gson.fromJson(reader, FlowData.class);
            reader.close();
            return flowData;
        }
        catch (IOException e){
            throw e;
        }
    }

    /**
     * serialize an object to a json file to a given path
     * @param diary the object that contains all data to serialize
     * @param path of the created json file
     * @throws IOException in case of wrong path
     */
    public static void serialize(Diary diary, String path) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter writer = new FileWriter(path);) {
            // convert diary object to json format
            gson.toJson(diary, writer);
            writer.close();
        }
        catch (IOException e){
            throw e;
        }
    }
}
