package com.example.appdanini.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.appdanini.R
import com.example.appdanini.data.model.remote.TokenManager
import com.example.appdanini.databinding.FragmentLoginBinding
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    // 로그인 fragment에 authviewmodel 연결 시키기
    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!! // null이면 안돼!
    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.root
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.toolbarLogin // toolbar 바인딩
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        // fragment는 자체 액션바 설정 불가
        // fragment를 호스팅하고 있는 activiy를 갖고 와서, AppcompatActivity로 형변환하고
        // 그 activiy의 액션바를 우리가 가져온 toolbar로 교체하는 작업
        // 이러면 툴바가 액션바처럼 동작한다.

        // 뒤로가기 버튼 눌렀을 때
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 자동 로그아웃 감지 처리
        authViewModel.checkForceLogout()

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

        authViewModel.loginResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                Log.d("LoginFragment", "로그인 응답 수신: $response")
                Log.d("LoginFragment", "로그인 응답 전체: $response")
                Log.d("LoginFragment", "토큰: ${response.token}")
                Log.d("LoginFragment", "리프레시 토큰: ${response.refreshToken}")
                Log.d("LoginFragment", "유저 이름: ${response.userName}")
                Log.d("LoginFragment", "유저 ID: ${response.userId}")

                val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                findNavController().navigate(action)
            } else {
                Log.e("LoginFragment", "로그인 응답 실패 (response == null)")
                Toast.makeText(requireContext(), "로그인에 실패했습니다. 서버 응답을 확인하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            Log.d("LoginFragment", "loginResult 변화 감지: $isSuccess")
        }

        authViewModel.shouldForceLogout.observe(viewLifecycleOwner, Observer { shouldLogout ->
            if (shouldLogout) {
                Toast.makeText(requireContext(), "세션이 만료되어 로그아웃 되었습니다.", Toast.LENGTH_LONG).show()
                // 로그아웃된 상태이므로 로그인 화면 유지
            }
        })


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}






