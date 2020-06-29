package com;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author yolo
 * @date 2020/4/10-11:08
 */
@WebServlet("/words.json")
public class WordServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        String condition=req.getParameter("condition");
        if(condition==null){
            condition="5";
        }


        HashMap<String,Integer>map=new HashMap<>();
        Connection connection = DBConfig.getConnection();
        String sql="SELECT word FROM tangshi";
        PreparedStatement statement=null;
        try {
            statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                String word=rs.getString("word");
                String[] split = word.split(",");
                for (String s : split) {
                    int count=map.getOrDefault(s,0);
                    map.put(s,++count);
                }
            }
            // System.out.println(map.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject object = new JSONObject();
            object.put("error", e.getMessage());
            resp.getWriter().println(object.toJSONString());
        }finally {
            DBConfig.close(connection,statement);
        }

        JSONArray jsonArray=new JSONArray();
        for (String s : map.keySet()) {
            JSONArray item=new JSONArray();
            item.add(s);
            item.add(map.get(s));
            jsonArray.add(item);
        }

        resp.setContentType("application/json ; charset=utf-8");
        resp.getWriter().println(jsonArray.toJSONString());
        System.out.println(jsonArray.toJSONString());
    }
}
