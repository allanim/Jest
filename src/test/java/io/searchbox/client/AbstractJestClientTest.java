package io.searchbox.client;

import io.searchbox.client.http.JestHttpClient;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.*;

/**
 * @author Dogukan Sonmez
 */

public class AbstractJestClientTest {

    JestHttpClient client = new JestHttpClient();

    @Test
    public void convertJsonStringToMapObject() {
        String json = "{\n" +
                "    \"ok\" : true,\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\"\n" +
                "}";
        Map jsonMap = client.convertJsonStringToMapObject(json);
        assertNotNull(jsonMap);
        assertEquals(4, jsonMap.size());
        assertEquals(true, jsonMap.get("ok"));
        assertEquals("twitter", jsonMap.get("_index"));
        assertEquals("tweet", jsonMap.get("_type"));
        assertEquals("1", jsonMap.get("_id"));
    }

    @Test
    public void convertEmptyJsonStringToMapObject() {
        Map jsonMap = client.convertJsonStringToMapObject("");
        assertNull(jsonMap);
    }

    @Test
    public void convertNullJsonStringToMapObject() {
        Map jsonMap = client.convertJsonStringToMapObject(null);
        assertNull(jsonMap);
    }


    @Test
    public void getSuccessIndexResult() {
        String jsonString = "{\n" +
                "    \"ok\" : true,\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\"\n" +
                "}\n";
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "");
        JestResult result = client.createNewElasticSearchResult(jsonString, statusLine, "INDEX", "");
        assertNotNull(result);
        assertTrue(result.isSucceeded());
    }

    @Test
    public void getFailedIndexResult() {
        String jsonString = "{\"error\":\"Invalid index\",\"status\":400}";
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 400, "");
        JestResult result = client.createNewElasticSearchResult(jsonString, statusLine, "INDEX", "");
        assertNotNull(result);
        assertFalse(result.isSucceeded());
        assertEquals("Invalid index", result.getErrorMessage());
    }

    @Test
    public void getSuccessDeleteResult() {
        String jsonString = "{\n" +
                "    \"ok\" : true,\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\",\n" +
                "    \"found\" : true\n" +
                "}\n";
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "");
        JestResult result = client.createNewElasticSearchResult(jsonString, statusLine, "DELETE", "");
        assertNotNull(result);
        assertTrue(result.isSucceeded());
    }

    @Test
    public void getFailedDeleteResult() {
        String jsonString = "{\n" +
                "    \"ok\" : true,\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\",\n" +
                "    \"found\" : false\n" +
                "}\n";
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "");
        JestResult result = client.createNewElasticSearchResult(jsonString, statusLine, "DELETE", "");
        assertNotNull(result);
        assertFalse(result.isSucceeded());
    }

    @Test
    public void getSuccessGetResult() {
        String jsonString = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\", \n" +
                "    \"_source\" : {\n" +
                "        \"user\" : \"kimchy\",\n" +
                "        \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "");
        JestResult result = client.createNewElasticSearchResult(jsonString, statusLine, "GET", "_source");
        assertNotNull(result);
        assertTrue(result.isSucceeded());
    }


    @Test
    public void isOperationSucceedWithTrueIndex() {
        Map jsonMap = new HashMap();
        jsonMap.put("ok", true);
        assertTrue(client.isOperationSucceed(jsonMap, "INDEX"));
    }

    @Test
    public void isOperationSucceedWithFalseIndex() {
        Map jsonMap = new HashMap();
        jsonMap.put("ok", false);
        assertFalse(client.isOperationSucceed(jsonMap, "INDEX"));
    }

    @Test
    public void isOperationSucceedWithUnExpectedIndexResult() {
        Map jsonMap = new HashMap();
        assertTrue(client.isOperationSucceed(jsonMap, "INDEX"));
    }

    @Test
    public void isOperationSucceedWithDelete() {
        Map jsonMap = new HashMap();
        jsonMap.put("ok", true);
        jsonMap.put("found", true);
        assertTrue(client.isOperationSucceed(jsonMap, "DELETE"));
    }

    @Test
    public void isOperationSucceedWithUnFoundDelete() {
        Map jsonMap = new HashMap();
        jsonMap.put("ok", true);
        jsonMap.put("found", false);
        assertFalse(client.isOperationSucceed(jsonMap, "DELETE"));
    }

    @Test
    public void isOperationSucceedWithUnExpectedDelete() {
        Map jsonMap = new HashMap();
        jsonMap.put("ok", true);
        assertTrue(client.isOperationSucceed(jsonMap, "DELETE"));
    }

    @Test
    public void isOperationSucceedWithWrongDelete() {
        Map jsonMap = new HashMap();
        assertTrue(client.isOperationSucceed(jsonMap, "DELETE"));
    }

    @Test
    public void isOperationSucceedWithUpdate() {
        Map jsonMap = new HashMap();
        jsonMap.put("ok", true);
        assertTrue(client.isOperationSucceed(jsonMap, "Update"));
    }

    @Test
    public void isOperationSucceedWithUnExpectedUpdate() {
        Map jsonMap = new HashMap();
        assertTrue(client.isOperationSucceed(jsonMap, "Update"));
    }

    @Test
    public void isOperationSucceedWithGet() {
        Map jsonMap = new HashMap();
        jsonMap.put("exists", true);
        assertTrue(client.isOperationSucceed(jsonMap, "GET"));
    }

    @Test
    public void isOperationSucceedWithFalseGet() {
        Map jsonMap = new HashMap();
        jsonMap.put("exists", false);
        assertFalse(client.isOperationSucceed(jsonMap, "GET"));
    }

    @Test
    public void extractDocumentsFromResponseForSearchRequest() {
        String searchResult = "{\n" +
                "    \"_shards\":{\n" +
                "        \"total\" : 5,\n" +
                "        \"successful\" : 5,\n" +
                "        \"failed\" : 0\n" +
                "    },\n" +
                "    \"hits\":{\n" +
                "        \"total\" : 1,\n" +
                "        \"hits\" : [\n" +
                "            {\n" +
                "                \"_index\" : \"twitter\",\n" +
                "                \"_type\" : \"tweet\",\n" +
                "                \"_id\" : \"1\", \n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elastic Search\"\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
    }

    @Test
    public void getRequestURL() {
        String requestURI = "twitter/tweet/1";
        String elasticSearchServer = "http://localhost:9200";
        assertEquals("http://localhost:9200/twitter/tweet/1", client.getRequestURL(elasticSearchServer, requestURI));
    }

	@Test
	public void testGetElasticSearchServer() throws Exception {
		JestHttpClient client = new JestHttpClient();
		LinkedHashSet<String> set=new LinkedHashSet<String>();
		set.add("http://localhost:9200");
		set.add("http://localhost:9300");
		set.add("http://localhost:9400");
		client.setServers(set);

		Set<String> serverList=new HashSet<String>();

		for(int i = 0; i <3;i++) {
			serverList.add(client.getElasticSearchServer());
		}

		assertEquals("round robin does not work",3,serverList.size());

		assertTrue(set.contains("http://localhost:9200"));
		assertTrue(set.contains("http://localhost:9300"));
		assertTrue(set.contains("http://localhost:9400"));
	}
}
