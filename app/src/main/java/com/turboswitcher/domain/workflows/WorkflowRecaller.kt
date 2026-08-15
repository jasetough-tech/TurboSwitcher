package com.turboswitcher.domain.workflows

import com.turboswitcher.data.model.WorkflowStep
import com.turboswitcher.data.repository.WorkflowRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

class WorkflowRecaller(private val workflowRepository: WorkflowRepository) {
    
    suspend fun getNextStep(workflowId: Long, currentStepIndex: Int): WorkflowStep? {
        return try {
            val workflow = workflowRepository.observeAll().first()
                .firstOrNull { it.id == workflowId }
            
            if (workflow == null) {
                Timber.w("Workflow not found: $workflowId")
                return null
            }
            
            val nextStep = workflow.steps.getOrNull(currentStepIndex + 1)
            if (nextStep != null) {
                Timber.d("Next step retrieved: workflow=$workflowId, index=${currentStepIndex + 1}")
            }
            nextStep
        } catch (e: Exception) {
            Timber.e(e, "Failed to get next step for workflow: $workflowId")
            null
        }
    }

    suspend fun getWorkflowSteps(workflowId: Long): List<WorkflowStep> {
        return try {
            val workflow = workflowRepository.observeAll().first()
                .firstOrNull { it.id == workflowId }
            
            workflow?.steps ?: emptyList().also {
                Timber.w("Workflow not found: $workflowId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get workflow steps: $workflowId")
            emptyList()
        }
    }
}
