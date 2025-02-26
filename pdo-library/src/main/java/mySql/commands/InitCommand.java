package mySql.commands;

import mySql.dataBase.Database;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

@CommandName("INIT")
public class InitCommand extends AbstractCommand {
    private String databaseName;

    public InitCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validate_sql();
        do_request(db);
    }

    private void validate_sql() {
        Pattern tablePattern = Pattern.compile("^INIT DATABASE ([a-zA-Z_][a-zA-Z0-9_]*)$");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        databaseName = tableMatcher.group(1);
    }

    private void do_request(Database db) {
        db.setName(databaseName);
    }
}
