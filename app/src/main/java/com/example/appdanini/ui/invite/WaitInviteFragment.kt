package com.example.appdanini.ui.invite

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appdanini.util.TokenManager
import com.example.appdanini.databinding.FragmentWaitInviteBinding
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class WaitInviteFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentWaitInviteBinding? = null
    private val binding get() = _binding!!
    private var pollingJob: Job? = null

    @Inject

    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaitInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.toolbarInvite1
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        startPollingInviteStatus()
    }

    private fun startPollingInviteStatus() {
        pollingJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    val response = authViewModel.checkInviteStatus()
                    response?.let {
                        Log.d("InvitePolling", "응답: status=${it.status}, request_id=${it.request_id}, group_id=${it.group_id}")

                        when (it.status?.uppercase()) {
                            "APPROVED" -> {
                                tokenManager.saveRequestCode(it.request_id.toString())

                                // ✅ group_id null 체크 후 저장
                                if (it.group_id != null) {
                                    tokenManager.saveGroupId(it.group_id)
                                    Log.d("InvitePolling", "group_id 저장 완료: ${it.group_id}")
                                } else {
                                    Log.e("InvitePolling", "group_id가 null입니다. 저장하지 않음.")
                                }

                                navigateToSuccessPage()
                                return@launch
                            }

                            "REJECT" -> {
                                showRejectedDialog()
                                return@launch
                            }

                            "PENDING" -> {
                                // 대기 중 메시지나 상태 갱신 가능
                            }

                            else -> {
                                Log.w("InvitePolling", "Unknown status: ${it.status}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "서버 연결이 불안정합니다. 다시 시도 중...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                delay(5000)
            }
        }
    }


    private fun navigateToSuccessPage() {
        val action = WaitInviteFragmentDirections.actionWaitInviteFragmentToClosenessFragment()
        findNavController().navigate(action)
    }

    private fun showRejectedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("초대 거절")
            .setMessage("초대가 거절되었습니다.")
            .setPositiveButton("확인") { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pollingJob?.cancel()
        _binding = null
    }

}

