public interface CensusDatasource {
    public String getStateCode(String state) throws DatasourceException;
    public String getCountyCode(String county, String state) throws DatasourceException;
}
