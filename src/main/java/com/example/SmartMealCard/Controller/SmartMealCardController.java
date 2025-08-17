package com.example.SmartMealCard.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SmartMealCardController {
    public String RollNo;
    public String veg;
    public String jdbcurl = "jdbc:mysql://127.0.0.1:3306/smartmeal";

    @GetMapping("/start")
    public String start() {
        RollNo = null;
        return "login";
    }

    @GetMapping("/hello")
    public String hello() {
        return "login";
    }

    @GetMapping("/create")
    public String create() {
        return "createAcc";
    }

    @GetMapping("/adminLogin")
    public String adminLogin() {
        return "adminLogin";
    }

    @GetMapping("/studentLogin")
    public String studentLogin() {
        return "login";
    }


    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/searchmaindish")
    public String searchmaindish() {
        return "search";
    }

    @GetMapping("/searchsidedish")
    public String searchsidedish() {
        return "searchsidedish";
    }

    @GetMapping("/searchsnack")
    public String searchsnack() {
        return "searchsnacks";
    }

    @GetMapping("/addAccount_page")
    public String addAccount_page() {
        return "addAcc";
    }

    @GetMapping("/studentdata_page")
    public String studentdata_page() {
        return "studentdata";
    }

    @GetMapping("/addAlert")
    public String addAlert() {
        return "addAcc";
    }

    @GetMapping("/editAlert")
    public String editAlert() {
        return "profile";
    }


    @GetMapping("/foodData")
    public String foodData() {
        return "foodData";
    }

    @GetMapping("/addFood_page")
    public String addFood_page() {
        return "addFood";
    }


    @PostMapping("/submit")
    public String submit(@RequestParam("RollNo") String RollNo, @RequestParam("password") String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "select pass from students where RollNo=?";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, RollNo);
            ResultSet rs = pstatement.executeQuery();
            if (rs.next()) {
                if (BCrypt.checkpw(password, rs.getString("pass"))) {
                    this.RollNo = RollNo;
                    return "home";
                } else {
                    return "alert";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "createAcc";
    }


    @PostMapping("/Adminsubmit")
    public String Adminsubmit(@RequestParam("username") String username, @RequestParam("password") String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "select pass from admintable where username=?";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, username);
            ResultSet rs = pstatement.executeQuery();
            if (rs.next()) {
                if (password.equals(rs.getString("pass"))) {
                    return "index";
                } else {
                    return "alert";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "adminLogin";
    }

    @PostMapping("/createAcc")
    public String createAcc(@RequestParam("Rollno") String Rollno, @RequestParam("name") String name, @RequestParam("dept") String dept, @RequestParam("mealtype") String mealtype, @RequestParam("Password") String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "insert into students(RollNo,name,meal,pass,hasRecieved,department) values(?,?,?,?,?,?)";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, Rollno);
            pstatement.setString(2, name);
            pstatement.setString(3, mealtype);
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            pstatement.setString(4, hashedPassword);
            pstatement.setInt(5, 0);
            pstatement.setString(6, dept);

            pstatement.execute();
        } catch (SQLException e) {
            // Check if the exception is due to a duplicate entry (integrity constraint violation)
            // SQLState "23000" is standard for integrity constraint violations.
            if ("23000".equals(e.getSQLState())) {
                return "existAlert"; // Return a view indicating the account already exists
            }
            // For other SQL errors, print the stack trace
            e.printStackTrace();
        } catch (Exception e) {
            // For any other exceptions, print the stack trace
            e.printStackTrace();
        }
        return "login";
    }

    @PostMapping("/maindish")
    public String searchmaindish(@RequestParam("search") String search) {
        List<Map<String, Object>> data = searchmaindata(search);
        return "search";
    }

    @ModelAttribute("searchmainList")
    public List<Map<String, Object>> searchmaindata(String search) {
        int f = 0;
        List<Map<String, Object>> listofdata = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "root")) {
            String sql = "select * from students where RollNo=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, search);
            ResultSet rs = statement.executeQuery();
            if (rs.next() && f < 1) {
                Map<String, Object> mp = new HashMap<>();
                if (rs.getInt("hasRecieved") == 0) {
                    mp.put("Name", rs.getString("name"));
                    if (rs.getString("meal").equals("NV")) {
                        mp.put("mealtype", "Non-Veg");
                    } else {
                        mp.put("mealtype", "Veg");
                    }
                    mp.put("RollNo", rs.getString("RollNo"));
                    mp.put("status", "Your meal/snack has been provided");
                    mp.put("dept", rs.getString("department"));
                    sql = "UPDATE students SET hasRecieved = 1 WHERE RollNo = ?";
                    PreparedStatement pstatement = connection.prepareStatement(sql);
                    pstatement.setString(1, search);
                    pstatement.executeUpdate();
                    listofdata.add(mp);
                    f++;
                } else {
                    mp.put("Name", rs.getString("name"));
                    if (rs.getString("meal").equals("NV")) {
                        mp.put("mealtype", "Non-Veg");
                    } else {
                        mp.put("mealtype", "Veg");
                    }
                    mp.put("RollNo", rs.getString("RollNo"));
                    mp.put("status", "You have already received your meal/snack");
                    mp.put("dept", rs.getString("department"));
                    listofdata.add(mp);
                    f++;
                }


            }

        } catch (Exception e) {
            System.out.println("The exception occurred: " + e);
        }
        return listofdata;
    }

    @PostMapping("/addAccount")
    public String addAccount(@RequestParam("RollNo") String Rollno, @RequestParam("name") String name, @RequestParam("dept") String dept, @RequestParam("mealtype") String mealtype, @RequestParam("Password") String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "insert into students(RollNo,name,meal,pass,hasRecieved,department) values(?,?,?,?,?,?)";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, Rollno);
            pstatement.setString(2, name);
            pstatement.setString(3, mealtype);
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            pstatement.setString(4, hashedPassword);
            pstatement.setInt(5, 0);
            pstatement.setString(6, dept);

            pstatement.execute();
        } catch (SQLException e) {
            // Check if the exception is due to a duplicate entry (integrity constraint violation)
            // SQLState "23000" is standard for integrity constraint violations.
            if ("23000".equals(e.getSQLState())) {
                return "existAlert"; // Return a view indicating the account already exists
            }
            // For other SQL errors, print the stack trace
            e.printStackTrace();
        } catch (Exception e) {
            // For any other exceptions, print the stack trace
            e.printStackTrace();
        }
        return "addAccAlert";
    }

    @PostMapping("/reset")
    public String reset() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "UPDATE students SET hasRecieved = 0";
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "resetAlert";
    }

    @GetMapping("/studentdata")
    public String studentdata() {
        List<Map<String, Object>> data = fetchProfile();
//      model.addAttribute("recipeList",data);
        return "studentdata";
    }

    @ModelAttribute("studentList")
    public List<Map<String, Object>> fetchProfile() {
        //System.out.println("The selected recipe is "+selectedRow);
        List<Map<String, Object>> listofinstructions = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "select * from students";
            //String uname=this.username;
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("Name", rs.getString("name"));
                mp.put("RollNo", rs.getString("RollNo"));
                mp.put("Department", rs.getString("department"));
                mp.put("VNV", rs.getString("meal"));
                listofinstructions.add(mp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listofinstructions;
    }

    @GetMapping("/deleteData")
    public String deleteData(@RequestParam("selectedRow") String selectedRow) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "DELETE FROM students WHERE RollNo=?";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, selectedRow);
            pstatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "deleteAlert";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        List<Map<String, Object>> data = searchprofiledata();
        return "profile";
    }

    @ModelAttribute("profilelist")
    public List<Map<String, Object>> searchprofiledata() {
        List<Map<String, Object>> listofdata = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "root")) {
            String sql = "select * from students where RollNo=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, this.RollNo);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("Name", rs.getString("name"));
                if (rs.getString("meal").equals("NV")) {
                    mp.put("mealtype", "Non-Veg");
                } else {
                    mp.put("mealtype", "Veg");
                }
                mp.put("RollNo", rs.getString("RollNo"));
                mp.put("dept", rs.getString("department"));
                listofdata.add(mp);
            }

        } catch (Exception e) {
            System.out.println("The exception occurred: " + e);
        }
        return listofdata;
    }

    @GetMapping("/editProf")
    public String profile() {
        List<Map<String, Object>> data = searcheditdata();
        return "editProfile";
    }

    @ModelAttribute("editList")
    public List<Map<String, Object>> searcheditdata() {
        List<Map<String, Object>> listofdata = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "root")) {
            String sql = "select * from students where RollNo=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, this.RollNo);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("Name", rs.getString("name"));
                mp.put("RollNo", rs.getString("RollNo"));
                mp.put("dept", rs.getString("department"));
                listofdata.add(mp);
            }

        } catch (Exception e) {
            System.out.println("The exception occurred: " + e);
        }
        return listofdata;
    }

    @PostMapping("/edited")
    public String edited(@RequestParam("RollNo") String Rollno, @RequestParam("name") String name, @RequestParam("dept") String dept, @RequestParam("mealtype") String mealtype, @RequestParam("Password") String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "UPDATE students SET RollNo = ?, name = ?, meal=?,pass=?,department=? WHERE RollNo = ?";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, Rollno);
            pstatement.setString(2, name);
            pstatement.setString(3, mealtype);
            pstatement.setString(4, password);
            pstatement.setString(5, dept);
            pstatement.setString(6, this.RollNo);

            pstatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "editAlert";
    }

    @ModelAttribute("countList")
    public List<Map<String, Object>> countList() {
        List<Map<String, Object>> listofdata = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "root")) {
            String sql = "select count(*) from students where meal='V'";
            Statement statement1 = connection.createStatement();
            ResultSet rs1 = statement1.executeQuery(sql);
            Map<String, Object> mp = new HashMap<>();
            if (rs1.next()) {
                mp.put("veg", rs1.getInt("count(*)"));
                listofdata.add(mp);
            }


            sql = "select count(*) from students where meal='NV'";
            Statement statement2 = connection.createStatement();
            ResultSet rs2 = statement2.executeQuery(sql);
            if (rs2.next()) {
                mp.put("nonveg", rs2.getInt("count(*)"));
                listofdata.add(mp);
            }


        } catch (Exception e) {
            System.out.println("The exception occurred: " + e);
        }
        return listofdata;
    }

    @GetMapping("/home")
    public String home() {
        List<Map<String, Object>> data = searchfooddata();
        return "home";
    }

    @ModelAttribute("foodList")
    public List<Map<String, Object>> searchfooddata() {
        List<Map<String, Object>> listofdata = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "root")) {
            String sql = "select * from food";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("veg", rs.getString("veg"));
                mp.put("nonveg", rs.getString("nonveg"));
                listofdata.add(mp);
            }

        } catch (Exception e) {
            System.out.println("The exception occurred: " + e);
        }
        return listofdata;
    }

    @PostMapping("/preference")
    public String preference(@RequestParam("mealtype") String mealtype) {
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "root")) {
            String sql = "UPDATE students SET meal=? WHERE RollNo = ?";
            try (PreparedStatement pstatement = connection.prepareStatement(sql)) {
                pstatement.setString(1, mealtype);
                pstatement.setString(2, this.RollNo);
                pstatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "changeAlert";
    }

    @PostMapping("/editFood")
    public String editFood(@RequestParam("selectedRow") String selectedRow) {
        this.veg=selectedRow;
        List<Map<String, Object>> data = searchfoodEditList(selectedRow);
        return "editFood";
    }

    @ModelAttribute("foodEditList")
    public List<Map<String, Object>> searchfoodEditList(String selectedRow) {
        List<Map<String, Object>> listofdata = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "root")) {
            String sql = "select * from food where veg=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, selectedRow);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("veg", rs.getString("veg"));
                mp.put("nonveg", rs.getString("nonveg"));
                listofdata.add(mp);
            }

        } catch (Exception e) {
            System.out.println("The exception occurred: " + e);
        }
        return listofdata;
    }

    @PostMapping("/editedFood")
    public String edited(@RequestParam("veg") String Veg, @RequestParam("nonveg") String nonveg) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "UPDATE food SET veg = ?, nonveg = ? WHERE veg = ?";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, Veg);
            pstatement.setString(2, nonveg);
            pstatement.setString(3, this.veg);
            pstatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "foodAlert";
    }

    @PostMapping("/addFood")
    public String addFood(@RequestParam("veg") String veg, @RequestParam("nonveg") String nonveg) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "insert into food(veg,nonveg) values(?,?)";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, veg);
            pstatement.setString(2, nonveg);
            pstatement.execute();
        } catch (Exception e) {
            return "existAlert";
        }
        return "foodAlert";
    }

    @GetMapping("/deleteFood")
    public String deleteData() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "root");
            String sql = "DELETE FROM food WHERE veg=?";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1, this.veg);
            pstatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "deleteFoodAlert";
    }
}
