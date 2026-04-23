package com.yusuke.nbadatascraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSaver {
    private final ObjectMapper mapper;
    private final Map<String, List<Object>> objectSaves;
    private static final String BASE_PATH = "src/main/resources/data/";

    public DataSaver() {

        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.objectSaves = new HashMap<>();
        try {
            Files.createDirectories(Paths.get(BASE_PATH));
        } catch (IOException e) {
            System.err.println("Error creating output directory: " + e.getMessage());
        }
    }

    public void addObject(String fileName, Object object) {
        objectSaves.computeIfAbsent(fileName, k -> new ArrayList<>()).add(object);
    }

    public void saveObjects() {
        if (objectSaves.isEmpty()) return;

        objectSaves.forEach((fileName, newList) -> {
            Path path = Paths.get(BASE_PATH, fileName);
            List<Object> finalDataToSave = new ArrayList<>();

            if (Files.exists(path)) {
                try {
                    List<Object> existingData = mapper.readValue(path.toFile(),
                            mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                    finalDataToSave.addAll(existingData);
                } catch (IOException e) {
                    System.out.println("Initializing new file: " + fileName);
                }
            }

            finalDataToSave.addAll(newList);

            try {
                mapper.writeValue(path.toFile(), finalDataToSave);
                System.out.printf("File updated: %s (%d new, %d total)\n",
                        fileName, newList.size(), finalDataToSave.size());
            } catch (IOException e) {
                System.err.println("Failed to save " + fileName + ": " + e.getMessage());
            }
        });

        objectSaves.clear();
    }

    public <T> List<T> loadList(String fileName, Class<T> clazz) {
        File file = new File(BASE_PATH + fileName);
        if (!file.exists()) return new ArrayList<>();

        try {
            return mapper.readValue(file,
                    mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            System.err.println("Read error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}