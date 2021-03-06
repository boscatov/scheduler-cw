package com.boscatov.schedulercw.view.ui.dialog.add_project

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.boscatov.schedulercw.R
import com.boscatov.schedulercw.data.entity.Project
import com.boscatov.schedulercw.view.adapter.add_project.AddProjectAdapter
import com.boscatov.schedulercw.view.viewmodel.add_project.AddProjectViewModel
import kotlinx.android.synthetic.main.dialog_add_project.*

/**
 * Created by boscatov on 14.04.2019.
 */

class AddProjectDialogFragment : DialogFragment(), AddProjectAdapter.Callback {
    companion object {
        fun getInstance(): DialogFragment {
            return AddProjectDialogFragment()
        }
    }

    private lateinit var projectViewModel: AddProjectViewModel
    private val adapter = AddProjectAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectViewModel = ViewModelProviders.of(this).get(AddProjectViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_add_project, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectViewModel.showProjects.observe(this, Observer {
            adapter.setProjects(it)
        })
        dialogAddProjectRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AddProjectDialogFragment.adapter
        }
        dialogAddProjectET.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                projectViewModel.onTextChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })
        adapter.setOnProjectSelectListener(this)
        projectViewModel.onLoadProjects()
    }

    override fun onProjectSelect(project: Project) {
        val intent = Intent("com.boscatov.schedulercw.add_project")
        intent.putExtra("PROJECT_ID", project.projectId)
        intent.putExtra("PROJECT_NAME", project.projectName)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        dismiss()
    }

    override fun onResume() {
        super.onResume()
        dialog.window?.setLayout(800, 1200)
    }
}