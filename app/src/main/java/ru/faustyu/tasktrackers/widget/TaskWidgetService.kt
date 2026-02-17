package ru.faustyu.tasktrackers.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.runBlocking
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.AppDatabase
import ru.faustyu.tasktrackers.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskWidgetFactory(applicationContext)
    }
}

class TaskWidgetFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<Task> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val db = AppDatabase.getDatabase(context)
        tasks = runBlocking {
            db.taskDao().getAllTasksForExport()
                .filter { !it.task.isArchived && !it.task.isCompleted }
                .sortedBy { it.task.dueDate ?: Long.MAX_VALUE }
                .take(10)
                .map { it.task }
        }
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        views.setTextViewText(R.id.widget_item_title, task.title)

        // Color dot
        if (task.colorHex != null) {
            try {
                views.setInt(R.id.widget_item_color_dot, "setBackgroundColor", Color.parseColor(task.colorHex))
                views.setViewVisibility(R.id.widget_item_color_dot, View.VISIBLE)
            } catch (e: Exception) {
                views.setViewVisibility(R.id.widget_item_color_dot, View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_item_color_dot, View.GONE)
        }

        // Due date
        if (task.dueDate != null) {
            views.setTextViewText(R.id.widget_item_due, dateFormat.format(Date(task.dueDate)))
            views.setViewVisibility(R.id.widget_item_due, View.VISIBLE)
            // Red if overdue
            if (task.dueDate < System.currentTimeMillis()) {
                views.setTextColor(R.id.widget_item_due, Color.parseColor("#EF5350"))
            }
        } else {
            views.setViewVisibility(R.id.widget_item_due, View.GONE)
        }

        // Fill intent for the click on this item
        val fillIntent = Intent()
        views.setOnClickFillInIntent(R.id.widget_item_container, fillIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = tasks[position].taskId
    override fun hasStableIds(): Boolean = true
}
