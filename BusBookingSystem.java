import java.sql.*;
import java.util.*;

public class BusBookingSystem  	 {

    static Scanner sc = new Scanner(System.in);
    static int loggedInUserId = -1;

    public static void main(String[] args) {

        System.out.println("===== BUS BOOKING SYSTEM =====");

        while (true) {
            System.out.println("\n1. User Login");
            System.out.println("2. Register");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            System.out.print("Choose: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    if (login()) userMenu();
                    else System.out.println("Invalid credentials!");
                    break;

                case 2:
                    registerUser();
                    break;

                case 3:
                    adminLogin();
                    break;

                case 4:
                    System.out.println("Thank you!");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    //================= USER MENU =================


    static void userMenu() {
	
	System.out.println("===== USER MENU =====");
        while (true) {
            System.out.println("\n1. Search Bus");
            System.out.println("2. Book Ticket");
            System.out.println("3. View Tickets");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. Logout");
            System.out.print("Choose: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1: searchBus(); break;
                case 2: bookTicket(); break;
                case 3: viewTickets(); break;
                case 4: cancelTicket(); break;
                case 5: System.out.println("Session Completed");
			System.out.println("Logout Sucessful");
			return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    // ================= LOGIN =================

    static boolean login() {
        try {
            Connection con = DBConnection.getConnection();

            sc.nextLine();
            System.out.print("Username: ");
            String username = sc.nextLine();

            System.out.print("Password: ");
            String password = sc.nextLine();

            String query = "SELECT user_id FROM users_table WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedInUserId = rs.getInt("user_id");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= REGISTER =================

    static void registerUser() {
        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter username: ");
            sc.nextLine();
            String username = sc.nextLine();

            System.out.print("Enter password: ");
            String password = sc.nextLine();

            String idQuery = "SELECT NVL(MAX(user_id),0)+1 FROM users_table";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(idQuery);

            int id = 1;
            if (rs.next()) id = rs.getInt(1);

            String query = "INSERT INTO users_table VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setString(2, username);
            ps.setString(3, password);

            ps.executeUpdate();
            System.out.println("Registration Successful!");

        } catch (Exception e) {
            System.out.println("Username already exists!");
        }
    }

    // ================= SEARCH BUS =================

    static void searchBus() {
        try {
            Connection con = DBConnection.getConnection();

            sc.nextLine();
            System.out.print("Enter Source: ");
            String source = sc.nextLine();

            System.out.print("Enter Destination: ");
            String dest = sc.nextLine();

            String query = "SELECT * FROM buses_table WHERE source=? AND destination=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, source);
            ps.setString(2, dest);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println("Bus ID: " + rs.getInt("bus_id") +
                        " | Seats: " + rs.getInt("seats_available") +
                        " | Price: " + rs.getDouble("price"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AVAILABLE SEATS =================

    static List<Integer> getAvailableSeats(int busId) {
        List<Integer> list = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();

            String totalQuery = "SELECT total_seats FROM buses_table WHERE bus_id=?";
            PreparedStatement ps1 = con.prepareStatement(totalQuery);
            ps1.setInt(1, busId);
            ResultSet rs1 = ps1.executeQuery();

            int total = 0;
            if (rs1.next()) total = rs1.getInt(1);

            String bookedQuery = "SELECT seat_number FROM tickets WHERE bus_id=?";
            PreparedStatement ps2 = con.prepareStatement(bookedQuery);
            ps2.setInt(1, busId);
            ResultSet rs2 = ps2.executeQuery();

            Set<Integer> booked = new HashSet<>();
            while (rs2.next()) booked.add(rs2.getInt(1));

            for (int i = 1; i <= total; i++) {
                if (!booked.contains(i)) list.add(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= BOOK =================

    static void bookTicket() {
        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter Bus ID: ");
            int busId = sc.nextInt();

            List<Integer> seats = getAvailableSeats(busId);

            if (seats.isEmpty()) {
                System.out.println("No seats available!");
                return;
            }

            System.out.println("Available Seats: " + seats);

            System.out.print("Choose seat: ");
            int seat = sc.nextInt();

            if (!seats.contains(seat)) {
                System.out.println("Invalid seat!");
                return;
            }
	    sc.nextLine();
            System.out.print("Passenger Name: ");
            
            String name = sc.nextLine();
	    
	    System.out.print("Enter Gender (Male/Female): ");
	    String gender = sc.nextLine();

	    System.out.print("Enter Travel Date (YYYY-MM-DD): ");
	    
	    String dateInput = sc.nextLine().trim();

	    java.sql.Date travelDate;

	   // System.out.println("DEBUG INPUT = [" + dateInput + "]");

	    

	    try {
    		travelDate = java.sql.Date.valueOf(dateInput);
	    } catch (Exception e) {
    		System.out.println("Invalid date format! Use YYYY-MM-DD.");
    		return;
	    }
	   
            if (travelDate.before(new java.sql.Date(System.currentTimeMillis()))) {
    		System.out.println("Cannot book for past dates!");
    		return;
	     }

	    
       	    String insert = "INSERT INTO tickets (ticket_id, user_id, bus_id, passenger_name, seat_number, travel_date, gender) VALUES 				 (ticket_seq.NEXTVAL, ?, ?, ?, ?, ?, ?)";

	    PreparedStatement ps = con.prepareStatement(insert);

	    ps.setInt(1, loggedInUserId);
	    ps.setInt(2, busId);
	    ps.setString(3, name);
	    ps.setInt(4, seat);
	    ps.setDate(5, travelDate);
	    ps.setString(6, gender);
	    System.out.println("DEBUG: inserting values...");
            ps.executeUpdate();

            String update = "UPDATE buses_table SET seats_available=seats_available-1 WHERE bus_id=?";
            PreparedStatement ps2 = con.prepareStatement(update);
            ps2.setInt(1, busId);
            ps2.executeUpdate();
	    System.out.println("Payment Processing.......");
	    System.out.println("Payment SUCESSFUL");
            System.out.println("Ticket Booked!");

        } catch (SQLException e) {
            if (e.getErrorCode() == 1)
                System.out.println("Seat already booked!");
            else e.printStackTrace();
        }
    }

    // ================= VIEW =================

    static void viewTickets() {
        try {
            Connection con = DBConnection.getConnection();

            String query = "SELECT * FROM tickets WHERE user_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, loggedInUserId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println("Ticket ID: " + rs.getInt(1) +
                        " | Bus: " + rs.getInt(3) +
			" | Passenger: " + rs.getString(4)+
			" | Gender : " + rs.getString(7)+
                        " | Seat: " + rs.getInt(5) + 
			" | Date: " + rs.getDate(6));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CANCEL =================

    static void cancelTicket() {
        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter Ticket ID: ");
            int id = sc.nextInt();

            String get = "SELECT bus_id FROM tickets WHERE ticket_id=?";
            PreparedStatement ps1 = con.prepareStatement(get);
            ps1.setInt(1, id);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
                System.out.println("Ticket not found!");
                return;
            }

            int busId = rs.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("DELETE FROM tickets WHERE ticket_id=?");
            ps2.setInt(1, id);
            ps2.executeUpdate();

            PreparedStatement ps3 = con.prepareStatement(
                    "UPDATE buses_table SET seats_available=seats_available+1 WHERE bus_id=?");
            ps3.setInt(1, busId);
            ps3.executeUpdate();

            System.out.println("Ticket Cancelled!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADMIN =================

    static void adminLogin() {
        sc.nextLine();

        System.out.print("Admin Username: ");
        String u = sc.nextLine();

        System.out.print("Admin Password: ");
        String p = sc.nextLine();

        if (u.equals("admin") && p.equals("admin123"))
            adminMenu();
        else
            System.out.println("Invalid admin!");
    }





    static void adminMenu() {
	System.out.println("===== ADMIN MENU =====");
        while (true) {
            System.out.println("\n1. Add Bus");
            System.out.println("2. View All Tickets");
            System.out.println("3. Logout");
	    System.out.print("Choose: ");

            int c = sc.nextInt();

            switch (c) {
                case 1: addBus(); break;
                case 2: viewAllTickets(); break;
                case 3: System.out.println("Session Completed");
			System.out.println("Logout Sucessful");
			return;
            }
        }
    }

    static void addBus() {
        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Bus ID: ");
            int id = sc.nextInt();

            sc.nextLine();
            System.out.print("Source: ");
            String s = sc.nextLine();

            System.out.print("Destination: ");
            String d = sc.nextLine();

            System.out.print("Seats: ");
            int seats = sc.nextInt();

            System.out.print("Price: ");
            double price = sc.nextDouble();

            String q = "INSERT INTO buses_table VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(q);

            ps.setInt(1, id);
            ps.setString(2, s);
            ps.setString(3, d);
            ps.setInt(4, seats);
            ps.setInt(5, seats);
            ps.setDouble(6, price);

            ps.executeUpdate();

            System.out.println("Bus Added!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void viewAllTickets() {
        try {
            Connection con = DBConnection.getConnection();

            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM tickets");

            while (rs.next()) {
                System.out.println("| Ticket: " + rs.getInt(1) +
                        " | User: " + rs.getInt(2) +
                        " | Bus: " + rs.getInt(3) +
			" | Passenger: " + rs.getString(4)+
			" | Gender: " + rs.getString(7)+	
                        " | Seat: " + rs.getInt(5) +
			" | Date: " + rs.getDate(6));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
