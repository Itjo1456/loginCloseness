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

            authViewModel.transferInviteCode(inviteCode)
        }

        authViewModel.transferInviteCodeResult.observe(viewLifecycleOwner) { result ->
            if (result == true) {
                tokenManager.markInviteCodeSubmitted()
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



