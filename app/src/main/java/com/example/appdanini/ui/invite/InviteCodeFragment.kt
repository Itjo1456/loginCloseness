package com.example.appdanini.ui.invite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.appdanini.util.TokenManager
import com.example.appdanini.databinding.FragmentInviteCodeBinding
import com.example.appdanini.viewmodel.AuthViewModel
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InviteCodeFragment : Fragment() {

    private var _binding: FragmentInviteCodeBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    @Inject
    lateinit var tokenManager: TokenManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: MaterialToolbar = binding.toolbarInviteCode1
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 🔥 초대코드 표시
        val inviteCode = tokenManager.getInviteCode()
        binding.etInviteLink.setText(inviteCode)
        binding.etInviteLink.isFocusable = false
        binding.etInviteLink.isClickable = false

        // 🔥 클립보드 복사 기능
        binding.btCopy.setOnClickListener {
            val clipboard =
                requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("초대코드", inviteCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "초대코드가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        binding.btNext.setOnClickListener {
            val action = InviteCodeFragmentDirections.actionInviteCodeFragment2ToClosenessFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
