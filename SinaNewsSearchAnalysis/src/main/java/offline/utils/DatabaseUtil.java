package offline.utils;

import offline.crawler.NewsModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DatabaseUtil {

    // 日志
    static final Logger logger = LogManager.getLogger(DatabaseUtil.class.getName());

    private Connection connection;
    PreparedStatement pStmtInsert = null;
    PreparedStatement pStmtSelectByCategory = null;
    PreparedStatement pStmtSelectByID = null;
    PreparedStatement pStmtSelectByURL = null;
    PreparedStatement pStmtSelectAll = null;
    PreparedStatement pStmtUpdateCategory = null;
    PreparedStatement pStmtSelectLatest = null;

    public DatabaseUtil() {
        try {
            linkDatabase();
        } catch (Exception e) {
            System.out.println(e);
            logger.error("数据库连接失败！");
            System.exit(-1);
        }
    }

    private void linkDatabase() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news?useSSL=false&characterEncoding=UTF-8&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root","root");
        pStmtInsert = (PreparedStatement) connection.prepareStatement("INSERT INTO `news` (`title`, `url`, `category`, `timestamp`, `content`) VALUES (?,?,?,?,?)");
        pStmtSelectByCategory = (PreparedStatement) connection.prepareStatement("SELECT * FROM `news` WHERE `category` = ? ORDER BY `timestamp` DESC LIMIT 20");
        pStmtSelectByID = (PreparedStatement) connection.prepareStatement("SELECT * FROM `news` WHERE `id` = ?");
        pStmtSelectByURL = (PreparedStatement) connection.prepareStatement("SELECT * FROM `news` WHERE `url` = ?");
        pStmtSelectAll = (PreparedStatement) connection.prepareStatement("SELECT * FROM `news` ORDER BY `timestamp` DESC ");
        pStmtUpdateCategory = (PreparedStatement) connection.prepareStatement("UPDATE `news` SET `category` = ? WHERE `id` = ?");
        pStmtSelectLatest = (PreparedStatement) connection.prepareStatement("SELECT * FROM `news` ORDER BY `timestamp` DESC LIMIT 20");
    }

    public void insertNews(NewsModel nm) throws SQLException {
        String uniqueUrl = nm.getLink();
        // 先判断是否已经存在，避免重复插入
        if (checkUrl(uniqueUrl)) {
            logger.warn("新闻 《" + nm.getTitle() +"》 已存在，忽略！");
            return;
        }
        pStmtInsert.setString(1, nm.getTitle());
        pStmtInsert.setString(2, uniqueUrl);
        pStmtInsert.setInt(3, nm.getCategory());
        pStmtInsert.setInt(4, nm.getTime());
        pStmtInsert.setString(5, nm.getContent());
        // System.out.println(nm.getContent());
        int i = pStmtInsert.executeUpdate();
        if ( i <= 0 ) {
            logger.error("插入失败，请检查数据库设置和连接！");
            System.exit(0);
        }
        else {
            logger.info("插入新闻 《" + nm.getTitle() +"》");
        }
    }

    public boolean checkUrl(String url) throws SQLException {
        pStmtSelectByURL.setString(1, url);
        ResultSet rs = pStmtSelectByURL.executeQuery();
        if (rs.next()) {
            // logger.warn("新闻已存在!");
            return true;
        }
        else
            return false;
    }

    public ResultSet selectAllNews() throws SQLException {
        return pStmtSelectAll.executeQuery();
    }

    public ResultSet selectNewsById(int id) throws SQLException {
        pStmtSelectByID.setInt(1, id);
        return pStmtSelectByID.executeQuery();
    }

    public int updateCategory(int id, int cate) throws SQLException {
        pStmtUpdateCategory.setInt(1, cate);
        pStmtUpdateCategory.setInt(2, id);
        try {
            pStmtUpdateCategory.executeUpdate();
            logger.info("修改了 " + id + " 的类别");
        }
        catch (Exception e)
        {
            System.out.println(e);
            return -1;
        }
        return 1;
    }

    public ResultSet getLatestNews() throws SQLException {
        return pStmtSelectLatest.executeQuery();
    }

    public ResultSet selectNewsByCategory(int cate) throws SQLException {
        pStmtSelectByCategory.setInt(1, cate);
        return pStmtSelectByCategory.executeQuery();
    }
}
