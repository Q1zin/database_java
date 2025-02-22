package db.newSql.commands;

import db.newSql.dataBase.Database;

@CommandName("SELECT")
public class SelectCommand extends AbstractCommand {

    public SelectCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        System.out.println("Ещё не сделал!");
        validate_sql();
        parse_data();
        validate_data(db);
        do_request(db);
    }

    private void validate_sql() {

    }

    private void parse_data() {

    }

    private void validate_data(Database db) {

    }
    private void do_request(Database db) {

    }
}