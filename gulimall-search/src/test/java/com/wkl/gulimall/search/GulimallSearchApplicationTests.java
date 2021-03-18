package com.wkl.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.wkl.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 检索数据
     */
    @Test
    public void searchDataTest() throws IOException {
        SearchRequest searchRequest = new SearchRequest();

        // 指定索引
        searchRequest.indices("bank");

        // 1.指定检索条件 DSL
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 1.1 构造检索条件
        builder.query(QueryBuilders.matchQuery("address","mill"));

        // 1.2 按照年龄值聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        builder.aggregation(ageAgg);
        // 1.3 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        builder.aggregation(balanceAvg);

        System.out.println("检索条件：" + builder.toString());
        searchRequest.source(builder);

        // 2.执行检索
        SearchResponse search = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 3.分析结果
//		Map map = JSON.parseObject(search.toString(), Map.class);
//		System.out.println(map);
        // 3.1 索取所有记录
        SearchHits hits = search.getHits();
        // 详细记录
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
//			String index = hit.getIndex();
//			String id = hit.getId();
            String source = hit.getSourceAsString();
            Account account = JSON.parseObject(source, Account.class);
            System.out.println(account);
        }
        // 获取分析数据
        Aggregations aggregations = search.getAggregations();
//		List<Aggregation> list = aggregations.asList();
//		for (Aggregation aggregation : list) {
//			Terms agg = aggregations.get(aggregation.getName());
//			System.out.println(agg.getBuckets());
//		}
        Terms agg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            System.out.println("年龄: " + bucket.getKeyAsString() + "-->" + bucket.getDocCount() + "人");
        }

        Avg avg = aggregations.get("balanceAvg");
        System.out.println("平均薪资： " + avg.getValue());
    }

    /**
     * 保存、更新都可使用index
     * @throws IOException
     */
    @Test
    void index() throws IOException {
        //声明index，存储索引
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        //数据
        User user = new User();
        user.setName("LiHua");
        user.setAge(12);
        //转json方式
        String s = JSON.toJSONString(user);
        //保存索引方式，要表明JSON类型
        indexRequest.source(s, XContentType.JSON);

        //创建索引，并且使用 请求选项，返回数据
        IndexResponse index = restHighLevelClient.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }
    @Data
    class User{
        private String name;
        private Integer age;
    }

    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

    @Data
    static class Account {

        private int accountNumber;

        private int balance;

        private String firstname;

        private String lastname;

        private int age;

        private String gender;

        private String address;

        private String employer;

        private String email;

        private String city;

        private String state;
    }

}
