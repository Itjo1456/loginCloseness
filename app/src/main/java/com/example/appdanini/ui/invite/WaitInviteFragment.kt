package com.example.appdanini.ui.invite

import android.os.Bundle
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
                    if (response != null) {
                        when (response.status) {
                            "accepted" -> {
                                response.group_Id?.let {
                                    tokenManager.saveGroupId(it) // 수정된 부분
                                }
                                navigateToSuccessPage()
                                break

                            }
                            "rejected" -> {
                                showRejectedDialog()
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "서버 연결이 불안정합니다. 다시 시도 중...", Toast.LENGTH_SHORT).show()
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


