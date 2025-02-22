package db.newSql.commands;

import db.newSql.dataBase.Database;

public abstract class AbstractCommand implements Command {
    String sql;

    public AbstractCommand(String sql) {
        this.sql = sql;
    }

    public abstract void execute(Database db);
}