package dapaixing;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/words.json")
public class RankServlet2 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=utf-8");

        JSONArray jsonArray = new JSONArray();
        try (Connection connection = DBConfig.getConnection()) {
            String sql = "SELECT words FROM tangpoetry";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<String> list = new ArrayList<>();
                    Map<String,Integer> map = new HashMap<>();
                    while (rs.next()){
                        String words = rs.getString("words");
                        list.add(words);
                    }
                    for (String s : list) {
                        String[] split = s.split(",");
                        for (int i = 0; i < split.length; i++) {
                            Integer value = map.get(split[i]);
                            if (value == null){
                                map.put(split[i],1);
                            }else {
                                value++;
                                map.put(split[i],value);
                            }
                        }
                    }
                    Set<Map.Entry<String, Integer>> set = map.entrySet();
                    for (Map.Entry<String, Integer> stringIntegerEntry : set) {
                        JSONArray item = new JSONArray();
                        item.add(stringIntegerEntry.getKey());
                        item.add(stringIntegerEntry.getValue());
                        jsonArray.add(item);
                    }
                    resp.getWriter().println(jsonArray.toJSONString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject object = new JSONObject();
            object.put("error", e.getMessage());
            resp.getWriter().println(object.toJSONString());
        }

        /*
        {
            JSONArray item = new JSONArray();
            item.add("李白");
            item.add(5);
            jsonArray.add(item);
        }
        {
            JSONArray item = new JSONArray();
            item.add("杜甫");
            item.add(3);
            jsonArray.add(item);
        }
        {
            JSONArray item = new JSONArray();
            item.add("白居易");
            item.add(2);
            jsonArray.add(item);
        }
        */
    }
}
