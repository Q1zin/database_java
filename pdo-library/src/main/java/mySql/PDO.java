package mySql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mySql.commands.Command;
import mySql.dataBase.Database;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDO {
    private Map<String, Database> listDB = new HashMap<>();
    private static boolean isErase = false;
    public static String PATH_DB = "/Users/mir/Documents/database_java/db/";
    public static String RESILT_SQL = "";

    public PDO() {}

    public void executeSQL(String sql, String dbName) {
        Database db = loadDB(dbName);
        isErase = sql.split(" ")[0].equals("ERASE");
        RESILT_SQL = "";
        Command commandObj = CommandFactory.createCommand(sql);
        if (commandObj == null) {
            System.out.println("Неизвестная команда: " + sql);
            return;
        }

        commandObj.execute(db);

        writeDB(db);

        updateListDB(db);

        System.out.println("__________________________________________");
        System.out.println(sql + " выполнено успешно.");
        System.out.println("Результат: " + RESILT_SQL);
        System.out.println("__________________________________________");
    }

    Database loadDB(String dbName) {
        if (dbName == null) return new Database();
        if (listDB.containsKey(dbName)) return listDB.get(dbName);

        Database db = null;

        ObjectMapper mapper = new ObjectMapper();
        try {
            db = mapper.readValue(new File(PATH_DB + dbName + ".json"), Database.class);
            return db;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Ошибка загрузки базы данных: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка загрузки базы данных: " + e.getMessage());
        }
    }

    void writeDB(Database db) {
        System.out.println("Куда: " + PATH_DB + db.getName() + ".json");
        try {
            new ObjectMapper().writeValue(new File(PATH_DB + db.getName() + ".json"), db);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        printJSONme(db);
    }

    private void updateListDB(Database db) {
        if (isErase) {
            listDB.remove(db.getName());
            return;
        }

        listDB.put(db.getName(), db);
    }

    void printJSONme(Database db) {
        try {
            String prettyJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(db);
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getResiltSql() {
        return RESILT_SQL;
    }

    public void setPathDb(String pathDb) {
        if (pathDb == null) return;
        if (pathDb.charAt(pathDb.length() - 1) != '/') {
            pathDb += "/";
        }
        PATH_DB = pathDb;
    }
}
