package db.newSql.dataBase;

import java.util.*;

public class Database {
    private String name;
    private final Map<String, Table> tables = new HashMap<>();

    public Database() {}

    public void addTable(Table table) {
        tables.put(table.getName(), table);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Table getTable(String name) {
        return tables.get(name);
    }

    public boolean containsTable(String tableName) {
        return tables.containsKey(tableName);
    }

    public void rmTable(String nameTable) {
        tables.remove(nameTable);
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public void setTables(Map<String, Table> tables) {
        this.tables.clear();
        this.tables.putAll(tables);
    }
}

