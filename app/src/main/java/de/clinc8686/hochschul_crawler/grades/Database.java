package de.clinc8686.hochschul_crawler.grades;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Database {
    private SQLiteDatabase sqlgrade;
    public Database(Context context) {
        this.sqlgrade = (context.getApplicationContext().openOrCreateDatabase("HochschulCrawlerGrades", Context.MODE_PRIVATE, null));
    }

    public boolean insertData(String semester, String modulNumber, String modul, String pass, String grade, Context context) {
        sqlgrade.execSQL("CREATE TABLE IF NOT EXISTS Grades(ID INTEGER PRIMARY KEY AUTOINCREMENT, SEMESTER TINYTEXT NOT NULL, MODULNUMBER TINYTEXT NOT NULL, MODUL TINYTEXT NOT NULL, PASS TINYTEXT NOT NULL, GRADE TINYTEXT);");
        Cursor resultSet = sqlgrade.rawQuery("Select * from Grades WHERE SEMESTER = \'" + semester + "\' and MODUL = \'" + modul + "\' and MODULNUMBER = \'" + modulNumber + "\'", null);

        if (!resultSet.moveToFirst()) {
            sqlgrade.execSQL("INSERT INTO Grades (SEMESTER, MODULNUMBER, MODUL, PASS, GRADE) VALUES(\"" + semester + "\", \"" + modulNumber + "\", \"" + modul + "\", \"" + pass + "\", \"" + grade + "\");");
            resultSet.close();
            return true;
        } else {
            @SuppressLint("Range")
            String databasegrade = resultSet.getString(resultSet.getColumnIndex("GRADE"));
            if (databasegrade.equals(grade)) {
                resultSet.close();
                return false;
            } else {
                sqlgrade.execSQL("UPDATE Grades SET GRADE = \'" + grade + "\' WHERE SEMESTER = \'" + semester + "\' and MODUL = \'" + modul + "\' and MODULNUMBER = \'" + modulNumber + "\'");
                resultSet.close();
                return true;
            }
        }
    }

    @SuppressLint("Range")
    public ArrayList<ModulInfo> selectData(Context context) {
        ArrayList<ModulInfo> arraylist = new ArrayList<ModulInfo>();
        @SuppressLint("Recycle")
        Cursor cursor = sqlgrade.rawQuery( "SELECT * FROM Grades;", null );
        if(cursor!=null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    arraylist.add(new ModulInfo(cursor.getString(cursor.getColumnIndex("SEMESTER")),
                            cursor.getString(cursor.getColumnIndex("MODULNUMBER")),
                            cursor.getString(cursor.getColumnIndex("MODUL")),
                            cursor.getString(cursor.getColumnIndex("PASS")),
                            cursor.getString(cursor.getColumnIndex("GRADE"))));
                } while (cursor.moveToNext());
                cursor.close();
            }

        }
        return arraylist;
    }

    public void dropTable()  {
        sqlgrade.execSQL("DROP TABLE IF EXISTS Grades");
    }
}
