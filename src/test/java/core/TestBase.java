package core;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adrian on 22/05/2016.
 */
public class TestBase {

    public JSONObject getJSONFromMVCResult(MvcResult mvcResult) throws Exception {

        String resposeBody = mvcResult.getResponse().getContentAsString();

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(resposeBody);
        JSONObject o = (JSONObject) obj;

        return o;
    }


}
