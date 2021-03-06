package com.boscatov.schedulercw.view.ui.activity.holder

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.boscatov.schedulercw.R
import com.boscatov.schedulercw.view.ui.fragment.chaos.ChaosFragment
import com.boscatov.schedulercw.view.ui.fragment.stats.StatsFragment
import com.boscatov.schedulercw.view.ui.fragment.task_list.TaskListFragment
import com.boscatov.schedulercw.view.ui.state.DefaultState
import com.boscatov.schedulercw.view.ui.state.NewTaskState
import com.boscatov.schedulercw.view.ui.state.State
import com.boscatov.schedulercw.view.viewmodel.holder.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_holder.*

class HolderActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var mainViewModel: MainViewModel
    private var toolbarMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holder)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        activityHolderBottomNV.setOnNavigationItemSelectedListener(this)
        mainViewModel.state.observe(this, Observer {
            changeState(it)
        })
        initializeBottomNavigationView()
        setupToolbar()
        initWorkers()
        changeToDefault()
        mainViewModel.testNetwork()

        nav_view.setNavigationItemSelectedListener {
            it.isChecked = true
            when(it.itemId) {
                R.id.navigationDrawerProjectAction -> navController.navigate(R.id.projectFragment)
            }
            activityHolderDrawerLayout.closeDrawers()

            return@setNavigationItemSelectedListener true
        }
    }

    // TODO: Перенести в Preferences
    private fun initWorkers() {
        mainViewModel.initNotificationWorker()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottomMenuChaosAction -> navController.navigate(R.id.calendarFragment)
            R.id.bottomMenuHomeAction -> navController.navigate(R.id.taskListFragment)
            R.id.bottomMenuStatsAction -> navController.navigate(R.id.statsFragment)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                if (mainViewModel.state.value is DefaultState) {
                    activityHolderDrawerLayout.openDrawer(GravityCompat.START)
                }
                else {
                    navController.navigateUp()
                    mainViewModel.onCloseNewTaskDialog()
                }
                true
            }
            R.id.toolbarDone -> {
                if(mainViewModel.state.value is NewTaskState) {
                    navController.navigateUp()
                    mainViewModel.onAcceptNewTask()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeBottomNavigationView() {
        val id = when (navController.currentDestination?.label) {
            TaskListFragment::class.java.simpleName -> R.id.bottomMenuHomeAction
            ChaosFragment::class.java.simpleName -> R.id.bottomMenuChaosAction
            StatsFragment::class.java.simpleName -> R.id.bottomMenuStatsAction
            else -> R.id.bottomMenuHomeAction
        }
        activityHolderBottomNV.selectedItemId = id
    }

    private fun setupToolbar() {
        setSupportActionBar(activityHolderToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
    }

    private fun changeState(state: State) {
        when(state) {
            is DefaultState -> changeToDefault()
            is NewTaskState -> changeToNewTask()
        }
    }

    private fun changeToDefault() {
        activityHolderBottomNV.visibility = View.VISIBLE
        toolbarMenu?.findItem(R.id.toolbarDone)?.isVisible = false
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }

    }

    private fun changeToNewTask() {
        activityHolderBottomNV.visibility = View.GONE
        toolbarMenu?.findItem(R.id.toolbarDone)?.isVisible = true
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        toolbarMenu = menu
        changeToDefault()
        return super.onCreateOptionsMenu(menu)
    }
}