package com.app.deying.taskmanager.ui.view

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.app.deying.taskmanager.ui.adapter.RoutineAdapter
import com.app.deying.taskmanager.ui.clickListener.OnTaskItemClicked
import com.app.deying.taskmanager.utils.SwipeToDelete
import com.app.deying.taskmanager.viewModel.RoutineViewModel
import com.app.deying.taskmanager.viewModel.RoutineViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.welcome_activity.ad_container
import kotlinx.android.synthetic.main.welcome_activity.btnEditDone
import kotlinx.android.synthetic.main.welcome_activity.btnFab
import kotlinx.android.synthetic.main.welcome_activity.etDecsEdit
import kotlinx.android.synthetic.main.welcome_activity.etRoutineEdit
import kotlinx.android.synthetic.main.welcome_activity.ivSelectDateEdit
import kotlinx.android.synthetic.main.welcome_activity.ivSelectTimeEdit
import kotlinx.android.synthetic.main.welcome_activity.layoutEdit
import kotlinx.android.synthetic.main.welcome_activity.layoutEmptyList
import kotlinx.android.synthetic.main.welcome_activity.recyclerView
import kotlinx.android.synthetic.main.welcome_activity.toolbar
import kotlinx.android.synthetic.main.welcome_activity.tvDateEdit
import kotlinx.android.synthetic.main.welcome_activity.tvTimeEdit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

class WelcomeActivity : AppCompatActivity(), OnTaskItemClicked {

    private val routineList = mutableListOf<RoutineModel>()
    lateinit var routineAdapter: RoutineAdapter
    private lateinit var routineRoomDB: RoutineRoomDB
    private lateinit var routineDAO: RoutineDAO
    private lateinit var routineViewModel: RoutineViewModel
    var timeNotify: String = ""
    var message: String = ""

