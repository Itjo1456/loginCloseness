package com.example.appdanini.ui.invite

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appdanini.R
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.appdanini.util.TokenManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class InvitePopupActivity : AppCompatActivity() {

    private var inviteRequestId: Int = 0
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_popup)

        val inviteRequestIdStr = intent.getStringExtra("invite_request_id")
        val senderName = intent.getStringExtra("sender_name") ?: "상대방"

        val inviteRequestId = inviteRequestIdStr?.toIntOrNull() ?: run {
            finish()
            return
        }

        val tvMessage = findViewById<TextView>(R.id.tvInviteMessage)
        val btnAccept = findViewById<Button>(R.id.btnAccept)
        val btnReject = findViewById<Button>(R.id.btnReject)

        tvMessage.text = "$senderName 님의 초대를 수락하시겠습니까?"

        btnAccept.setOnClickListener {
            handleInviteResponse(true, inviteRequestId)
        }

        btnReject.setOnClickListener {
            handleInviteResponse(false, inviteRequestId)
        }
    }

    private fun handleInviteResponse(isAccepted: Boolean, inviteRequestId: Int) {
        lifecycleScope.launch {
            val success = authViewModel.acceptGroup(inviteRequestId.toString(), isAccepted)
            if (!success) {
                Toast.makeText(this@InvitePopupActivity, "요청 처리에 실패했습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
            finish()
        }
    }
}

