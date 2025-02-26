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
import java.util.Set;

public class PDO {
    public static String PATH_DB = "";
    public static String RESILT_SQL = "";

    public PDO() {}

    public void executeSQL(String sql, String dbName) {
        boolean isErase = sql.split(" ")[0].equals("ERASE");
        boolean isInit = sql.split(" ")[0].equals("INIT");

        Database db = (isErase || isInit) ? loadDB(null) : loadDB(dbName);
        
        RESILT_SQL = "";
        Command commandObj = CommandFactory.createCommand(sql);
        if (commandObj == null) {
            throw new IllegalArgumentException("Неизвестная команда: " + sql);
        }

        commandObj.execute(db);

        if (!isErase)
            writeDB(db);


        System.out.println("sql: " + sql);
        System.out.println("__________________________________________");
        System.out.println(sql + " выполнено успешно.");
        System.out.println("Результат: " + RESILT_SQL);
        System.out.println("__________________________________________");
    }

    private Database loadDB(String dbName) {
        if (dbName == null) return new Database();

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

    private void writeDB(Database db) {
        try {
            new ObjectMapper().writeValue(new File(PATH_DB + db.getName() + ".json"), db);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        printJSONme(db);
    }

    public String getResiltSql() {
        return RESILT_SQL;
    }

    public Set<String> getSetTables(String nameDB) {
        Database db = loadDB(nameDB);

        return db.getSetTables();
    }

    public void setPathDb(String pathDb) {
        if (pathDb == null) return;
        if (pathDb.charAt(pathDb.length() - 1) != '/') {
            pathDb += "/";
        }
        PATH_DB = pathDb;
    }

    public void createDB(String nameDB) {
        File file = new File(PATH_DB + nameDB + ".json");

        if (file.exists() && file.isFile()) {
            throw new IllegalArgumentException("База данных с именем " + nameDB + " уже существует");
        }

        writeDB(new Database(nameDB));
    }

    private void printJSONme(Database db) {
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
