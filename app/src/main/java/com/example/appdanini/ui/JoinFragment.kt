package com.example.appdanini.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import com.example.appdanini.R
import com.example.appdanini.data.model.remote.TokenManager
import com.example.appdanini.databinding.FragmentJoinBinding
import com.example.appdanini.databinding.FragmentLoginBinding
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class JoinFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentJoinBinding? = null
    private val binding get() = _binding!! // null이면 안돼!
    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinBinding.inflate(inflater, container, false)
        binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.toolbarJoin // toolbar 바인딩
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

        binding.btJoginNext.setOnClickListener {
            val userName = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()

        }

    }
}