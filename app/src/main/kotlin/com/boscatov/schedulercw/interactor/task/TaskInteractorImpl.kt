package com.boscatov.schedulercw.interactor.task

import com.boscatov.schedulercw.data.entity.Task
import com.boscatov.schedulercw.data.repository.task.TaskRepository
import com.boscatov.schedulercw.di.Scopes
import toothpick.Toothpick
import javax.inject.Inject

class TaskInteractorImpl : TaskInteractor {

    @Inject
    lateinit var taskRepository: TaskRepository

    init {
        val scope = Toothpick.openScope(Scopes.TASK_SCOPE)
        Toothpick.inject(this, scope)
    }

    override fun getTasks(): List<Task> {
        return taskRepository.getTasks()
    }

    override fun getNearestTask(): Task? {
        return taskRepository.getNearestTask()
    }

    override fun saveTask(task: Task) {
        taskRepository.saveTask(task)
    }
}