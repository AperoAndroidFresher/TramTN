package com.example.musicapp.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.musicapp.R
import com.example.musicapp.base.BaseActivity
import com.example.musicapp.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val languages = listOf("English", "Korean", "French", "Vietnamese")
    private val languageCodes = listOf("en", "ko", "fr", "vi")

    private var currentLanguageCode: String = "en"
    private var selectedLanguageCode: String = "en"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentLanguageCode = getSavedLanguage()
        selectedLanguageCode = currentLanguageCode

        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, languages)
        adapter.setDropDownViewResource(R.layout.item_spinner)
        binding.spinnerLanguage.adapter = adapter

        val currentIndex = languageCodes.indexOf(currentLanguageCode)
        if (currentIndex != -1) binding.spinnerLanguage.setSelection(currentIndex)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLanguageCode = languageCodes[position]
                binding.icTick.visibility = if (selectedLanguageCode != currentLanguageCode) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.icTick.setOnClickListener {
            val newLang = selectedLanguageCode
            (requireActivity() as BaseActivity).setLocale(newLang)
            Toast.makeText(requireContext(), "Language changed to $newLang", Toast.LENGTH_SHORT).show()

            parentFragmentManager.popBackStack()
        }

        binding.icBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun getSavedLanguage(): String {
        val prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return prefs.getString("App_Lang", "en") ?: "en"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
