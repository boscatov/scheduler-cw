package com.boscatov.schedulercw.view.ui.fragment.task_list

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.boscatov.schedulercw.R
import com.boscatov.schedulercw.data.entity.Task
import com.boscatov.schedulercw.view.adapter.task.TaskAdapter
import com.boscatov.schedulercw.view.viewmodel.holder.MainViewModel
import com.boscatov.schedulercw.view.viewmodel.task_list.TaskListViewModel
import kotlinx.android.synthetic.main.fragment_task_list.*
import java.text.SimpleDateFormat
import java.util.*


class TaskListFragment : Fragment(), TaskAdapter.ItemTouch.SwipeCallback, TaskAdapter.OnTaskClickListener {
    lateinit var mainViewModel: MainViewModel
    lateinit var taskListViewModel: TaskListViewModel
    private val taskListAdapter: TaskAdapter = TaskAdapter(arrayListOf())
    private val receiver = UpdateReceiver()
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = activity?.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        taskListViewModel = ViewModelProviders.of(this).get(TaskListViewModel::class.java)

        taskListFragmentRV.apply {
            layoutManager = LinearLayoutManager(this@TaskListFragment.requireContext())
            adapter = taskListAdapter
        }

        taskListViewModel.tasks.observe(this, Observer<List<Task>> {
            taskListAdapter.setTasks(it)
            val id = taskListViewModel.getCurrentTaskId()
            id?.let { taskPosition ->
                if (taskPosition >= 0) {
                    Handler().postDelayed({
                        taskListFragmentRV?.layoutManager?.scrollToPosition(taskPosition)
                    }, 200L)
                }
            }
        })

        taskListFragmentRV.itemAnimator = null
        taskListViewModel.day.observe(this, Observer {
            changeTitle(it)
            taskListViewModel.loadData()
        })
        taskListFragmentFAB.setOnClickListener {
            mainViewModel.onOpenNewTaskDialog()
            it.findNavController().navigate(R.id.action_taskListFragment_to_newTaskDialogFragment)
        }

        taskListFragmentDayBackIB.setOnClickListener {
            taskListViewModel.decreaseDate()
        }
        taskListFragmentDayForwardIB.setOnClickListener {
            taskListViewModel.increaseDate()
        }
        mainViewModel.state.observe(this, Observer {
            taskListViewModel.loadData()
        })
        val itemTouch = TaskAdapter.ItemTouch(requireContext())
        itemTouch.setListener(this)
        taskListAdapter.setOnClickListener(this)
        itemTouchHelper = ItemTouchHelper(itemTouch)
        itemTouchHelper.attachToRecyclerView(taskListFragmentRV)
        taskListViewModel.loadData()
    }

    override fun onChaosSwiped(position: Int) {
        taskListViewModel.onChaosSwipe(position)
    }

    override fun onDoneSwipe(position: Int) {
        taskListViewModel.onDoneSwipe(position)
    }

    override fun onTaskClicked(taskId: Long) {
        mainViewModel.onOpenNewTaskDialog()
        val bundle = Bundle()
        bundle.putLong("TASK_ID", taskId)
        findNavController().navigate(R.id.action_taskListFragment_to_editTaskDialogFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter("com.boscatov.schedulercw.updatelist")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        super.onPause()
    }

    private fun changeTitle(date: Date) {
        val formatter = SimpleDateFormat("dd")
        taskListFragmentTitle.setText(formatter.format(date))
    }

    inner class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    "com.boscatov.schedulercw.updatelist" -> {
                        Handler().postDelayed(
                            {taskListViewModel.loadData()}
                            , 200L
                        )
                    }
                    else -> return
                }
            }
        }
    }
}