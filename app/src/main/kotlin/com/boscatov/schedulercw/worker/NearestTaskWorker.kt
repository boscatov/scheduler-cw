package com.boscatov.schedulercw.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.boscatov.schedulercw.Actions
import com.boscatov.schedulercw.R
import com.boscatov.schedulercw.data.entity.TASK_ACTION
import com.boscatov.schedulercw.data.entity.Task
import com.boscatov.schedulercw.data.entity.TaskAction
import com.boscatov.schedulercw.data.entity.TaskStatus
import com.boscatov.schedulercw.di.Scopes
import com.boscatov.schedulercw.interactor.task.TaskInteractor
import com.boscatov.schedulercw.receiver.NotificationTaskReceiver
import toothpick.Toothpick
import java.text.SimpleDateFormat
import javax.inject.Inject


class NearestTaskWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    @Inject
    lateinit var taskInteractor: TaskInteractor

    init {
        val scope = Toothpick.openScope(Scopes.TASK_SCOPE)
        Toothpick.inject(this, scope)
    }

    override fun doWork(): Result {
        sendNotificationStart(taskInteractor.getNearestTask(arrayOf(TaskStatus.PENDING, TaskStatus.ACTIVE)))
        return Result.success()
    }

    private fun sendNotificationStart(task: Task?) {
        createNotificationChannel()
        task?.let {
            if (it.taskStatus == TaskStatus.ACTIVE) return
        }

        val remoteViews: RemoteViews
        if (task != null && task.taskStatus == TaskStatus.PENDING && task.taskDateStart != null) {
            remoteViews = RemoteViews(context.packageName, R.layout.notification_start)
            remoteViews.setTextViewText(R.id.notificationStartTitleTV, task.taskTitle)
            remoteViews.setTextViewText(
                R.id.notificationStartTimeTV,
                SimpleDateFormat("HH:mm").format(task.taskDateStart)
            )
            val intent = Intent(Actions.NOTIFICATION_TASK).also {
                it.putExtra(TASK_ACTION, TaskAction.START.ordinal)
                it.setClass(context, NotificationTaskReceiver::class.java)
            }

            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.notificationStartIB, pendingIntent)

            val intent2 = Intent(Actions.NOTIFICATION_TASK).also {
                it.putExtra(TASK_ACTION, TaskAction.ABANDON.ordinal)
                it.setClass(context, NotificationTaskReceiver::class.java)
            }
            val pendingIntent2 = PendingIntent.getBroadcast(context, 1, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.notificationStartCancelIB, pendingIntent2)
        } else {
            remoteViews = RemoteViews(context.packageName, R.layout.notification_no_tasks)
            remoteViews.setTextViewText(R.id.notificationNoTaskTitleTV, "No recent tasks")
            remoteViews.setTextViewText(R.id.notificationNoTaskTimeTV, "You've done all tasks yet")
        }
        val builder = NotificationCompat.Builder(context, PERIODIC_TASK_NOTIFICATION_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            val notification = builder.build()
            notification.flags = Notification.FLAG_ONGOING_EVENT
            notify(TASK_MONITOR_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(PERIODIC_TASK_NOTIFICATION_ID, "Tasks", NotificationManager.IMPORTANCE_DEFAULT)
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            mNotificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val PERIODIC_TASK_NOTIFICATION_ID = "TASK_CHANNEL"
        const val TASK_MONITOR_ID = 672
    }
}