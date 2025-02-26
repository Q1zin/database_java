package mySql.commands;

import mySql.dataBase.Database;

import static mySql.PDO.PATH_DB;

import java.io.File;
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
        validate_data(db);
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

    private void validate_data(Database db) {
        File file = new File(PATH_DB + databaseName + ".json");
        if (file.exists()) {
            throw new IllegalArgumentException("База данных с таким именем уже существует!");
        }
    }

    private void do_request(Database db) {
        db.setName(databaseName);
    }
}
