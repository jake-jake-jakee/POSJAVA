package POS;

import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL = "jdbc:mysql://localhost:3306/cafe_management";
    static final String USER = "root";
    static final String PASS = "";

    public static void main(String[] args) {
        POSCoffeeShop pos = new POSCoffeeShop();
        pos.runPOS();
    }

    static class POSCoffeeShop {

        Scanner input = new Scanner(System.in);
        Connection conn;
        String customerName;
        double totalAmount = 0;
        int orderId;

        POSCoffeeShop() {
            connectToDatabase();
        }

        void connectToDatabase() {
            try {
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Connected");
            } catch (SQLException e) {
                System.out.println("Database connection error: " + e.getMessage());
                System.exit(1); // Exit if cannot connect
            }
        }

        void runPOS() {
            greetCustomer();
            showMenu();

            Order order = new Order(conn, orderId);
            order.takeOrders();
        }

        void greetCustomer() {
            System.out.println("--------- Welcome to 5th Cafe! ---------");
            System.out.print("Enter your name: ");
            customerName = input.nextLine();

            // Insert customer name into Orders table with total_amount = 0 initially
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Orders (customer_name, total_amount) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, customerName);
                ps.setDouble(2, 0.0);
                ps.executeUpdate();

                // Get generated order ID
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }
            } catch (SQLException e) {
                System.out.println("Failed to create order: " + e.getMessage());
            }
        }

        void showMenu() {
            System.out.println("-----------------------------------------");
            System.out.println("\t\t  MENU");

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM MenuItems")) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("product_name");
                    double price = rs.getDouble("price");
                    System.out.printf("%d. %-25s ₱%.2f%n", id, name, price);
                }

            } catch (SQLException e) {
                System.out.println("Failed to fetch menu: " + e.getMessage());
            }

            System.out.println("-----------------------------------------");
        }

    }
}
