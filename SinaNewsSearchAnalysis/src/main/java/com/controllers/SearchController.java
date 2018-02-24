package com.controllers;

import com.models.SearchResult;
import offline.lucene.NewsIndexer;
import offline.utils.Category;
import offline.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 仅返回数据！
 */
@RestController
public class SearchController {
    static final Logger logger = LogManager.getLogger(SearchController.class.getName());

    private NewsIndexer ni;
    private DatabaseUtil dbUtil;
    public SearchController() throws IOException {
       ni = new NewsIndexer("./luceneindex");
       dbUtil = new DatabaseUtil();
    }

    @RequestMapping(value="/search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<SearchResult> search(@RequestParam(name = "query") String query) throws IOException, ParseException, SQLException {
        logger.info("搜索关键词：" + query);
        List<Integer> idList = ni.queryIndexer(query, 100);
        ArrayList<SearchResult> resultList = new ArrayList<>();
        // System.out.println(idList);
        for(Integer i: idList) {
            ResultSet rsI = dbUtil.selectNewsById(i);
            if (rsI.next()) {
                SearchResult sr = new SearchResult();
                sr.setId(i);
                sr.setTitle(rsI.getString("title"));
                sr.setCategory(rsI.getInt("category"));
                sr.setUrl(rsI.getString("url"));
                sr.setCategoryText(Category.int2Cate(sr.getCategory()));
                resultList.add(sr);
            }
        }
        // System.out.println(resultList);
        return resultList;
    }
}
