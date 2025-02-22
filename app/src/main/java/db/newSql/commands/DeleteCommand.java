package db.newSql.commands;

import db.newSql.dataBase.Database;
import db.newSql.dataBase.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandName("DELETE")
public class DeleteCommand extends AbstractCommand {
    private String tableName;
    private String conditionString;
    private List<String> conditionList = new ArrayList<>();
    List<Map<String, Object>> listToDeleted;

    public DeleteCommand(String sql) {
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
        Pattern tablePattern = Pattern.compile("^DELETE FROM ([a-zA-Z_][a-zA-Z0-9_]*) WHERE (.+)$");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        tableName = tableMatcher.group(1);
        conditionString = tableMatcher.group(2);
    }

    private void parse_data() {
        if (conditionString.isEmpty()) {
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
        listToDeleted = table.getWhereData(conditionList);
    }

    private void do_request(Database db) {
        db.getTable(tableName).getData().removeIf(listToDeleted::contains);
    }
}