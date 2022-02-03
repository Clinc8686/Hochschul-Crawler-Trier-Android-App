package de.clinc8686.hochschul_crawler;

public class ModulInfo {
    public String semester;
    public String grade;
    public String pass;
    public String modul;
    public String modulNumber;

    public ModulInfo(String semester, String modulNumber, String modul, String pass, String grade) {
        this.semester = semester;
        this.modul = modul;
        this.pass = pass;
        this.grade = grade;
        this.modulNumber = modulNumber;
    }
}
