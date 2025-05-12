package com.example.appdanini.ui.invite

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.appdanini.util.TokenManager
import com.example.appdanini.databinding.FragmentFamilyNameBinding
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FamilyNameFragment : Fragment() {

    private var _binding: FragmentFamilyNameBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.toolbarInviteCode1
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btNameChk.setOnClickListener {
            val familyName = binding.etFamilyName.text.toString().trim()

            if (familyName.isBlank()) {
                Toast.makeText(requireContext(), "가족 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (familyName.length > 8) {
                Toast.makeText(requireContext(), "가족 이름은 8글자 이내로 입력해야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 클릭 방지
            binding.btNameChk.isEnabled = false

            // 가족명을 저장
            tokenManager.saveFamilyname(familyName)
            Toast.makeText(requireContext(), "가족 이름 저장 완료: $familyName", Toast.LENGTH_SHORT).show()

            // 서버에 초대 코드 요청
            authViewModel.requestInviteCode(familyName)
        }

// 초대 코드 응답 수신 후 다음 화면 이동
        authViewModel.inviteCode.observe(viewLifecycleOwner) { code ->
            // 버튼 다시 활성화
            binding.btNameChk.isEnabled = true

            if (!code.isNullOrBlank()) {
                Toast.makeText(requireContext(), "초대 코드 생성 완료: $code", Toast.LENGTH_SHORT).show()
                tokenManager.saveInviteCode(code)
                val action = FamilyNameFragmentDirections.actionFamilyNameFragmentToInviteCodeFragment2()
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "초대 코드 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

