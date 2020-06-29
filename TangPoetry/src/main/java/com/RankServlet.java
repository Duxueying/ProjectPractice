package com;



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

/**
 * @author yolo
 * @date 2020/4/10-11:04
 */
@WebServlet("/rank.json")
public class RankServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=utf-8");
        //用的get方法
        String condition = req.getParameter("condition");
        if (condition == null) {
            condition = "5";
        }

        JSONArray jsonArray = new JSONArray();
        try (Connection connection = DBConfig.getConnection()) {
            String sql = "SELECT author, count(*) AS cnt FROM tangshi GROUP BY author HAVING cnt >= ? ORDER BY cnt DESC";
            //把作者全部统计出来
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, condition);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String author = rs.getString("author");
                        int count = rs.getInt("cnt");
                        JSONArray item = new JSONArray();
                        item.add(author);
                        item.add(count);
                        jsonArray.add(item);
                        System.out.println(author+":"+count);
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
    }
}