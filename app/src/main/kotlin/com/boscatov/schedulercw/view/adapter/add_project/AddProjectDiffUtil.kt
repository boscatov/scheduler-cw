package com.boscatov.schedulercw.view.adapter.add_project

import androidx.recyclerview.widget.DiffUtil
import com.boscatov.schedulercw.data.entity.Project

/**
 * Created by boscatov on 14.04.2019.
 */
class AddProjectDiffUtil(
    private val oldList: List<Project>,
    private val newList: List<Project>
): DiffUtil.Callback() {
    override fun getNewListSize(): Int = newList.count()
    override fun getOldListSize(): Int = oldList.count()

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return old.projectName == new.projectName
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old.projectId == new.projectId
    }
}