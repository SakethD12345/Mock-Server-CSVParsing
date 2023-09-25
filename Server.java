import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ArrayList<List<String>> loadedCSV;
    public Server() {

    }

    public void loadcsv(String filepath, Boolean hasHeader) {
        try {
            FileReader reader = new FileReader(filepath);
            RowCreator rowCreator = new RowCreator();
            CSVParser<List<String>> parser = new CSVParser<>(reader, hasHeader, rowCreator);
            this.loadedCSV = parser.parseCSVFile();
        }
        catch (FileNotFoundException e){
            System.err.println("CSV file not found. Please input a valid file path.");
        }
    }

    public String viewcsv() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<> adapter = moshi.adapter


    }
}
