package me.ecogaming.heartratemonitor.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import me.ecogaming.heartratemonitor.R
import me.ecogaming.heartratemonitor.database.HeartRateHistory
import me.ecogaming.heartratemonitor.database.HeartRateHistoryEntry
import me.ecogaming.heartratemonitor.databinding.FragmentHeartRateBinding
import java.util.Date


class HeartRateFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHeartRateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var measure = false
    private val values = ArrayList<Int>()
    private var average = 0
    private lateinit var date: Date

    private lateinit var heartRateHistory: HeartRateHistory

    private val heartRateRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                measureHeartRate()
            } else {
                showBodySensorRationale(requireContext())
                measure = false
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        heartRateHistory = HeartRateHistory(requireContext())

        _binding = FragmentHeartRateBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonMeasureHeartRate.setOnClickListener {
            measure = !measure
            if (measure) {
                measureHeartRate()
            } else {
                stopMeasuringHeartRate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0]
            val heartRateInt = heartRate.toInt()
            binding.textHeartRate.text = getString(R.string.text_heart_rate, heartRateInt.toString())
            if (heartRate > 0) {
                if (average == 0) {
                    date = Date()
                }
                values.add(heartRateInt)
                val sum = values.sum()
                average = sum / values.size
            }
            binding.textHeartRateAverage.text = getString(R.string.text_heart_rate_average, average.toString())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onPause() {
        super.onPause()
        if (measure) {
            stopMeasuringHeartRate()
            measure = false
        }
    }

    private fun measureHeartRate() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            values.clear()
            average = 0
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
            binding.textHeartRate.text = getString(R.string.text_heart_rate, "0")
            binding.textHeartRateAverage.text = getString(R.string.text_heart_rate_average, "0")
            binding.buttonMeasureHeartRate.text = getString(R.string.button_measure_heart_rate_stop)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BODY_SENSORS)) {
                showBodySensorRationale(requireContext())
                measure = false
            } else {
                heartRateRequestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    private fun stopMeasuringHeartRate() {
        sensorManager.unregisterListener(this)
        binding.textHeartRate.text = getString(R.string.text_heart_rate_idle)
        binding.textHeartRateAverage.text = ""
        binding.buttonMeasureHeartRate.text = getString(R.string.button_measure_heart_rate)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (sharedPreferences.getBoolean("pref_save_history", true) && average > 0) {
            val entry = HeartRateHistoryEntry(date, average)
            heartRateHistory.pushValue(entry)
        }
    }

    private fun showBodySensorRationale(context: Context) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.body_sensors_permission_dialog_title)
            .setMessage(R.string.body_sensors_permission_dialog_message)
            .setPositiveButton(R.string.body_sensors_permission_dialog_go_to_settings) { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri =
                    Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}