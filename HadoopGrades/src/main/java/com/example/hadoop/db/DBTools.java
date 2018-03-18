package com.example.hadoop.db;


import java.sql.*;

public class DBTools {

    private Connection connection;
    PreparedStatement pStmt2014Insert = null;
    PreparedStatement pStmt2014Select = null;
    PreparedStatement pStmt2015Insert = null;
    PreparedStatement pStmt2015Select = null;
    PreparedStatement pStmt2016Insert = null;
    PreparedStatement pStmt2016Select = null;
    PreparedStatement pStmt2017Insert = null;
    PreparedStatement pStmt2017Select = null;

    public DBTools() {
        try {
            linkDatabase();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("数据库连接错误！");
            System.exit(-1);
        }
    }

    private void linkDatabase() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/grades?useSSL=false&characterEncoding=UTF-8&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root","root");
        pStmt2014Insert = (PreparedStatement) connection.prepareStatement("INSERT INTO `2014` (`department`, `totalCount`, `average`, `gradeA`, `gradeB`, `gradeC`, `gradeD`, `gradeF`, `excellent`, `qualified`, `failed`) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        pStmt2014Select = (PreparedStatement) connection.prepareStatement("SELECT * FROM `2014` WHERE `department` LIKE ?");
        pStmt2015Insert = (PreparedStatement) connection.prepareStatement("INSERT INTO `2015` (`department`, `totalCount`, `average`, `gradeA`, `gradeB`, `gradeC`, `gradeD`, `gradeF`, `excellent`, `qualified`, `failed`) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        pStmt2015Select = (PreparedStatement) connection.prepareStatement("SELECT * FROM `2015` WHERE `department` LIKE ?");
        pStmt2016Insert = (PreparedStatement) connection.prepareStatement("INSERT INTO `2016` (`department`, `totalCount`, `average`, `gradeA`, `gradeB`, `gradeC`, `gradeD`, `gradeF`, `excellent`, `qualified`, `failed`) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        pStmt2016Select = (PreparedStatement) connection.prepareStatement("SELECT * FROM `2016` WHERE `department` LIKE ?");
        pStmt2017Insert = (PreparedStatement) connection.prepareStatement("INSERT INTO `2017` (`department`, `totalCount`, `average`, `gradeA`, `gradeB`, `gradeC`, `gradeD`, `gradeF`, `excellent`, `qualified`, `failed`) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        pStmt2017Select = (PreparedStatement) connection.prepareStatement("SELECT * FROM `2017` WHERE `department` LIKE ?");
    }

    public int insert(int y, DBEntity entity) throws SQLException {
        if (y == 2014) {
            pStmt2014Insert.setString(1, entity.getDepartment());
            pStmt2014Insert.setInt(2, entity.getTotalCount());
            pStmt2014Insert.setDouble(3, entity.getAverage());
            pStmt2014Insert.setDouble(4, entity.getGradeAPercent());
            pStmt2014Insert.setDouble(5, entity.getGradeBPercent());
            pStmt2014Insert.setDouble(6, entity.getGradeCPercent());
            pStmt2014Insert.setDouble(7, entity.getGradeDPercent());
            pStmt2014Insert.setDouble(8, entity.getGradeFPercent());
            pStmt2014Insert.setDouble(9, entity.getExcellentPercent());
            pStmt2014Insert.setDouble(10, entity.getQualifiedPercent());
            pStmt2014Insert.setDouble(11, entity.getFailedPercent());
            return pStmt2014Insert.executeUpdate();
        } else if (y == 2015) {
            pStmt2015Insert.setString(1, entity.getDepartment());
            pStmt2015Insert.setInt(2, entity.getTotalCount());
            pStmt2015Insert.setDouble(3, entity.getAverage());
            pStmt2015Insert.setDouble(4, entity.getGradeAPercent());
            pStmt2015Insert.setDouble(5, entity.getGradeBPercent());
            pStmt2015Insert.setDouble(6, entity.getGradeCPercent());
            pStmt2015Insert.setDouble(7, entity.getGradeDPercent());
            pStmt2015Insert.setDouble(8, entity.getGradeFPercent());
            pStmt2015Insert.setDouble(9, entity.getExcellentPercent());
            pStmt2015Insert.setDouble(10, entity.getQualifiedPercent());
            pStmt2015Insert.setDouble(11, entity.getFailedPercent());
            return pStmt2015Insert.executeUpdate();
        } else if (y == 2016) {
            pStmt2016Insert.setString(1, entity.getDepartment());
            pStmt2016Insert.setInt(2, entity.getTotalCount());
            pStmt2016Insert.setDouble(3, entity.getAverage());
            pStmt2016Insert.setDouble(4, entity.getGradeAPercent());
            pStmt2016Insert.setDouble(5, entity.getGradeBPercent());
            pStmt2016Insert.setDouble(6, entity.getGradeCPercent());
            pStmt2016Insert.setDouble(7, entity.getGradeDPercent());
            pStmt2016Insert.setDouble(8, entity.getGradeFPercent());
            pStmt2016Insert.setDouble(9, entity.getExcellentPercent());
            pStmt2016Insert.setDouble(10, entity.getQualifiedPercent());
            pStmt2016Insert.setDouble(11, entity.getFailedPercent());
            return pStmt2016Insert.executeUpdate();
        } else if (y == 2017) {
            pStmt2017Insert.setString(1, entity.getDepartment());
            pStmt2017Insert.setInt(2, entity.getTotalCount());
            pStmt2017Insert.setDouble(3, entity.getAverage());
            pStmt2017Insert.setDouble(4, entity.getGradeAPercent());
            pStmt2017Insert.setDouble(5, entity.getGradeBPercent());
            pStmt2017Insert.setDouble(6, entity.getGradeCPercent());
            pStmt2017Insert.setDouble(7, entity.getGradeDPercent());
            pStmt2017Insert.setDouble(8, entity.getGradeFPercent());
            pStmt2017Insert.setDouble(9, entity.getExcellentPercent());
            pStmt2017Insert.setDouble(10, entity.getQualifiedPercent());
            pStmt2017Insert.setDouble(11, entity.getFailedPercent());
            return pStmt2017Insert.executeUpdate();
        }
        return 0;
    }

    // 一般根据年份查找，默认 y = 0，查找 2017 年的全校成绩情况
    public DBEntity select(int y, String d) throws SQLException {
        String department;
        if (d.length() < 1) {
            department = "全校";
        } else {
            department = d;
        }
        DBEntity dbe = new DBEntity();
        ResultSet rs = null;
        if(y == 2014) {
            pStmt2014Select.setString(1, department);
            rs = pStmt2014Select.executeQuery();
        } else if (y == 2015) {
            pStmt2015Select.setString(1, department);
            rs = pStmt2015Select.executeQuery();
        } else if (y == 2016) {
            pStmt2016Select.setString(1, department);
            rs = pStmt2016Select.executeQuery();
        } else if (y == 2017 || y == 0) {
            pStmt2017Select.setString(1, department);
            rs = pStmt2017Select.executeQuery();
        }
        if(rs.next()) {
            dbe.setDepartment(rs.getString(1));
            dbe.setTotalCount(rs.getInt(2));
            dbe.setAverage(rs.getDouble(3));
            dbe.setGradeAPercent(rs.getDouble(4));
            dbe.setGradeBPercent(rs.getDouble(5));
            dbe.setGradeCPercent(rs.getDouble(6));
            dbe.setGradeDPercent(rs.getDouble(7));
            dbe.setGradeFPercent(rs.getDouble(8));
            dbe.setExcellentPercent(rs.getDouble(9));
            dbe.setQualifiedPercent(rs.getDouble(10));
            dbe.setFailedPercent(rs.getDouble(11));
            return dbe;
        }
        return null;
    }
}
