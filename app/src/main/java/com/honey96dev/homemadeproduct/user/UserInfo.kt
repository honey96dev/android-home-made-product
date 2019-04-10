package com.honey96dev.homemadeproduct.user

class UserInfo {
    var UserID: String
    var Username: String
    var FirstName: String
    var LastName: String
    var Email: String
    var Password: String
    var City: String
    var Phone: String
    var Type: String
    var StoreID: String

    constructor() {
        this.UserID = ""
        this.Username = ""
        this.FirstName = ""
        this.LastName = ""
        this.Email = ""
        this.Password = ""
        this.City = ""
        this.Phone = ""
        this.Type = ""
        this.StoreID = ""
    }

    constructor(userID: String, username: String, firstName: String, lastName: String, email: String,
                city: String, phone: String, type: String, storeID: String) {
        this.UserID = userID
        this.Username = username
        this.FirstName = firstName
        this.LastName = lastName
        this.Email = email
        this.Password = ""
        this.City = city
        this.Phone = phone
        this.Type = type
        this.StoreID = storeID
    }
}
