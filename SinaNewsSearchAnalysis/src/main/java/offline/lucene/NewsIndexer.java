package offline.lucene;


import com.hankcs.lucene.HanLPAnalyzer;
import offline.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.orm.jpa.vendor.Database;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import offline.utils.Category;


public class NewsIndexer {

    // 日志记录
    static final Logger logger = LogManager.getLogger(NewsIndexer.class.getName());

    private String path;
    private Directory directory;
    private DatabaseUtil db;
    private Analyzer analyzer;

    private IndexSearcher indexSearcher;

    public NewsIndexer() throws IOException {
        path = "./index";
        directory = FSDirectory.open(Paths.get(path));
        db = new DatabaseUtil();
        analyzer = new HanLPAnalyzer();
        indexSearcher = null;
    }


    public NewsIndexer(String index) throws IOException {
        path = index;
        directory = FSDirectory.open(Paths.get(path));
        db = new DatabaseUtil();
        analyzer = new HanLPAnalyzer();
        indexSearcher = null;
    }

    public void writeIndexer() throws IOException, SQLException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        logger.info("开始在" + directory.toString() + " 中生成目录...");
        ResultSet rs = db.selectAllNews();
        while(rs.next()) {
            Document doc = new Document();
            int id = rs.getInt("id");
            doc.add(new TextField("ID", String.valueOf(id), Field.Store.YES));

            String title = rs.getString("title");
            doc.add(new TextField("TITLE", title, Field.Store.YES));

            int categoryInteger = rs.getInt("category");
            String category = Category.int2Cate(categoryInteger);
            doc.add(new StringField("CATEGORY", category, Field.Store.NO));

            java.sql.Blob blob = rs.getBlob("content");
            String content = new String(blob.getBytes(1l, (int) blob.length()));
            doc.add(new TextField("CONTENT", content, Field.Store.YES));

            indexWriter.addDocument(doc);
        }
        indexWriter.close();
    }

    public void readIndexer() throws IOException {
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(path)));
        indexSearcher = new IndexSearcher(indexReader);
    }

    public ArrayList<Integer> queryIndexer(String q, int n) throws ParseException, IOException {
        if (indexSearcher == null) {
            readIndexer();
        }
        ArrayList<Integer> hitsID = new ArrayList<>();
        Set<String> filterSet = new HashSet<>();
        QueryParser parser = new MultiFieldQueryParser(new String[] {"CONTENT", "TITLE"}, analyzer);
        Query query = parser.parse(q);
        TopDocs results = indexSearcher.search(query, n*3);
        ScoreDoc[] hits = results.scoreDocs;
        for (ScoreDoc scoreDoc: hits) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            String id = doc.get("ID");
            if (filterSet.contains(id))
                continue;
            // String title = doc.get("TITLE");
            hitsID.add(Integer.valueOf(id));
            filterSet.add(id);
            // System.out.println(id);
            // int id = Integer.valueOf(doc.get("ID"));
            // System.out.println(scoreDoc.score);
        }
        return hitsID;
    }

}
