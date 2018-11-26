package com.company.parts.model;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Orefin
 *         Date: 23.11.18.
 *         Time: 12:45
 *         Class for loading data from database
 */
public class DataProvider {
    private static final String url = "jdbc:postgresql://127.0.0.1:5432/postgres";
    private static final String user = "postgres";
    private static final String password = "postgres";
    public static Pattern pattern = Pattern.compile("\\[.*\\]");
    private DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

    private String tableName;
    private Map<String, FieldMetadata> fields;
    private String fieldsClause;

    public DataProvider(String tableName, Map<String, FieldMetadata> fields) {
        this.tableName = tableName;
        this.fields = fields;
        StringBuilder sb = new StringBuilder();
        fields.keySet().forEach(s -> sb.append(s).append(", "));
        sb.delete(sb.length() - 2, sb.length() -1);
        fieldsClause = sb.toString();
    }

    /**
     * Get data from DB
     * @param sortField fieldname for sorting
     * @param asc sort direction, ASC if true
     * @param filters applied filters
     * @param offset offset of selected page
     * @param pageSize size of selected page
     * @return JSON array with table data
     */
    public JSONArray getData(String sortField, Boolean asc, Map<String, String> filters, int offset, int pageSize) {
        JSONArray result = new JSONArray();
        try {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                return new JSONArray("[{\"error\":\"Postgres Driver class not found\"}]");
            }

            Map<String, DataTypes> fieldTypes = new HashMap<>();
            String query = getStatement(sortField, asc, filters, fieldTypes, offset, pageSize);

            Connection connection;
            try {
                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                return new JSONArray("[{\"error\":\"Connection to DB failed\"}]");
            }

            ResultSet resultSet;
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                if (filters != null) {
                    int i = 1;
                    for (Map.Entry<String, String>  entry : filters.entrySet()) {
                        DataTypes fieldType = fieldTypes.get(entry.getKey());
                        switch (fieldType) {
                            case INT:
                                statement.setInt(i, Integer.parseInt(entry.getValue()));
                                break;
                            case TEXT:
                                statement.setString(i, "%" + entry.getValue() + "%");
                                break;
                            case DATE:
                                try {
                                    Date date = new Date(dateFormat.parse(entry.getValue()).getTime());
                                    statement.setDate(i, date);
                                } catch (ParseException e) {
                                    return new JSONArray("[{\"error\":\"incorrect date format\"}]");
                                }
                                break;
                            default:
                                statement.setString(i, entry.getValue());
                        }
                        i++;
                    }
                }
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    JSONObject item = new JSONObject();
                    for (FieldMetadata field : fields.values()) {
                        String value = resultSet.getString(field.getName());
                        item.put(field.getName(), value);
                    }
                    result.put(item);
                }
            } catch (SQLException e) {
                return new JSONArray("[{\"error\":\"SQL error\"}]");
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException connectionCloseException) {
                        return new JSONArray("[{\"error\":\"connectionCloseException\"}]");
                    }
                }
            }

        } catch(JSONException jsException) {
            return null;
        }
        return result;
    }

    /**
     * Constuct SQL query
     * @param sortField fieldname for sorting
     * @param asc sort direction, ASC if true
     * @param filters applied filters
     * @param fieldTypes map with fields description
     * @param offset offset of selected page
     * @param pageSize size of selected page
     * @return parametrized SQL query
     */
    private String getStatement(String sortField, Boolean asc, Map<String, String> filters,
                                Map<String, DataTypes> fieldTypes, int offset, int pageSize) {
        String filterClause = "";
        StringBuilder sb = new StringBuilder();
        if (filters != null) {
            for (String key : filters.keySet()) {
                if (sb.length() != 0) {
                    sb.append(" AND ");
                }
                sb.append(getFilterCondition(key, fieldTypes));
            }
            if (filters.size() > 0)
                filterClause = " where " + sb.toString();
        }
        String sortClause = "";
        if (sortField != null && asc != null && !sortField.isEmpty()) {
            sortClause = " ORDER BY " + sortField + " " + (asc ? "ASC" : "DESC");
        }
        String limitClause = pageSize == 0 ? "" : String.format(" LIMIT %d OFFSET %d ", pageSize, offset);
        return "select " + fieldsClause + " from " + tableName + filterClause + sortClause + limitClause + ";";
    }

    /**
     * Parses filter condition for a field
     * @param param string in [filtecondition]filtername format
     * @param fieldTypes map with fields description
     * @return filter condition for SQL query
     */
    private String getFilterCondition(String param, Map<String, DataTypes> fieldTypes) {
        Matcher matcher = pattern.matcher(param);
        String condition = " = ";
        String fieldName = "";
        if (matcher.find()) {
            String con = matcher.group();
            fieldName = param.replace(con, "");
            fieldTypes.put(param, fields.get(fieldName).getType());
            switch (con) {
                case "[moreeq]":
                    condition = " >= ";
                    break;
                case "[lesseq]":
                    condition = " <= ";
                    break;
                case "[like]":
                    condition = " LIKE ";
            }
        }
        return fieldName + condition + "? ";
    }

}
