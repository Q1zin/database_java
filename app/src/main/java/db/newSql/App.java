package db.newSql;

import db.newSql.commands.Command;
import db.newSql.dataBase.Database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static final String PATH_DB = "db/";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java --jar database.jar --db <database-file> -c <sql-command>");
            return;
        }

        String dbFile = null;
        String sqlCommand = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--db") && i + 1 < args.length) {
                dbFile = args[i + 1];
            } else if (args[i].equals("-c") && i + 1 < args.length) {
                sqlCommand = args[i + 1];
            }
        }

        if (sqlCommand == null ||
                dbFile == null && !(sqlCommand.split(" ")[0].equals("INIT") || sqlCommand.split(" ")[0].equals("ERASE"))) {
            System.out.println("Error: Missing --db or -c parameter.");
            System.out.println("Usage: java --jar database.jar --db <database-file> -c <sql-command>");
            return;
        }

        Database db;
        if (dbFile != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                db = mapper.readValue(new File(dbFile), Database.class);
                System.out.println("Database name: " + db.getName());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else {
            db = new Database();
        }

        printJSONme(db);

        Command commandObj = CommandFactory.createCommand(sqlCommand);
        if (commandObj == null) {
            System.out.println("Неизвестная команда: " + sqlCommand);
            return;
        }

        commandObj.execute(db);

        printJSONme(db);

        if (sqlCommand.split(" ")[0].equals("ERASE")) return;

        try {
            new ObjectMapper().writeValue(new File(PATH_DB + db.getName() + ".json"), db);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printJSONme(Database db) {
        try {
            String prettyJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(db);
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
