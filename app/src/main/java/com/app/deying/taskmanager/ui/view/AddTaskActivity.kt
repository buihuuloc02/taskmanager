package com.app.deying.taskmanager.ui.view

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.adcolony.sdk.AdColony
import com.adcolony.sdk.AdColonyAdOptions
import com.adcolony.sdk.AdColonyAdSize
import com.adcolony.sdk.AdColonyAdView
import com.adcolony.sdk.AdColonyAdViewListener
import com.adcolony.sdk.AdColonyAppOptions
import com.adcolony.sdk.AdColonyInterstitial
import com.adcolony.sdk.AdColonyInterstitialListener
import com.adcolony.sdk.AdColonyZone
import com.app.deying.taskmanager.R
import com.app.deying.taskmanager.data.model.RoutineModel
import com.app.deying.taskmanager.data.repository.RoutineRepository
import com.app.deying.taskmanager.data.roomDB.RoutineDAO
import com.app.deying.taskmanager.data.roomDB.RoutineRoomDB
import com.app.deying.taskmanager.notification.NotificationBroadcast
import com.app.deying.taskmanager.viewModel.RoutineViewModel
import com.app.deying.taskmanager.viewModel.RoutineViewModelFactory
import kotlinx.android.synthetic.main.activity_add_task.*
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {
    private val APP_ID = "app7bcc0e86705f4832b0"
    private val ZONE_ID = "vzd544a10da756482d9e"//"INSERT_YOUR_BANNER_ZONE_ID_HERE"
    private val ZONE_ID_FULL = "vz8ede6137beda44469e"//"INSERT_YOUR_BANNER_ZONE_ID_HERE"
    private var listenerInterstitial: AdColonyInterstitialListener? = null
    private var adOptions: AdColonyAdOptions? = null
    private var ad: AdColonyInterstitial? = null
    private var adView: AdColonyAdView? = null
    private var listener: AdColonyAdViewListener? = null
    private var adOptions1: AdColonyAppOptions? = null
    private val TAG = "AddTaskActivity"
    private lateinit var routineRoomDB: RoutineRoomDB
    private lateinit var routineDAO: RoutineDAO
    private lateinit var routineViewModel: RoutineViewModel
    var timeNotify: String = ""
    var message: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        routineDAO = this.let { RoutineRoomDB.getDatabaseObject(it).getRoutineDAO() }
        val routineRepository = RoutineRepository(routineDAO)
        val routineViewModelFactory = RoutineViewModelFactory(routineRepository)
        routineViewModel =
            ViewModelProviders.of(this, routineViewModelFactory).get(RoutineViewModel::class.java)

        ivSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val timePickerDialog = TimePickerDialog(
                this,
                { timePicker, i, i1 ->
                    timeNotify = "$i:$i1"
                    tvTime.text = FormatTime(i, i1).toString()
                }, hour, minute, false
            )
            timePickerDialog.show()
        }

        ivSelectDate.setOnClickListener(View.OnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]
            val datePickerDialog = DatePickerDialog(
                this,
                { datePicker, year, month, day ->
                    tvDate.text = day.toString() + "-" + (month + 1) + "-" + year
                }, year, month, day
            )
            datePickerDialog.show()
        })

        ivPriority.setOnClickListener {
            val title = etRoutine.text.toString()
            if (title.isNotEmpty()){
                showToast("$title added to high priority")
            }else{
                showToast("Added to High priority")
            }
        }

        btnTaskDone.setOnClickListener {

            val title = etRoutine.text.toString()
            val decs = etDecs.text.toString()
            val date = tvDate.text.toString()
            val time = tvTime.text.toString()

            if (title.isNotEmpty() && decs.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                val routineModel = RoutineModel(title, decs, date, time, "High")
                routineViewModel.addRoutineData(routineModel)
            } else {
                showToast("check all fields")
            }

            setAlarm(title, decs, date, time)
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
        if (ad_containerAdd?.getChildCount()!! > 0) {
            ad_containerAdd?.removeView(adView)
        }
        adOptions1 = AdColonyAppOptions()
            .setUserID("unique_user_id")
            .setKeepScreenOn(true)

        initBannerAd()
        AdColony.configure(this, adOptions1, APP_ID)
    }

    private fun setAlarm(title: String, decs: String, date: String, time: String) {
        val alarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, NotificationBroadcast::class.java)
        intent.putExtra("title", title)
        intent.putExtra("decs", decs)
        intent.putExtra("date", date)
        intent.putExtra("time", time)

        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val datetime: String = "$date  $timeNotify"
        val formatter: DateFormat = SimpleDateFormat("d-M-yyyy hh:mm")

        try {
            val date1 = formatter.parse(datetime)
            alarmManager.set(AlarmManager.RTC_WAKEUP, date1.time, pendingIntent)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun FormatTime(hour: Int, minute: Int): String {
        var time: String = ""
        val formattedMinute: String = if (minute / 10 == 0) {
            "0$minute"
        } else {
            "" + minute
        }
        time = when {
            hour == 0 -> {
                "12:$formattedMinute AM"
            }
            hour < 12 -> {
                "$hour:$formattedMinute AM"
            }
            hour == 12 -> {
                "12:$formattedMinute PM"
            }
            else -> {
                val temp = hour - 12
                "$temp:$formattedMinute PM"
            }
        }
        return time
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun initFull(){
        listenerInterstitial = object : AdColonyInterstitialListener() {
            override fun onRequestFilled(ad: AdColonyInterstitial) {
                // Ad passed back in request filled callback, ad can now be shown
                this@AddTaskActivity.ad = ad
                if(firstLoad) {
                    this@AddTaskActivity.ad!!.show()
                    firstLoad = false
                }
                Log.d(TAG, "onRequestFilled")
            }

            override fun onRequestNotFilled(zone: AdColonyZone) {
                // Ad request was not filled
                Log.d(TAG, "onRequestNotFilled")
            }

            override fun onOpened(ad: AdColonyInterstitial) {
                // Ad opened, reset UI to reflect state change
                Log.d(TAG, "onOpened")
            }

            override fun onExpiring(ad: AdColonyInterstitial) {
                // Request a new ad if ad is expiring
                AdColony.requestInterstitial(ZONE_ID_FULL, this, adOptions)
                Log.d(TAG, "onExpiring")
            }
        }
    }

    var firstLoad: Boolean = true
    override fun onResume() {
        super.onResume()

        if (ad == null || ad!!.isExpired) {

            //AdColony.requestInterstitial(ZONE_ID_FULL, listenerInterstitial!!, adOptions)
        }
        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            if (firstLoad) {
                //this@AddTaskActivity.ad?.show()
                //firstLoad = false
            }
        }, 1000L)
    }
    private fun requestBannerAd() {
        // Optional Ad specific options to be sent with request
        adOptions = AdColonyAdOptions()
        //Request Ad
        AdColony.requestAdView(ZONE_ID, listener!!, AdColonyAdSize.BANNER, adOptions)
    }
    override fun onDestroy() {
        super.onDestroy()
        firstLoad = true
    }
    private fun initBannerAd() {
        adOptions1 = AdColonyAppOptions()
            .setUserID("unique_user_id")
            .setKeepScreenOn(true)

        AdColony.configure(this, adOptions1, APP_ID)
        listener = object : AdColonyAdViewListener() {
            override fun onRequestFilled(adColonyAdView: AdColonyAdView) {
                Log.d(TAG, "onRequestFilled")
                ad_containerAdd?.addView(adColonyAdView)
                adView = adColonyAdView
            }

            override fun onRequestNotFilled(zone: AdColonyZone) {
                super.onRequestNotFilled(zone)
                Log.d(TAG, "onRequestNotFilled")
            }

            override fun onOpened(ad: AdColonyAdView) {
                super.onOpened(ad)
                Log.d(TAG, "onOpened")
            }

            override fun onClosed(ad: AdColonyAdView) {
                super.onClosed(ad)
                Log.d(TAG, "onClosed")
            }

            override fun onClicked(ad: AdColonyAdView) {
                super.onClicked(ad)
                Log.d(TAG, "onClicked")
            }

            override fun onLeftApplication(ad: AdColonyAdView) {
                super.onLeftApplication(ad)
                Log.d(TAG, "onLeftApplication")
            }
        }

        Handler(Looper.myLooper()!!).postDelayed(
            Runnable {
                requestBannerAd()
            }, 200
        )
    }
    override fun onBackPressed() {
        super.onBackPressed()
        firstLoad = true
    }

}