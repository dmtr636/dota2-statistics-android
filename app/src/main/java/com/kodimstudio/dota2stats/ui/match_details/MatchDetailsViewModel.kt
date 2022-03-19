package com.kodimstudio.dota2stats.ui.match_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kodimstudio.dota2stats.api.RequestStatus
import com.kodimstudio.dota2stats.api.Common
import com.kodimstudio.dota2stats.api.model.MatchDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchDetailsViewModel : ViewModel() {
    private val matchDetails = MutableLiveData<MatchDetails>()
    private val loadMatchDetailsStatus = MutableLiveData<Int>()

    var errorStatus = MutableLiveData<Int>()

    var matchId: Long = 0

    private val serviceOpenDota = Common.retrofitServiceOpenDota

    fun getMatchDetails() = matchDetails

    fun loadMatchDetails() {
        loadMatchDetailsStatus.value = RequestStatus.EXECUTING
        serviceOpenDota.getMatchDetails(matchId).enqueue(object: Callback<MatchDetails> {
            override fun onResponse(call: Call<MatchDetails>, response: Response<MatchDetails>) {
                if (!response.isSuccessful) {
                    errorStatus.value = -1
                    loadMatchDetailsStatus.value = RequestStatus.ERROR
                    return
                }
                val data = response.body() as MatchDetails
                matchDetails.value = data
                loadMatchDetailsStatus.value = RequestStatus.SUCCESS
            }

            override fun onFailure(call: Call<MatchDetails>, t: Throwable) {
                errorStatus.value = -1
                loadMatchDetailsStatus.value = RequestStatus.ERROR
            }
       })
    }
}