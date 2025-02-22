package db.newSql.dataBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    public void addData(List<Map<String, Object>> data) {
        this.data.addAll(data);
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

    @JsonIgnore
    public int getMinArgs() {
        int minColumns = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getFlags().contains("not-null")) {
                minColumns = i;
            }
        }

        return minColumns == -1 ? 0 : minColumns + 1;
    }
}
