package com.gameiom.real.sports;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Myhajlo.Rozputnyj
 */
public class CsvReader {
    public Map<String, String> parseCSV() throws IOException {
        Map<String, String> users = new HashMap<>();
        CSVParser parser = new CSVParser(new FileReader("src/main/resources/userName.csv"), CSVFormat.DEFAULT.withHeader());
        for (CSVRecord record : parser) {
            users.put(record.get("userName"), record.get("currency"));
        }
        parser.close();
        return users;
    }

}
