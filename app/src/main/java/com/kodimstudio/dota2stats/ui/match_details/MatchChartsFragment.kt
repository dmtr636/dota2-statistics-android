package com.kodimstudio.dota2stats.ui.match_details

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.tabs.TabLayout
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.Common
import com.kodimstudio.dota2stats.api.model.MakeRequestResponse
import com.kodimstudio.dota2stats.api.model.RequestStatusResponse
import com.kodimstudio.dota2stats.data.Data
import com.kodimstudio.dota2stats.databinding.FragmentMatchChartsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MatchChartsFragment : Fragment() {

    private lateinit var binding: FragmentMatchChartsBinding
    private val openDotaService = Common.retrofitServiceOpenDota
    private var makeRequestStatus = 0
    private val viewModel: MatchDetailsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchChartsBinding.inflate(inflater, container, false)

        viewModel.getMatchDetails().observe(viewLifecycleOwner) { matchDetails ->
            if (matchDetails.radiant_gold_adv != null) {
                binding.errorMessageLayout.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.INVISIBLE
                binding.chart.visibility = View.VISIBLE
                drawCharts()
            } else {
                binding.chart.visibility = View.INVISIBLE
                if (Data.EXECUTING_REQUESTS.containsKey(matchDetails.match_id)) {
                    binding.progressBar.visibility = View.VISIBLE
                    waitForRequestComplete(Data.EXECUTING_REQUESTS[matchDetails.match_id]!!)
                } else {
                    if (System.currentTimeMillis() / 1000 - matchDetails.start_time >= 60 * 60 * 24 * 30) {
                        binding.makeRequestButton.visibility = View.GONE
                        binding.makeRequestButtonLabel.visibility = View.GONE
                        binding.errorMessage.text = getString(R.string.charts_for_old_matches_error_message)
                    }

                    binding.errorMessageLayout.visibility = View.VISIBLE
                }
            }
        }

        binding.makeRequestButton.setOnClickListener {
            makeRequest(MatchDetailsActivity.matchInfo.match_id)
        }

        return binding.root
    }

    private fun makeRequest(matchId: Long) {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorMessageLayout.visibility = View.INVISIBLE
        openDotaService.makeRequest(matchId).enqueue(object: Callback<MakeRequestResponse> {
            override fun onResponse(
                call: Call<MakeRequestResponse>,
                response: Response<MakeRequestResponse>
            ) {
                if (!response.isSuccessful) {
                    return
                }
                val data = response.body() as MakeRequestResponse
                val jobId = data.job.jobId
                Data.EXECUTING_REQUESTS[matchId] = jobId

                waitForRequestComplete(jobId)
            }

            override fun onFailure(call: Call<MakeRequestResponse>, t: Throwable) {

            }
        })
    }

    private fun waitForRequestComplete(jobId: Long) {
        makeRequestStatus = 0
        checkRequestStatus(jobId)
        lifecycleScope.launch {
            while(makeRequestStatus == 0) {
                delay(50L)
            }
            Data.EXECUTING_REQUESTS.remove(viewModel.matchId)
            viewModel.loadMatchDetails()
        }
    }

    private fun checkRequestStatus(jobId: Long) {
        openDotaService.getRequestStatus(jobId).enqueue(object: Callback<RequestStatusResponse> {
            override fun onResponse(
                call: Call<RequestStatusResponse>,
                response: Response<RequestStatusResponse>
            ) {
                if (!response.isSuccessful) {
                    return
                }
                if (response.body() == null) {
                    makeRequestStatus = 1
                } else {
                    lifecycleScope.launch {
                        delay(2000L)
                        checkRequestStatus(jobId)
                    }
                }
            }

            override fun onFailure(call: Call<RequestStatusResponse>, t: Throwable) {

            }
        })
    }

    private fun drawCharts() {
        val chart: LineChart = binding.chart

        val formatterXAxis: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return "${value.toInt()}:00"
            }
        }
        val formatterYAxis: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return "${(value / 1000).toInt()}K"
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        chart.apply {
                            data = getDataSet(viewModel.getMatchDetails().value?.radiant_gold_adv!!)
                            invalidate()
                        }
                    }
                    1 -> {
                        chart.apply {
                            data = getDataSet(viewModel.getMatchDetails().value?.radiant_xp_adv!!)
                            invalidate()
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        chart.apply {
            data = when(binding.tabLayout.selectedTabPosition) {
                0 -> getDataSet(viewModel.getMatchDetails().value?.radiant_gold_adv!!)
                1 -> getDataSet(viewModel.getMatchDetails().value?.radiant_xp_adv!!)
                else -> getDataSet(viewModel.getMatchDetails().value?.radiant_gold_adv!!)
            }

            axisLeft.apply {
                textColor = Color.WHITE
                axisLineWidth = 2f
                axisLineColor = Color.WHITE
                gridLineWidth = 1f
                gridColor = Color.argb(35, 192, 189, 189)
                zeroLineColor = Color.WHITE
                zeroLineWidth = 2f
                setDrawZeroLine(true)
                textSize = 12f
                textColor = Color.parseColor("#c0bdbd")
                typeface = ResourcesCompat.getFont(requireContext(), R.font.arial_boldmt)
                granularity = 1f
                valueFormatter = formatterYAxis
            }

            axisRight.isEnabled = false

            xAxis.apply {
                textColor = Color.WHITE
                textSize = 12f
                axisLineWidth = 2f
                axisLineColor = Color.WHITE
                gridLineWidth = 1f
                gridColor = Color.argb(35, 192, 189, 189)
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#c0bdbd")
                typeface = ResourcesCompat.getFont(requireContext(), R.font.arial_boldmt)
                valueFormatter = formatterXAxis
                granularity = 1f
            }

            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(10f, 10f, 10f, 10f)
            invalidate()
        }
    }

    private fun getDataSet(data: List<Int>): LineData {
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        val yArray = data.map {
            it.toFloat()
        }
        var entries = ArrayList<Entry?>()
        var prevValueIsPositive = false
        var prevValueIsNegative = false
        val step = 1f
        for (i in yArray.indices) {
            val y = yArray[i]
            //positive y values
            if (y >= 0) {
                //we are changing to positive values so draw the current negative dataSets
                if (prevValueIsNegative) {
                    //calculate the common mid point between a positive and negative y
                    val midEntry = Entry(i.toFloat() - step / 2, 0f)
                    entries.add(midEntry)

                    //draw the current negative dataSet to Red color
                    dataSets.add(getLineDataSet(entries, R.color.chartFillRed, R.color.chartLineRed))

                    //and initialize a new DataSet starting from the above mid point Entry
                    entries = ArrayList()
                    entries.add(midEntry)
                    prevValueIsNegative = false
                }

                //we are already in a positive dataSet continue adding positive y values
                entries.add(Entry(i.toFloat(), y))
                prevValueIsPositive = true
                //not having any other positive-negative changes so add the remaining positive values in the final dataSet
                if (i == yArray.size - 1) {
                    dataSets.add(getLineDataSet(entries, R.color.chartFillGreen, R.color.chartLineGreen))
                }
            } else {
                //we are changing to negative values so draw the current positive dataSets
                if (prevValueIsPositive) {
                    //calculate the common mid point between a positive and negative y
                    val midEntry = Entry(i.toFloat() - step / 2, 0f)
                    entries.add(midEntry)

                    //draw the current positive dataSet to Green color
                    dataSets.add(getLineDataSet(entries, R.color.chartFillGreen, R.color.chartLineGreen))

                    //and initialize a new DataSet starting from the above mid point Entry
                    entries = ArrayList()
                    entries.add(midEntry)
                    prevValueIsPositive = false
                }

                //we are already in a negative dataSet continue adding negative y values
                entries.add(Entry(i.toFloat(), y))
                prevValueIsNegative = true
                //not having any other positive-negative changes so add the remaining negative values in the final dataSet
                if (i == yArray.size - 1) {
                    dataSets.add(getLineDataSet(entries, R.color.chartFillRed, R.color.chartLineRed))
                }
            }
        }
        return LineData(dataSets)
    }

    private fun getLineDataSet(entries: ArrayList<Entry?>, fillColor: Int, lineColor: Int): LineDataSet {
        val dataSet = LineDataSet(entries, "")
        dataSet.setDrawCircles(false)
        dataSet.valueTextSize = 0f
        dataSet.lineWidth = 4f
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.color = ContextCompat.getColor(requireContext(), lineColor)
        dataSet.setDrawFilled(true)
        dataSet.fillColor = ContextCompat.getColor(requireContext(), fillColor)
        dataSet.fillFormatter = IFillFormatter { _, _ -> 0F }
        return dataSet
    }
}