package POS;

import java.sql.*;
import java.util.Scanner;

public class Order {

    private final Connection conn;
    private final Scanner input;
    private final int orderId;
    private double totalAmount;

    public Order(Connection conn, int orderId) {
        this.conn = conn;
        this.input = new Scanner(System.in);
        this.orderId = orderId;
        this.totalAmount = 0;
    }

    public void takeOrders() {
        while (true) {
            System.out.print("Enter Menu Item ID to order (0 to finish): ");
            int menuItemId = input.nextInt();
            if (menuItemId == 0) break;

            System.out.print("Enter quantity: ");
            int quantity = input.nextInt();

            if (addOrderItem(menuItemId, quantity)) {
                updateTotal(menuItemId, quantity);
            }
        }

        updateOrderTotal();
        System.out.printf("Order completed. Total: ₱%.2f%n", totalAmount);
    }

    private boolean addOrderItem(int menuItemId, int quantity) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)")) {
            ps.setInt(1, orderId);
            ps.setInt(2, menuItemId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Failed to add order item: " + e.getMessage());
            return false;
        }
    }

    private void updateTotal(int menuItemId, int quantity) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT price FROM menuitems WHERE id = ?")) {
            ps.setInt(1, menuItemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double price = rs.getDouble("price");
                totalAmount += price * quantity;
            }
        } catch (SQLException e) {
            System.out.println("Failed to get item price: " + e.getMessage());
        }
    }

    private void updateOrderTotal() {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE orders SET total_amount = ? WHERE orderID = ?")) {
            ps.setDouble(1, totalAmount);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to update order total: " + e.getMessage());
        }
    }
}
