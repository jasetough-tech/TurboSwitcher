package com.turboswitcher.domain.workflows

import com.turboswitcher.data.model.Workflow
import com.turboswitcher.data.model.WorkflowStep
import com.turboswitcher.data.repository.WorkflowRepository
import timber.log.Timber
import java.util.UUID

class WorkflowRecorder(private val workflowRepository: WorkflowRepository) {

    private var currentSteps = mutableListOf<WorkflowStep>()
    private var isRecording = false

    fun startRecording() {
        currentSteps.clear()
        isRecording = true
        Timber.d("Workflow recording started")
    }

    fun recordStep(
        surfaceType: String,
        appPackage: String?,
        actionType: String,
        checkpointRequired: Boolean = false
    ) {
        if (!isRecording) {
            Timber.w("Attempted to record step while not recording")
            return
        }
        
        currentSteps.add(
            WorkflowStep(
                id = UUID.randomUUID().toString(),
                order = currentSteps.size,
                surfaceType = surfaceType,
                appPackage = appPackage,
                actionType = actionType,
                checkpointRequired = checkpointRequired,
                confidence = 0f
            )
        )
        Timber.d("Workflow step recorded: action=$actionType, package=$appPackage")
    }

    suspend fun saveWorkflow(name: String, targetPackage: String): Long? {
        if (currentSteps.isEmpty()) {
            Timber.w("Attempted to save workflow with no steps")
            return null
        }
        
        return try {
            val workflow = Workflow(
                name = name,
                targetPackage = targetPackage,
                steps = currentSteps.toList(),
                lastSuccess = System.currentTimeMillis()
            )
            val id = workflowRepository.save(workflow)
            isRecording = false
            currentSteps.clear()
            Timber.d("Workflow saved: name=$name, id=$id, steps=${workflow.steps.size}")
            id
        } catch (e: Exception) {
            Timber.e(e, "Failed to save workflow: $name")
            null
        }
    }

    fun cancelRecording() {
        isRecording = false
        currentSteps.clear()
        Timber.d("Workflow recording cancelled")
    }

    fun isCurrentlyRecording(): Boolean = isRecording
}
