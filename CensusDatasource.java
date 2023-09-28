import com.google.common.cache.Cache;

import java.io.IOException;

public interface CensusDatasource {
    public String getStateCode(String state) throws DatasourceException, IOException;
    public String getCountyCode(String county, String state) throws DatasourceException, IOException;
    public String[] getBroadband(Cache<String, String[]> cache, String county, String state) throws DatasourceException, IOException;
    public String[] getBroadband(String county, String state) throws DatasourceException, IOException;
}
