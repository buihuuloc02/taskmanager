package com.app.deying.taskmanager.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.deying.taskmanager.R
import kotlinx.android.synthetic.main.activity_notification_message.*

class NotificationMessage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_message)

        val bundle = intent.extras
        tv_message.text = bundle!!.getString("message")
    }
}