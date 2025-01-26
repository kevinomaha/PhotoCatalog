package com.example.photocatalog.ui.projects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photocatalog.R
import com.example.photocatalog.data.models.Project
import com.example.photocatalog.databinding.ItemProjectBinding

class ProjectsAdapter(
    private val onProjectClick: (Project) -> Unit,
    private val onProjectOptionsClick: (Project, android.view.View) -> Unit
) : ListAdapter<Project, ProjectsAdapter.ProjectViewHolder>(ProjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectViewHolder(
        private val binding: ItemProjectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(project: Project) {
            binding.apply {
                projectName.text = project.name
                projectDescription.text = project.description
                
                // Load thumbnail if available
                project.thumbnailUrl?.let { url ->
                    Glide.with(projectThumbnail)
                        .load(url)
                        .centerCrop()
                        .into(projectThumbnail)
                }

                // Set photo count
                photoCount.text = root.context.resources.getQuantityString(
                    R.plurals.photo_count,
                    project.photoCount,
                    project.photoCount
                )

                // Set click listeners
                root.setOnClickListener { onProjectClick(project) }
                menuButton.setOnClickListener { onProjectOptionsClick(project, it) }
            }
        }
    }
}

class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
    override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
        return oldItem == newItem
    }
}
