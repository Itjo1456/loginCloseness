package com.example.appdanini.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appdanini.util.TokenManager
import com.example.appdanini.databinding.FragmentLoginBinding
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.toolbarLogin
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btLoginNext.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("LoginFragment", "로그인 요청: email=$email")
            authViewModel.login(email, password)
        }

        // ✅ ViewModel에서 loginResult로 수정했을 경우
        authViewModel.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                if (tokenManager.isInitialSetupDone()) {
                    // 초기 설정 완료 → 홈 이동
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    findNavController().navigate(action)
                } else {
                    if (!tokenManager.isInviteCodeSubmitted()) {
                        // 초대 코드 입력 안 했으면 InviteFragment로 이동
                        val action = LoginFragmentDirections.actionLoginFragmentToInviteFragment()
                        findNavController().navigate(action)
                    } else {
                        // 초대 코드 제출했으면 서버 상태 확인0
                        checkInviteStatusAndNavigate()
                    }
                }
            } else {
                Log.e("LoginFragment", "로그인 실패 또는 서버 응답 없음")
                Toast.makeText(requireContext(), "로그인 실패. 이메일과 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkInviteStatusAndNavigate() {
        lifecycleScope.launch {
            try {
                val response = authViewModel.checkInviteStatus()
                val status = response?.status

                when (status) {
                    "APPROVED" -> {
                        val action = LoginFragmentDirections.actionLoginFragmentToClosenessFragment()
                        findNavController().navigate(action)
                    }
                    "REJECTED" -> {
                        Toast.makeText(requireContext(), "초대가 거절되었습니다. 다시 가입해 주세요.", Toast.LENGTH_SHORT).show()
                        val action = LoginFragmentDirections.actionLoginFragmentToInviteFragment()
                        findNavController().navigate(action)
                    }
                    "PENDING", null -> {
                        val action = LoginFragmentDirections.actionLoginFragmentToWaitInviteFragment()
                        findNavController().navigate(action)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val action = LoginFragmentDirections.actionLoginFragmentToWaitInviteFragment()
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
