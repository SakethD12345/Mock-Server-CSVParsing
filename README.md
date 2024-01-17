Design choices:
The main class makes the first overarching constructor call in its run method where it creates a new server object.
This calls the server constructor which creates the port using spark for the http://localhost:4567 url to work. Next it
makes a call to the LoadHandler constructor. The LoadHandler takes in a filename and uses a CSVParser object to parse
the file with that filename. The RowCreator class that makes each row makes a new row and copies in the old row which
adds a layer of security for the loadcsv. Next the server constructor creates a new ViewHandler which displays a 2D Json
Array with result: success (if it worked) and then data: [data] with the parsed File displayed for the user to see. It
gets the parsedFile using the getLoadedCSV method in Server. Next, the SearchHandler is created which creates a
CSVSearcher using the getLoadedCSV and getHeader method from Server. The other main call in the SearchHandler is to the
search methods in the searcher class (one that takes in a column identifier and one that doesn’t). The final handler
that is made in the server constructor is the BroadbandHandler. The BroadbandHandler uses the CensusDatasource
interface which requires four methods: getStateCode, getCountyCode, getBroadband with a cache parameter, and
getBroadband without a cache parameter. The main class we made (that isn’t a mock class) that implements this
interface was APICensusDatasource. The BroadbandHandler gets the county and state names and calls the getBroadband
method from the CensusDatasource interface which gets the broadband percentage. In the error catching outside of the
java errors that we used we also used DatasourceException, ParserException, and SearchException which we made.

For this project we used hash maps mainly for mapping the state names to their respective code and mapping the county
names to the county codes. The first cache we implemented was for storing the county codes. This cache uses the state
name as a key and the value is the hash map of county name to county code. The reason we used a cache for this was
because there are 50 states with on average over 50 counties. We only save a certain amount of state name to county name
to county codes using a max size and timer of 10 minutes. We thought this was logical as if they are searching for
broadband percentages it’s likely they might want to use multiple counties in a state so in that case the state’s
county’s will be saved in the cache for accessibility. The other cache we implemented was to store the actual broadband
percentages for the counties. It is passed in to the BroadbandHandler for the constructor which allows the user to give
their own restrictions for the max time and max size and so on for the cache.

Errors/Bugs:
The only sustained error we ran into was the data that was supposed to display when ViewHandler was called was always
empty. We went line by line to see where this could be happening along with printing out the loadedCSV variable at
certain places to see where it’s empty and where it’s populated. We finally realized that the problem was that we were
passing the loadedCSV variable into the ViewHandler constructor as a parameter which doesn’t work because the CSV is
loaded and parsed in the handle method of LoadHandler which is done when the user hits enter on the call
http://localhost:4567/viewcsv… url. Once we fixed it we agreed on the design choice to not have parameters in the
constructors of load, view, or search handler to avoid repeating the issue.

Other than that we never ran into problems (outside of with packaging classes, setup, or maven) that wasn’t immediately
noticed and fixed, as we programmed defensively with avoiding bugs in mind.

One syntax error we forgot was to put pair programming in our GitHub commits even though we pair programmed all of the
code together.

Tests:
There are 4 different test classes that test out different parts of the entire program. The first is the TestAPIHandler
class which tests the broadband handler and in turn tests the APICensusDatasource class and methods. The next class is
the TestCensusDatasource class which more specifically tests the methods in the CensusDatasources which are getStateCode
and getCountyCode. The third testing class is the TestCSVHandler which tests the other three handlers which are load,
view, and search. It also tests the error catching that is dealt with in these handlers. Finally, the last testing
class is the TestCSVProcessing class which tests that the exceptions we added for the searcher and parser classes for
CSV are thrown properly and when intended. To run the tests you can simply just run the entire class for all the 4 test
suites.

Running the Program:
You run the program by running the main class which will prompt the user to click on a url in the terminal. Once you do
so you will end up at http://localhost:4567 where you can put
http://localhost:4567/loadcsv?filename=[insert filename]&header=[true or false] to get the file loaded and parsed. The
website will tell the user it was successful and display the filename if the load was successful. Then the user can
view the loaded CSV file by calling http://localhost:4567/viewcsv, and can search the loaded CSV file by calling
http://localhost:4567/searchcsv?target=[target search], which will search across all columns, or
by calling
http://localhost:4567/searchcsv?target=[target search]&header=[true or false]&identifier=[column name or index] to
search in a specific column. Finally, the user can find the broadband percentage for a county in a state by calling
http://localhost:4567/broadband?state=[state]&county=[county].
