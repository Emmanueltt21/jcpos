package com.example.jetpackpos.data.repository

import com.example.jetpackpos.data.db.CustomerDao
import com.example.jetpackpos.data.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(private val customerDao: CustomerDao) {

    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()

    fun getCustomerById(customerId: Long): Flow<Customer?> = customerDao.getCustomerById(customerId)

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    suspend fun insertCustomer(customer: Customer): Long {
        // Basic validation or pre-processing can happen here if needed
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun isPhoneNumberExisting(phoneNumber: String, currentCustomerId: Long? = null): Boolean {
        if (phoneNumber.isBlank()) return false // Don't check empty phone numbers for existence
        val customer = customerDao.getCustomerByPhoneNumber(phoneNumber).firstOrNull()
        return customer != null && (currentCustomerId == null || customer.id != currentCustomerId)
    }

    suspend fun isEmailExisting(email: String, currentCustomerId: Long? = null): Boolean {
        if (email.isBlank()) return false // Don't check empty emails for existence
        val customer = customerDao.getCustomerByEmail(email).firstOrNull()
        return customer != null && (currentCustomerId == null || customer.id != currentCustomerId)
    }

    suspend fun clearAllCustomers() {
        customerDao.clearAllCustomers()
    }
}
