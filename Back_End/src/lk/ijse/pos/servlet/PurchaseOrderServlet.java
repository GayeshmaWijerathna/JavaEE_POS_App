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

                System.out.println("awaaaa");

                break;
            case    "item" :



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


     /*   try {
            forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "root123");
            PreparedStatement pstm = connection.prepareStatement("select * from orders");
              ResultSet rst = pstm.executeQuery();

            PrintWriter writer = resp.getWriter();
            resp.addHeader("Access-Control-Allow-Origin","*");
            resp.addHeader("Content-Type","application/json");

            JsonArrayBuilder allCustomers = Json.createArrayBuilder();


            while (rst.next()) {
                String orderID = rst.getString(1);
                String orderCusID = rst.getString(2);
                String orderDate = rst.getString(3);

                JsonObjectBuilder customer = Json.createObjectBuilder();

                customer.add("oid",orderID);
                customer.add("customerID",orderCusID);
                customer.add("date",orderDate);

                allCustomers.add(customer.build());
            }


            writer.print(allCustomers.build());


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
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