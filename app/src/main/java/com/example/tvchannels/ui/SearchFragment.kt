package com.example.tvchannels.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseFragment
import androidx.leanback.widget.*
import com.example.tvchannels.R
import com.example.tvchannels.data.Channel

class SearchFragment : Fragment() {

    private var onSearchListener: ((String) -> Unit)? = null
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var browseFragment: BrowseFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupBrowseFragment()
    }

    private fun setupViews(view: View) {
        searchEditText = view.findViewById(R.id.search_edit_text)
        searchButton = view.findViewById(R.id.search_button)
        backButton = view.findViewById(R.id.back_button)
        
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotBlank()) {
                onSearchListener?.invoke(query)
            }
        }
        
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        // Обработка нажатия Enter
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString()
                if (query.isNotBlank()) {
                    onSearchListener?.invoke(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupBrowseFragment() {
        browseFragment = childFragmentManager.findFragmentById(R.id.search_browse_fragment) as BrowseFragment
        
        browseFragment.headersState = BrowseFragment.HEADERS_DISABLED
        browseFragment.title = "Поиск каналов"
        
        // Показываем пустой список до начала поиска
        val emptyAdapter = ArrayObjectAdapter(ChannelPresenter())
        val emptyRow = ListRow(HeaderItem("Введите запрос для поиска"), emptyAdapter)
        browseFragment.adapter = ArrayObjectAdapter(ListRowPresenter()).apply {
            add(emptyRow)
        }
    }

    fun setOnSearchListener(listener: (String) -> Unit) {
        onSearchListener = listener
    }

    fun showSearchResults(channels: List<Channel>, query: String) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        if (channels.isNotEmpty()) {
            val searchAdapter = ArrayObjectAdapter(ChannelPresenter())
            channels.forEach { channel ->
                searchAdapter.add(channel)
            }
            rowsAdapter.add(ListRow(HeaderItem("Результаты поиска: $query"), searchAdapter))
        } else {
            val emptyAdapter = ArrayObjectAdapter(ChannelPresenter())
            rowsAdapter.add(ListRow(HeaderItem("Ничего не найдено для: $query"), emptyAdapter))
        }
        
        browseFragment.adapter = rowsAdapter
    }
} 