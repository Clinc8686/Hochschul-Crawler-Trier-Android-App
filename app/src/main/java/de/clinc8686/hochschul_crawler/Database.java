package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {
    private SQLiteDatabase sqlgrade;
    Database(Context context) {
        super(context, "Grades", null, 1);
        this.sqlgrade = (context.getApplicationContext().openOrCreateDatabase("HochschulCrawlerGrades", Context.MODE_PRIVATE, null));
    }

    public boolean insertData(String semester, String modulNumber, String modul, String pass, String grade, Context context) {
        sqlgrade.execSQL("CREATE TABLE IF NOT EXISTS Grades(ID INTEGER PRIMARY KEY AUTOINCREMENT, SEMESTER TINYTEXT NOT NULL, MODULNUMBER TINYTEXT NOT NULL, MODUL TINYTEXT NOT NULL, PASS TINYTEXT NOT NULL, GRADE TINYTEXT);");
        Cursor resultSet = sqlgrade.rawQuery("Select * from Grades WHERE SEMESTER = \'" + semester + "\' and MODUL = \'" + modul + "\' and MODULNUMBER = \'" + modulNumber + "\'", null);

        if (!resultSet.moveToFirst()) {
            sqlgrade.execSQL("INSERT INTO Grades (SEMESTER, MODULNUMBER, MODUL, PASS, GRADE) VALUES(\"" + semester + "\", \"" + modulNumber + "\", \"" + modul + "\", \"" + pass + "\", \"" + grade + "\");");
            resultSet.close();
            return true;
        }
        resultSet.close();
        return false;
    }

    public ArrayList<ModulInfo> selectData(Context context) {
        //SQLiteDatabase db = (context.getApplicationContext().openOrCreateDatabase("HochschulCrawlerGrades", Context.MODE_PRIVATE, null));
        ArrayList<ModulInfo> arraylist = new ArrayList<ModulInfo>();
        @SuppressLint("Recycle")
        Cursor cursor = sqlgrade.rawQuery( "SELECT * FROM Grades", null );
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            arraylist.add(new ModulInfo(cursor.getString(1), cursor.getString(2), cursor.getString(3),  cursor.getString(4), cursor.getString(5)));
            //array_list.add(cursor.getString(cursor.getColumnIndex("SEMMOD")));
            cursor.moveToNext();
        }
        return arraylist;
    }

    public void dropTable()  {
        sqlgrade.execSQL("DROP TABLE IF EXISTS Grades");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /*private boolean connectToDatabase(String sem_mod, Context context) {
        sqlgrade.execSQL("CREATE TABLE IF NOT EXISTS Grades(ID INTEGER PRIMARY KEY AUTOINCREMENT,SEMMOD TEXT NOT NULL);");
        Cursor resultSet = sqlgrade.rawQuery("Select SEMMOD from Grades WHERE SEMMOD = \'" + sem_mod + "\'", null);

        if (!resultSet.moveToFirst()) {
            sqlgrade.execSQL("INSERT INTO Grades (SEMMOD) VALUES(\"" + sem_mod + "\");");
            resultSet.close();
            return true;
        }
        resultSet.close();
        return false;
    }*/
}
