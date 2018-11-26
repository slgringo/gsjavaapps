package com.company.parts;

import com.company.parts.model.DataEntity;
import com.company.parts.model.DataProvider;
import com.company.parts.model.DataTypes;
import com.company.parts.model.FieldMetadata;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Orefin
 *         Date: 23.11.18.
 *         Time: 16:02
 *         POST requests handler
 */
class RequestResolver {
    private static final Map<String, DataEntity> metadata = new HashMap<>();

    //adding metadata for table part_entity
    static {
        Map<String, FieldMetadata> fields = new HashMap<>();
        fields.put("part_name", new FieldMetadata("part_name", DataTypes.TEXT));
        fields.put("part_number", new FieldMetadata("part_number", DataTypes.TEXT));
        fields.put("vendor", new FieldMetadata("vendor", DataTypes.TEXT));
        fields.put("qty", new FieldMetadata("qty", DataTypes.INT));
        fields.put("shipped", new FieldMetadata("shipped", DataTypes.DATE));
        fields.put("receive", new FieldMetadata("receive", DataTypes.DATE));
        DataEntity partEntity = new DataEntity("part_entity", fields);
        metadata.put(partEntity.getTableName(), partEntity);
    }

    /**
     * HTTP request processing
     * @param request HTTP request
     * @return JSON string with table data
     */
    static String resolve(HttpServletRequest request) {
        JSONArray jsonArray;
        String params = request.getParameterNames().nextElement();
        Map<String, String> filters = null;
        String tableName = null;
        String sortParam = null;
        Boolean sortAsc = null;
        int offset = 0;
        int pageSize = 0;
        if (params != null && !params.isEmpty()) {
            try {
                JSONObject jsParams = new JSONObject(params);
                if (jsParams.has("filter")) { //parsing filter conditions
                    filters = new HashMap<>();
                    JSONObject jsFilter = jsParams.getJSONObject("filter");
                    Iterator keysIterator =  jsFilter.keys();
                    while (keysIterator.hasNext()) {
                        String paramName = (String) keysIterator.next();
                        String paramValue = jsFilter.getString(paramName);
                        filters.put(paramName, paramValue);
                    }

                }
                if (jsParams.has("table")) { //parsing table name
                    tableName = jsParams.getString("table");
                }
                if (jsParams.has("sorting")) {//parsing sort params
                    String sp = jsParams.getString("sorting");
                    Matcher matcher = DataProvider.pattern.matcher(sp);
                    String direction;
                    String fieldName;
                    if (matcher.find()) {
                        direction = matcher.group();
                        fieldName = sp.replace(direction, "");
                        sortParam = fieldName;
                        sortAsc = "[ASC]".equals(direction);
                    }
                }
                if (jsParams.has("page")) {//parsing paging params
                    JSONObject paging = jsParams.getJSONObject("page");
                    offset = paging.getInt("OFFSET");
                    pageSize = paging.getInt("LIMIT");
                }
            } catch (JSONException e) {
                return "{\"error\":\"Wrong filter params\"}";
            }
        }
        if (tableName == null) {
            return "{\"error\":\"Wrong reqest. Table not specified.\"}";
        }
        DataProvider dataProvider = new DataProvider(tableName, metadata.get(tableName).getFields());
        jsonArray = dataProvider.getData(sortParam, sortAsc, filters, offset, pageSize);
        return jsonArray != null ? jsonArray.toString() : "{\"error\":\"Server DataProvider JSON fault\"}";
    }
}
