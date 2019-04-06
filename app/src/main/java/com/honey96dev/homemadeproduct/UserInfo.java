package com.honey96dev.homemadeproduct;

public class UserInfo {
    public String UserID;
    public String Username;
    public String FirstName;
    public String LastName;
    public String Email;
    public String Password;
    public String City;
    public String Phone;
    public String Type;
    public String StoreID;
    public UserInfo() {
        this.UserID = "";
        this.Username = "";
        this.FirstName = "";
        this.LastName = "";
        this.Email = "";
        this.Password = "";
        this.City = "";
        this.Phone = "";
        this.Type = "";
        this.StoreID = "";
    }
    public UserInfo(String userID, String username, String firstName, String lastName, String email,
                    String city, String phone, String type, String storeID) {
        this.UserID = userID;
        this.Username = username;
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Email = email;
        this.Password = "";
        this.City = city;
        this.Phone = phone;
        this.Type = type;
        this.StoreID = storeID;
    }
}
