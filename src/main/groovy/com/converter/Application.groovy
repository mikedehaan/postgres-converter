package com.converter

import java.util.regex.Matcher

/**
 * Created by mike on 10/8/2016.
 */
class Application {
    public static void main(String[] args) {
        String source = Application.getClass().getResource( '/source.txt' ).text

        String className = "tablename";
        source.eachLine {
            Matcher classMatcher = it =~ /(?im)class\s+([^\s{]+)/
            if (classMatcher.find()) {
                className = classMatcher.group(1);
            }
        }

        String tableName = convertName(className);

        println("CREATE TABLE ${tableName}\n("
        );

        def firstLine = true;
        source.eachLine {
            //println("File Line: " + it);

            if (firstLine) {
                firstLine = false;
                return;
            }

            Matcher regexMatcher = it =~ /(?im)([^\s]+)\s+([^\s;]+);?/
            if (regexMatcher.find()) {

                String dataType = regexMatcher.group(1);
                String fieldName = regexMatcher.group(2);

                String postgresFieldName = convertName(fieldName);

                println("  " + postgresFieldName.padRight(20) + "    " + convertDataType(postgresFieldName, dataType) + ",");
            }
        }

        println("  PRIMARY KEY (id)\n" +
                ")\n" +
                "WITH (\n" +
                "OIDS = FALSE\n" +
                ")\n" +
                ";"
        );

        println();
        println("Table Mapping:\n" +
                "    static mapping = {\n" +
                "        table '${tableName}'\n" +
                "        id generator: 'native', params: [sequence: '${tableName}_id_seq']\n" +
                "        version false\n" +
                "    }"
        );
    }

    public static String convertName(String name) {
        def result = []
        String current = "";
        def chars = name.getChars();
        for (int i = 0; i < chars.size(); i++) {
            if (chars[i].isLowerCase() || current.size() == 0) {
                current += chars[i].toLowerCase();
            } else {
                result.add(current);
                current = chars[i].toLowerCase();
            }
        }

        result.add(current);

        return result.join("_");
    }

    public static String convertDataType(final String postgresFieldName, String dataType) {
        switch (dataType) {
            case "ObjectId":
                if (postgresFieldName == "id") {
                    return "BIGSERIAL";
                } else {
                    return "BIGINT";
                }
            case "String":
                return "TEXT"
            case "Date":
                return "TIMESTAMP"
            case "Boolean":
                return "BOOLEAN"
            case "boolean":
                return "BOOLEAN"
            case "Integer":
                return "INTEGER"
            case "int":
                return "INTEGER"
            case "Long":
                if (postgresFieldName == "id") {
                    return "BIGSERIAL";
                } else {
                    return "BIGINT";
                }
            case "long":
                if (postgresFieldName == "id") {
                    return "BIGSERIAL";
                } else {
                    return "BIGINT";
                }
            case "BigDecimal":
                return "NUMERIC"

            default:
                throw new Exception("Unknown dataType: " + dataType);
        }

    }
}
