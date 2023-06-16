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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import me.ecogaming.heartratemonitor.R
import me.ecogaming.heartratemonitor.databinding.FragmentHomeBinding


class HomeFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var measure = false

    private val heartRateRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                measureHeartRate()
            } else {
                showBodySensorRationale(requireContext())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_main, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_settings -> {
                            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                            true
                        }
                        R.id.action_about -> {
                            findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner, Lifecycle.State.RESUMED
        )

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
            binding.textHeartRate.text = getString(R.string.text_heart_rate, heartRate.toInt().toString())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_HEART_RATE) {
            binding.textHeartRateAccuracy.text = getString(R.string.text_heart_rate_accuracy, accuracy.toString())
        }
    }

    override fun onPause() {
        super.onPause()
        stopMeasuringHeartRate()
    }

    private fun measureHeartRate() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
            binding.textHeartRate.text = getString(R.string.text_heart_rate, "0")
            binding.textHeartRate.textSize = 32F
            binding.textHeartRateAccuracy.text = getString(R.string.text_heart_rate_accuracy, "0")
            binding.buttonMeasureHeartRate.text = getString(R.string.button_measure_heart_rate_stop)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BODY_SENSORS)) {
                showBodySensorRationale(requireContext())
            } else {
                heartRateRequestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    private fun stopMeasuringHeartRate() {
        sensorManager.unregisterListener(this)
        binding.textHeartRate.text = getString(R.string.text_heart_rate_idle)
        binding.textHeartRate.textSize = 24F
        binding.textHeartRateAccuracy.text = ""
        binding.buttonMeasureHeartRate.text = getString(R.string.button_measure_heart_rate)
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