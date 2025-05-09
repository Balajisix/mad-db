package com.example.dbapp

data class Expense(
    var id: String = "",
    var title: String = "",
    var amount: Double = 0.0,
    var date: String = ""
)