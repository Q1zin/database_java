package db.newSql;

import db.newSql.commands.Command;
import db.newSql.PDO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.crypto.Data;

public class App {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java --jar database.jar --db <database-file> -c <sql-command>");
            return;
        }

        String dbName = "";
        String sqlCommand = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--db") && i + 1 < args.length) {
                dbName = args[i + 1];
            } else if (args[i].equals("-c") && i + 1 < args.length) {
                sqlCommand = args[i + 1];
            }
        }

        if (sqlCommand == null ||
                dbName.isEmpty() && !(sqlCommand.split(" ")[0].equals("INIT") ||
            sqlCommand.split(" ")[0].equals("ERASE"))) {
            System.out.println("Error: Missing --db or -c parameter.");
            System.out.println("Usage: java --jar database.jar --db <database-file> -c <sql-command>");
            return;
        }

        PDO pdo = new PDO();

        File dbFile = new File(dbName);
        String dbPath = dbFile.getParentFile().getParent();
        String dbFileName = dbFile.getName();
        String dbBaseName = dbFileName.substring(0, dbFileName.lastIndexOf('.'));

        pdo.setPathDb(dbPath);

        pdo.executeSQL(sqlCommand, dbBaseName);

        if (sqlCommand.split(" ")[0].equals("SELECT")) {
            System.out.println(pdo.getResiltSql());
        }
    }


}
