package com.example.hadoop.db;

public class DBEntity {

    String department;
    int totalCount;
    double average;
    double gradeAPercent;
    double gradeBPercent;
    double gradeCPercent;
    double gradeDPercent;
    double gradeFPercent;
    double excellentPercent;
    double qualifiedPercent;
    double failedPercent;

    public DBEntity() {}

    public DBEntity(String department, int totalCount, double average, double gradeAPercent, double gradeBPercent, double gradeCPercent, double gradeDPercent, double gradeFPercent, double excellentPercent, double qualifiedPercent, double failedPercent) {
        this.department = department;
        this.totalCount = totalCount;
        this.average = average;
        this.gradeAPercent = gradeAPercent;
        this.gradeBPercent = gradeBPercent;
        this.gradeCPercent = gradeCPercent;
        this.gradeDPercent = gradeDPercent;
        this.gradeFPercent = gradeFPercent;
        this.excellentPercent = excellentPercent;
        this.qualifiedPercent = qualifiedPercent;
        this.failedPercent = failedPercent;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getGradeAPercent() {
        return gradeAPercent;
    }

    public void setGradeAPercent(double gradeAPercent) {
        this.gradeAPercent = gradeAPercent;
    }

    public double getGradeBPercent() {
        return gradeBPercent;
    }

    public void setGradeBPercent(double gradeBPercent) {
        this.gradeBPercent = gradeBPercent;
    }

    public double getGradeCPercent() {
        return gradeCPercent;
    }

    public void setGradeCPercent(double gradeCPercent) {
        this.gradeCPercent = gradeCPercent;
    }

    public double getGradeDPercent() {
        return gradeDPercent;
    }

    public void setGradeDPercent(double gradeDPercent) {
        this.gradeDPercent = gradeDPercent;
    }

    public double getGradeFPercent() {
        return gradeFPercent;
    }

    public void setGradeFPercent(double gradeFPercent) {
        this.gradeFPercent = gradeFPercent;
    }

    public double getExcellentPercent() {
        return excellentPercent;
    }

    public void setExcellentPercent(double excellentPercent) {
        this.excellentPercent = excellentPercent;
    }

    public double getQualifiedPercent() {
        return qualifiedPercent;
    }

    public void setQualifiedPercent(double qualifiedPercent) {
        this.qualifiedPercent = qualifiedPercent;
    }

    public double getFailedPercent() {
        return failedPercent;
    }

    public void setFailedPercent(double failedPercent) {
        this.failedPercent = failedPercent;
    }
}
