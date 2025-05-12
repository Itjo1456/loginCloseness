package com.example.appdanini.ui.invite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.appdanini.util.TokenManager
import com.example.appdanini.databinding.FragmentInviteBinding
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InviteFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentInviteBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var tokenManager: TokenManager

    private var currentInviteCode: String? = null // ✅ 추가

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btChk.setOnClickListener {
            val inviteCode = binding.etInviteCode.text.toString().trim()

            if (inviteCode.isBlank()) {
                Toast.makeText(requireContext(), "초대 코드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentInviteCode = inviteCode  // ✅ 저장
            authViewModel.transferInviteCode(inviteCode)
        }

        // ui에 라이브 데이터를 띄우고 싶으면 observe로 관찰해야함
        authViewModel.transferInviteCodeResult.observe(viewLifecycleOwner) { result ->
            if (result == true) {
                currentInviteCode?.let { code ->
                    // 초대 코드를 제출 했음을 체크
                    // loginfragment에서의 분기점
                    tokenManager.markInviteCodeSubmitted()
                    // 이때 저장한 초대 코드가 splshAcitivity의 분기점
                    tokenManager.saveInviteCode(code)
                }

                Toast.makeText(requireContext(), "초대 코드가 정상적으로 처리되었습니다.", Toast.LENGTH_SHORT).show()
                val action = InviteFragmentDirections.actionInviteFragmentToWaitInviteFragment()
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "초대 코드가 올바르지 않거나 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btInviteChk.setOnClickListener {
            val action = InviteFragmentDirections.actionInviteFragmentToFamilyNameFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
