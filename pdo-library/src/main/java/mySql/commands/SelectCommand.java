package mySql.commands;

import mySql.PDO;
import mySql.dataBase.Database;
import mySql.dataBase.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandName("SELECT")
public class SelectCommand extends AbstractCommand {
    private String tableName;
    private String conditionString;
    private List<String> conditionList = new ArrayList<>();
    private String fieldsString;
    private List<String> fieldList = new ArrayList<>();
    private boolean allFields = false;

    List<Map<String, Object>> listSelected;

    public SelectCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validate_sql();
        parse_data();
        validate_data(db);
        do_request(db);
    }

    private void validate_sql() {
        Pattern tablePattern = Pattern.compile("SELECT (\\*|[a-zA-Z_,]+) FROM ([a-zA-Z_][a-zA-Z0-9_]*)(?: WHERE (.+))?");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        fieldsString = tableMatcher.group(1);
        tableName = tableMatcher.group(2);
        conditionString = tableMatcher.group(3);
    }

    private void parse_data() {
        if (fieldsString.equals("*")) {
            allFields = true;
        }

        fieldList = List.of(fieldsString.split(","));

        if (conditionString == null || conditionString.isEmpty()) {
            return;
        }

        Pattern conditionPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*(>=|<=|!=|=|>|<)\\s*(\\\"[^\\\"]*\\\"|\\[(?:[^\\[\\]]*?)\\]|[a-zA-Z0-9_\\-]+)"); // мб когда-нибудь реализую OR, !=, <, ...
        Matcher conditionMatcher = conditionPattern.matcher(conditionString);

        while (conditionMatcher.find()) {
            conditionList.add(conditionMatcher.group(1) + conditionMatcher.group(2) +  conditionMatcher.group(3));
        }
    }

    private void validate_data(Database db) {
        if (!db.containsTable(tableName)) {
            throw new IllegalArgumentException("Таблицы с таким именем нету");
        }

        Table table = db.getTable(tableName);

        if (!allFields) {
            for (String col : fieldList) {
                if (!table.hasColumn(col)) {
                    throw new IllegalArgumentException("Нету такого поля");
                }
            }
        }

        listSelected = table.getWhereData(conditionList);
    }

    private void do_request(Database db) {
        if (allFields) {
            PDO.RESILT_SQL = listSelected.toString();
            return;
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (var row : listSelected) {
            Map<String, Object> newRow = new HashMap<>();
            for (String col : fieldList) {
                if (row.containsKey(col)) {
                    newRow.put(col, row.get(col));
                }
            }
            if (!newRow.isEmpty()) {
                result.add(newRow);
            }
        }
        PDO.RESILT_SQL = result.toString();
    }
}