package com.musinsa.jth.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.musinsa.jth.domain.model.remote.ContentsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import com.musinsa.jth.domain.model.remote.DataItem
import com.musinsa.jth.domain.usecase.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getContentsUseCase: GetContentsUseCase,
    private val convertContentsListToMapUseCase: ConvertContentsListToMapUseCase,
    private val getFirstContentsItemListMapUseCase: GetFirstContentsItemListMapUseCase,
    private val getNextContentsItemListMapUseCase: GetNextContentsItemListMapUseCase,
    private val getRanDomContentsItemListMapUseCase: GetRanDomContentsItemListMapUseCase,
    private val getNextContentsItemListUseCase: GetNextContentsItemListUseCase,
    private val getRandomContentsItemListUseCase: GetRandomContentsItemListUseCase,
) : BaseViewModel() {

    private var _changeType = MutableLiveData<String>()
    val changeType: LiveData<String> = _changeType

    private var _originalContentsMapData = MutableLiveData<Map<String, DataItem>>()
    val originalContentsMapData: LiveData<Map<String, DataItem>> = _originalContentsMapData

    private var _currentContentsMapData = MutableLiveData<Map<String, List<ContentsItem>>>()
    val currentContentsMapData: LiveData<Map<String, List<ContentsItem>>> = _currentContentsMapData

    private var _currentContentsListData = MutableLiveData<List<List<ContentsItem>>>()
    val currentContentsListData: LiveData<List<List<ContentsItem>>> = _currentContentsListData

    fun getContents() {
        updateProgress(true)

        getContentsUseCase(scope = viewModelScope, { result ->
            _originalContentsMapData.value = convertContentsListToMapUseCase(result)
            _currentContentsMapData.value = getFirstContentsItemListMapUseCase(result)

            currentContentsMapData.value?.let {
                _currentContentsListData.value = getNextContentsItemListUseCase(it)
            }

            updateProgress(false)
        }, {
            updateToast(it)
            updateProgress(false)
        })
    }

    fun getNextContents(type: String) {
        updateProgress(true)

        originalContentsMapData.value?.let { originalMap ->
            currentContentsMapData.value?.let { currentMap ->
                val map = getNextContentsItemListMapUseCase(
                    type = type,
                    originalMap = originalMap,
                    currentMap = currentMap
                )

                _currentContentsMapData.value = LinkedHashMap<String, List<ContentsItem>>(map)
                _currentContentsListData.value = getNextContentsItemListUseCase(map)
                _changeType.value = type
            }
        }

        updateProgress(false)
    }

    fun getRandomContents(type: String) {
        updateProgress(true)

        currentContentsMapData.value?.let { currentMap ->
            val map= getRanDomContentsItemListMapUseCase(type, currentMap)
            _currentContentsListData.value = getNextContentsItemListUseCase(map)
            _currentContentsMapData.value = map
            _changeType.value = type
        }

        updateProgress(false)
    }
}