    ////
    private val APP_ID = "app7bcc0e86705f4832b0"
    private val ZONE_ID = "vzd544a10da756482d9e"//"INSERT_YOUR_BANNER_ZONE_ID_HERE"
    private val ZONE_ID_FULL = "vz8ede6137beda44469e"//"INSERT_YOUR_BANNER_ZONE_ID_HERE"
    private val TAG = "AdColonyBannerDemo"
    private var ad: AdColonyInterstitial? = null
    private var listener: AdColonyAdViewListener? = null
    private var listenerInterstitial: AdColonyInterstitialListener? = null
    private var adOptions: AdColonyAdOptions? = null
    private var adView: AdColonyAdView? = null
    private var adOptions1: AdColonyAppOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_activity)
        supportActionBar?.hide()
        setSupportActionBar(toolbar)

        setUpRecyclerView()
        initialize()

        btnFab.setOnClickListener {
            if (this@WelcomeActivity.ad != null) {
                Handler(Looper.myLooper()!!).postDelayed(Runnable {
                    this@WelcomeActivity.ad?.show()
                }, 100L)
            }
        }

        routineViewModel.getRoutines().observe(this, Observer {
            routineList.clear()
            routineList.addAll(it)
            updateUI(routineList)
            routineAdapter.notifyDataSetChanged()
        })
        if (ad_container?.getChildCount()!! > 0) {
            ad_container?.removeView(adView)
        }
        initBannerAd()
    }

    private fun initialize() {
        routineDAO = RoutineRoomDB.getDatabaseObject(this).getRoutineDAO()
        val routineRepository = RoutineRepository(routineDAO)
        val routineViewModelFactory = RoutineViewModelFactory(routineRepository)
        routineViewModel =
            ViewModelProviders.of(this, routineViewModelFactory).get(RoutineViewModel::class.java)
    }

    private fun updateUI(routineModelList: List<RoutineModel>) {

        if (routineModelList.isEmpty()) {
            recyclerView.visibility = View.GONE
            layoutEmptyList.visibility = View.VISIBLE
        } else {
            layoutEmptyList.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setUpRecyclerView() {
        routineAdapter = RoutineAdapter(this, routineList, this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routineAdapter

            itemAnimator = SlideInUpAnimator().apply {
                addDuration = 300
            }
            swipeToDelete(this)
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeCallback = object : SwipeToDelete(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemClicked = routineAdapter.routineList[viewHolder.adapterPosition]
                routineViewModel.deleteRoutineData(itemClicked)
                restoreData(viewHolder.itemView, itemClicked)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreData(
        view: View,
        routineModel: RoutineModel
    ) {
        Snackbar.make(view, "Deleted ${routineModel.title}", Snackbar.LENGTH_LONG).also {
            it.apply {
                setActionTextColor(Color.parseColor("#1C1F27"))
                setAction("Undo") {
                    routineViewModel.addRoutineData(routineModel)
                }
                setBackgroundTint(Color.parseColor("#11CFC5"))
                show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        val searchView = menu!!.findItem(R.id.search_bar)
        val searchManager = searchView.actionView as SearchView
        searchManager.queryHint = "search task here"
        searchManager.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    routineViewModel.getSearchRoutine(query)
                        .observe(this@WelcomeActivity, Observer {
                            routineList.clear()
                            routineList.addAll(it)
                            updateUI(routineList)
                            routineAdapter.notifyDataSetChanged()
                        })
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteAll -> {
                deleteAllRoutines()
            }





            R.id.search_bar -> {
                showToast("currently not available")
            }
        }
        return true
    }

    override fun onEditClicked(routineModel: RoutineModel) {
        layoutEdit.visibility = View.VISIBLE
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutEdit).apply {
            peekHeight = 200
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        ivSelectTimeEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val timePickerDialog = TimePickerDialog(
                this,
                { timePicker, i, i1 ->
                    timeNotify = "$i:$i1"
                    tvTimeEdit.text = FormatTime(i, i1).toString()
                }, hour, minute, false
            )
            timePickerDialog.show()
        }

        ivSelectDateEdit.setOnClickListener(View.OnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]
            val datePickerDialog = DatePickerDialog(
                this,
                { datePicker, year, month, day ->
                    tvDateEdit.text = day.toString() + "-" + (month + 1) + "-" + year
                }, year, month, day
            )
            datePickerDialog.show()
        })

        btnEditDone.setOnClickListener {

            val newTitle = etRoutineEdit.text.toString()
            val newDecs = etDecsEdit.text.toString()
            val newDate = tvDateEdit.text.toString()
            val newTime = tvTimeEdit.text.toString()

            routineModel.apply {
                title = newTitle
                decs = newDecs
                date = newDate
                time = newTime
                priority = "High"
            }
            CoroutineScope(Dispatchers.IO).launch {
                routineViewModel.updateRoutineData(routineModel)
            }

            setAlarm(newTitle, newDecs, newDate, newTime)
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        }
    }

    override fun onDeleteClicked(routineModel: RoutineModel) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Do you want to remove this task??")
            setPositiveButton("Yes") { _, _ ->
                routineViewModel.deleteRoutineData(routineModel)
                showToast("Task deleted successfully")
            }
            setNegativeButton("No") { _, _ -> }
            create()
            show()
        }
    }

    override fun onTaskClick(routineModel: RoutineModel) {
        showToast("${routineModel.title} clicked")
    }

    private fun deleteAllRoutines() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Do you want to remove all task??")
            setPositiveButton("Yes") { _, _ ->
                if (routineList.size == 0) {
                    showToast("List is already empty")
                } else {
                    routineViewModel.deleteAllRoutines()
                    showToast("Task Deleted Successfully")
                }
            }
            setNegativeButton("No") { _, _ -> }
            create()
            show()
        }
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

    private fun initBannerAd() {
        adOptions1 = AdColonyAppOptions()
            .setUserID("unique_user_id")
            .setKeepScreenOn(true)

        AdColony.configure(this, adOptions1, APP_ID)
        listener = object : AdColonyAdViewListener() {
            override fun onRequestFilled(adColonyAdView: AdColonyAdView) {
                Log.d(TAG, "onRequestFilled")
                ad_container?.addView(adColonyAdView)
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


    private fun requestBannerAd() {
        // Optional Ad specific options to be sent with request
        adOptions = AdColonyAdOptions()
        //Request Ad
        AdColony.requestAdView(ZONE_ID, listener!!, AdColonyAdSize.BANNER, adOptions)
    }

    fun initFull() {
        listenerInterstitial = object : AdColonyInterstitialListener() {
            override fun onRequestFilled(ad: AdColonyInterstitial) {
                // Ad passed back in request filled callback, ad can now be shown
                this@WelcomeActivity.ad = ad
                //this@WelcomeActivity.ad!!.show()
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

            override fun onClosed(ad: AdColonyInterstitial?) {
                super.onClosed(ad)
                val intent = Intent(this@WelcomeActivity, AddTaskActivity::class.java);
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initFull()
        if (ad == null || ad!!.isExpired) {

            AdColony.requestInterstitial(ZONE_ID_FULL, listenerInterstitial!!, adOptions)
        }

    }

}
