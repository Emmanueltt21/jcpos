package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.OrderWithItems
import com.example.jetpackpos.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class ReportFilterType {
    TODAY, WEEK, MONTH, YEAR
}

data class ReportsUiState(
    val selectedFilter: ReportFilterType = ReportFilterType.TODAY,
    val transactions: List<OrderWithItems> = emptyList(), // For the list below chart
    val chartData: Any? = null, // Placeholder for actual chart data structure
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        // Initial load based on default filter
        loadReportData(ReportFilterType.TODAY)
    }

    fun onFilterChange(newFilter: ReportFilterType) {
        _uiState.update { it.copy(selectedFilter = newFilter, isLoading = true) }
        loadReportData(newFilter)
    }

    private fun loadReportData(filterType: ReportFilterType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val (startDate, endDate) = calculateDateRange(filterType)
                // Fetch transactions for the list view based on the filter
                orderRepository.getOrdersByDateRange(startDate, endDate).collect { orders ->
                    // Here you would also prepare/aggregate data for the chart based on 'orders'
                    // For now, just updating the transaction list and a placeholder chart data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            transactions = orders,
                            chartData = "Chart data for ${filterType.name}" // Placeholder
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error loading report data") }
            }
        }
    }

    private fun calculateDateRange(filterType: ReportFilterType): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis // End of today

        when (filterType) {
            ReportFilterType.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ReportFilterType.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0) // Start of the week
                // endDate will be end of today, which is fine for "This Week so far"
            }
            ReportFilterType.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0) // Start of the month
            }
            ReportFilterType.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0) // Start of the year
            }
        }
        val startDate = calendar.timeInMillis
        return Pair(startDate, endDate)
    }
}
