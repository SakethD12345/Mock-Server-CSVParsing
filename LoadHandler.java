public class LoadHandler implements Route {

    public LoadHandler(String filepath) {

    }
    public Object handle(Request request) {
        String filename = request.queryParmams("filename");
        Moshi moshi = new Moshi.Builder().build();
        
    }
}
