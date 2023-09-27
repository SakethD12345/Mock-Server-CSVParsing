public final class Main {
    /**
     * The initial method called when execution begins.
     *
     * @param args An array of command line arguments
     */
    public static void main(String[] args) {
        new Main(args).run();
    }

    private Main(String[] args) {}

    /**
     * A method that prompts user input for a file name (and information about the file), search term,
     * and column identifier and then performs a search on the given file for the search term in the
     * correct column (if applicable). This method is called in the main method above.
     */
    private void run() {
        new Server();
        System.out.println("Server started; exiting main...");
    }
}
