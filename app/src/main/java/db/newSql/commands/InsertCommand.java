package db.newSql.commands;

import db.newSql.dataBase.Column;
import db.newSql.dataBase.Database;
import db.newSql.dataBase.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandName("INSERT")
public class InsertCommand extends AbstractCommand {
    private String tableName;
    private String stringData;
    private List<List> dataStringList = new ArrayList<>();
    private List<Map<String, Object>> dataList = new ArrayList<>();


    public InsertCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validate_sql();
        parse_data();
        validate_data(db);
        do_request(db);
    }

    private void validate_sql() {
        Pattern inserPattern = Pattern.compile("^INSERT INTO ([a-zA-Z_][a-zA-Z0-9_]*) \\((.*)\\)$");
        Matcher insertMatcher = inserPattern.matcher(sql);

        if (!insertMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        tableName = insertMatcher.group(1);
        stringData = insertMatcher.group(2);

        if (!stringData.trim().startsWith("(") || !stringData.trim().endsWith(")")) {
            stringData = "(" + stringData + ")";
        }
    }

    public void parse_data() {
        if (stringData.isEmpty()) {
            return;
        }

        Pattern groupPattern = Pattern.compile("\\((.*?)\\)");
        Matcher groupMatcher = groupPattern.matcher(stringData);

        while (groupMatcher.find()) {
            String data = groupMatcher.group(1);
            dataStringList.add(List.of(data.split(", ")));
        }
    }

    private void validate_data(Database db) {
        if (!db.containsTable(tableName)) {
            throw new IllegalArgumentException("Таблицы с таким именем нету");
        }

        Table table = db.getTable(tableName);
        List<Column> columns = table.getColumns();
        int minArgs = table.getMinArgs();

        for (var dataCollection : dataStringList) {
            if (dataCollection.size() < minArgs) {
                throw new IllegalArgumentException("Недостаточно аргументов!");
            }

            Map<String, Object> data = new HashMap<>();

            for (int i = 0; i < dataCollection.size(); i++) {
                Column col = columns.get(i);
                String typeString = col.getType();

                if (dataCollection.get(i).toString().isEmpty()) {
                    throw new IllegalArgumentException("Неверный формат данных!");
                }

                Object value;
                try {
                    value = switch (typeString) {
                        case "int" -> Integer.parseInt(dataCollection.get(i).toString());
                        case "string", "date", "[]strings" -> dataCollection.get(i).toString();
                        case "boolean" -> Boolean.parseBoolean(dataCollection.get(i).toString());
                        default -> throw new IllegalArgumentException("Неизвестный тип данных в базе данных");
                    };
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Неверный формат данных! Ошибка преобразований");
                };

                data.put(col.getName(), value);
            }

            dataList.add(data);
        }

    }
    private void do_request(Database db) {
        db.getTable(tableName).addData(dataList);
    }
}