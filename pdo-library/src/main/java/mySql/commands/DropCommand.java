package mySql.commands;

import mySql.dataBase.Database;

import java.util.regex.*;

@CommandName("DROP")
public class DropCommand extends AbstractCommand {
    private String tableName;

    public DropCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validate_sql();
        validate_data(db);
        do_request(db);
    }

    private void validate_sql() {
        Pattern tablePattern = Pattern.compile("^DROP TABLE ([a-zA-Z_][a-zA-Z0-9_]*)$");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        tableName = tableMatcher.group(1);
    }

    private void validate_data(Database db) {
        if (!db.containsTable(tableName)) {
            throw new IllegalArgumentException("Таблица с таким именем нету");
        }
    }

    private void do_request(Database db) {
        db.rmTable(tableName);
    }
}
