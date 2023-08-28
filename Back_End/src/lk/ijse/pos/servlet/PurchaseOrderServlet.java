package lk.ijse.pos.servlet;

import javafx.embed.swt.SWTFXUtils;
import lk.ijse.pos.dto.CustomerDTO;
import lk.ijse.pos.dto.ItemDTO;
import lk.ijse.pos.dto.OrderDTO;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import static java.lang.Class.forName;

@WebServlet(urlPatterns = "/pages/purchase-order")
public class PurchaseOrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String option = req.getParameter("option");

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "root123");

                    switch (option){
                        case "customer":
                            String id1 = req.getParameter("id");
                        PreparedStatement pstm = connection.prepareStatement("select * from Customer where id =?");
                        pstm.setString(1,id1);

                    ResultSet rst = pstm.executeQuery();
                    resp.addHeader("Content-Type","application/json");
                    resp.addHeader("Access-Control-Allow-Origin", "*");

                    JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

                    while (rst.next()) {

                        String id = rst.getString(1);
                        String name = rst.getString(2);
                        String address = rst.getString(3);

                        objectBuilder.add("id",id);
                        objectBuilder.add("name",name);
                        objectBuilder.add("address",address);


                    }

                    resp.getWriter().print(objectBuilder.build());

//                System.out.println("awaaaa");

                break;
            case    "item" :

                String code = req.getParameter("code");
                PreparedStatement pstm1 = connection.prepareStatement("select * from item where code =?");
                pstm1.setString(1,code);

                ResultSet rst1 = pstm1.executeQuery();
                resp.addHeader("Content-Type","application/json");
                resp.addHeader("Access-Control-Allow-Origin", "*");

                JsonObjectBuilder objectBuilder1 = Json.createObjectBuilder();

                while (rst1.next()) {

                    String code1 = rst1.getString(1);
                    String description = rst1.getString(2);
                    String qty = rst1.getString(3);
                    String price = rst1.getString(4);

                    objectBuilder1.add("code",code1);
                    objectBuilder1.add("description",description);
                    objectBuilder1.add("qtyOnHand",qty);
                    objectBuilder1.add("unitPrice",price);


                }

                resp.getWriter().print(objectBuilder1.build());

                break;
        }

                } catch (ClassNotFoundException e) {

                    JsonObjectBuilder error = Json.createObjectBuilder();
                    error.add("state","Error");
                    error.add("message",e.getLocalizedMessage());
                    error.add("Data"," ");
                    resp.setStatus(500);
                    resp.getWriter().print(error.build());

                } catch (SQLException e) {
                    JsonObjectBuilder error = Json.createObjectBuilder();
                    error.add("state","Error");
                    error.add("message",e.getLocalizedMessage());
                    error.add("Data"," ");
                    resp.getWriter().print(error.build());
                }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject jsonObject = reader.readObject();

        resp.addHeader("Content-Type","application/json");
        resp.addHeader("Access-Control-Allow-Origin","*");

        String orderId = jsonObject.getString("oid");
        String orderDate = jsonObject.getString("date");
        String customerId = jsonObject.getString("customerID");
        String itemCode = jsonObject.getString("code");
        String qty = jsonObject.getString("qtyOnHand");
        String unitPrice = jsonObject.getString("unitPrice");
        JsonArray orderDetails = jsonObject.getJsonArray("orderDetails");

        try {
            forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "root123");
            connection.setAutoCommit(false);

            PreparedStatement orderStatement = connection.prepareStatement("INSERT INTO orders VALUES(?,?,?)");
            orderStatement.setString(1, orderId);
            orderStatement.setString(2, orderDate);
            orderStatement.setString(3, customerId);

            int affectedRows = orderStatement.executeUpdate();
            if (affectedRows == 0) {
                connection.rollback();
                throw new RuntimeException("Failed to save the order");
            } else {
                System.out.println("Order Saved");

            }


            PreparedStatement orderDetailStatement = connection.prepareStatement("INSERT INTO orderDetails VALUES(?,?,?,?)");
            orderDetailStatement.setString(1, orderId);
            orderDetailStatement.setString(2, itemCode);
            orderDetailStatement.setString(3, qty);
            orderDetailStatement.setString(4, unitPrice);

            affectedRows = orderDetailStatement.executeUpdate();
            if (affectedRows == 0) {
                connection.rollback();
                throw new RuntimeException("Failed to save the order details");
            } else {
                System.out.println("Order Details Saved");
            }


            connection.commit();
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("message", "Successfully Purchased Order.");
            objectBuilder.add("status", resp.getStatus());
            resp.getWriter().print(objectBuilder.build());


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("message", "Failed to save the order.");
            objectBuilder.add("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(objectBuilder.build());

        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin","*");
        resp.addHeader("Access-Control-Allow-Methods","PUT, DELETE");
        resp.addHeader("Access-Control-Allow-Headers","content-type");
    }
}