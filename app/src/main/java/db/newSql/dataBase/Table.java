package db.newSql.dataBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table {
    private String name;
    private List<Column> columns = new ArrayList<>();
    private List<Map<String, Object>> data = new ArrayList<>();

    public Table() {}

    public Table(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void insertData(Map<String, Object> rowData) {
        data.add(rowData);
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }
}
