public class MockCensusDatasource implements CensusDatasource {

    public MockCensusDatasource() {

    }

    public String getStateCode(String state) {
        return "44";
    }

    public String getCountyCode(String county, String state) {
        return "007";
    }

}
