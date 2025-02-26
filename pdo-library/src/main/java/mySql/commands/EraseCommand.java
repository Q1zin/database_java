package mySql.commands;

import mySql.dataBase.Database;
import static mySql.PDO.PATH_DB;

import java.io.File;
import java.util.regex.*;

@CommandName("ERASE")
public class EraseCommand extends AbstractCommand {
    private String dbName;
    File file;

    public EraseCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validate_sql();
        validate_data(db);
        do_request(db);
    }

    private void validate_sql() {
        Pattern databasePattern = Pattern.compile("^ERASE DATABASE ([a-zA-Z_][a-zA-Z0-9_]*)$");
        Matcher databaseMatcher = databasePattern.matcher(sql);

        if (!databaseMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        dbName = databaseMatcher.group(1);
    }

    private void validate_data(Database db) {
        file = new File(PATH_DB + dbName + ".json");
        if (!file.exists()) {
            throw new IllegalArgumentException("Базы данных с таким именем нету");
        }
    }

    private void do_request(Database db) {
        file.delete();
    }
}
