package db.newSql.commands;

import db.newSql.dataBase.Database;

public interface Command {
    void execute(Database db);
}