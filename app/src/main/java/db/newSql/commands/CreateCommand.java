package db.newSql.commands;

import db.newSql.dataBase.Column;
import db.newSql.dataBase.Database;
import db.newSql.dataBase.Table;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

@CommandName("CREATE")
public class CreateCommand extends AbstractCommand {
    private String tableName;
    private List<Column> columnsList = new ArrayList<>();

    private String columnsString;

    public CreateCommand(String sql) {
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
        Pattern tablePattern = Pattern.compile("^CREATE TABLE ([a-zA-Z_][a-zA-Z0-9_]*) \\((.*)\\)$");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        tableName = tableMatcher.group(1);
        columnsString = tableMatcher.group(2);
    }

    private void parse_data() {
        if (columnsString.trim().isEmpty()) { return; }
        String[] columnsArray = columnsString.trim().split(";\\s*");
        Pattern columnPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*) (int|string|date|boolean|\\[\\]strings)(?: (unique|not-null))?(?: (unique|not-null))?");

        for (String column : columnsArray) {
            Column newColumn = getColumn(column, columnPattern);

            columnsList.add(newColumn);
        }
    }

    private static Column getColumn(String column, Pattern columnPattern) {
        Matcher columnMatcher = columnPattern.matcher(column.trim());

        if (!columnMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат столбца: " + column);
        }


        List<String> attributes = new ArrayList<>();
        for (int i = 3; i <= columnMatcher.groupCount(); i++) {
            if (columnMatcher.group(i) != null) {
                attributes.add(columnMatcher.group(i));
            }
        }

        Column newColumn = new Column(columnMatcher.group(1), columnMatcher.group(2), attributes);
        return newColumn;
    }

    private void validate_data(Database db) {
        if (db.containsTable(tableName)) {
            throw new IllegalArgumentException("Таблица с таким именем уже есть");
        }
    }

    private void do_request(Database db) {
        Table newTable = new Table(tableName);
        for (Column column : columnsList) {
            newTable.addColumn(column);
        }
        db.addTable(newTable);
    }
}
