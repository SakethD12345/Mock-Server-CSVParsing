import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ArrayList<List<String>> loadedCSV;
    private LoadHandler loadHandler;
    public Server() {

        this.loadedCSV = new Map<String, Object>();
        //this.loadHandler = new LoadHandler(this);

        int port = 3232;

        Spark.port(port);
        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });

        // Setting up the handler for the GET /order and /mock endpoints
        Spark.get("load", new LoadHandler());
        Spark.get("view", new ViewHandler());
        Spark.init();
        Spark.awaitInitialization();

        // Notice this link alone leads to a 404... Why is that?
        System.out.println("Server started at http://localhost:" + port);

    }
    public static void main(String[] args) {
        // At time of creation, we decide on a specific datasource class:
        Server server = new Server();
        // Notice that this runs, but the program continues executing. Why
        // do you think that is? (We'll address this in a couple of weeks.)
        System.out.println("Server started; exiting main...");
    }




    public Map<String, Object> getLoadedCSV() {
        return this.loadedCSV;
    }

    public void setLoadedCSV(Map<String, Object> loadedCSV) {
        this.loadedCSV = loadedCSV;
    }

}
