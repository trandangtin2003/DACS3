package com.example.dacs3.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    private val loginRepository = LoginRepository()

    private val _loginSuccess = MutableLiveData<Unit>()
    val loginSuccess: LiveData<Unit>
        get() = _loginSuccess

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String>
        get() = _loginError

    fun login(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginError.value = "Invalid Email format .... "
        } else if (password.isEmpty()) {
            _loginError.value = "Nhập Mật Khẩu .... "
        } else {
            loginRepository.loginUser(email, password,
                onSuccess = {
                    _loginSuccess.value = Unit
                },
                onFailure = { errorMessage ->
                    _loginError.value = "Login failed due to $errorMessage"
                }
            )
        }
    }
}