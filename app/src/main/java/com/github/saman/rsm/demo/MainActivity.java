package com.github.saman.rsm.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.saman.rsm.android.RuntimeStateMigration;
import com.github.saman.rsm.android.interfaces.OnNewDeviceListener;
import com.github.saman.rsm.android.interfaces.OnRequestStateListener;
import com.github.saman.rsm.android.interfaces.OnStateChangeListener;
import com.github.saman.rsm.android.models.Config;
import com.github.saman.rsm.android.models.Device;
import com.github.saman.rsm.android.models.Server;
import com.github.saman.rsm.demo.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnNewDeviceListener, OnRequestStateListener, OnStateChangeListener {
    private static String TAG = "SamanSystem";
    private String sendingEmailModel = "sending-email";

    private RuntimeStateMigration rsm;

    ObjectMapper mapper = new ObjectMapper();

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Config config = new Config(new Server("tcp://130.185.123.111", 1883), "SamanSystem");
        RuntimeStateMigration.init(this, config);

        rsm = RuntimeStateMigration.getInstance();

        rsm.setOnNewDeviceListener(this);
        rsm.setOnRequestStateListener(this);
        rsm.setOnStateChangeListener(this);

        try {
            rsm.addModel("{\"asml\":\"1.0.0\",\"info\":{\"title\":\"sending-email\",\"description\":\"A schema model for run-time state migration of sending an email\",\"version\":\"1.0.0\",\"contact\":{\"name\":\"Saman Soltani\",\"email\":\"saman@mail.upb.de\",\"url\":\"samansoltani.com\"}},\"properties\":{\"from\":{\"description\":\"The sender email\",\"type\":\"string\",\"format\":\"email\"},\"to\":{\"description\":\"The reciever email\",\"type\":\"string\",\"format\":\"email\"},\"body\":{\"description\":\"The body text of the email\",\"type\":\"string\"}},\"required\":[\"from\",\"to\",\"body\"]}");

            rsm.introduce();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        binding.deviceId.setText(rsm.getDevice().getId());
        binding.getDevices.setOnClickListener(v -> {
            Log.d(TAG, "GetDevices");
            List<Device> devices = rsm.getDevices("sending-email");
            binding.devicesGroup.removeAllViews();
            for (Device d : devices) {
                RadioButton rb = new RadioButton(this);
                rb.setText(d.getName());
                rb.setTag(d.getId());
                binding.devicesGroup.addView(rb);
            }
        });
//        binding.devicesGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            RadioButton radio = findViewById(checkedId);
//            Toast.makeText(this, radio.getText().toString() + " : " + radio.getTag(), Toast.LENGTH_SHORT).show();
//        });

        binding.getData.setOnClickListener(v -> {
            int selectionId = binding.devicesGroup.getCheckedRadioButtonId();
            if (selectionId < 0) {
                Toast.makeText(this, "Please select a device first", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton rb = findViewById(selectionId);

            rsm.getStateDevice(sendingEmailModel, (String) rb.getTag());
        });
    }

    @Override public void onNewDevice(String modelName, Device device) {
        Log.d(TAG, "onNewDevice() called with: modelName = [" + modelName + "], device = [" + device + "]");
        Toast.makeText(this, String.format("%s joined %s", device.getName(), modelName), Toast.LENGTH_SHORT).show();
    }

    @Override public void onRequestState(String modelName, Device device) {
        Log.d(TAG, "onRequestState() called with: modelName = [" + modelName + "], device = [" + device + "]");
        SendingEmailState state = new SendingEmailState(
                binding.from.getText().toString(),
                binding.to.getText().toString(),
                binding.body.getText().toString()
        );
//        String jsonString = null;
//        try {
//            jsonString = mapper.writeValueAsString(state);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
        rsm.setState("sending-email", state.toString());
        rsm.sendState("sending-email", device.getId());
    }

    @Override public void onState(String modelName, Device device, String state, boolean isValid) {
        Log.d(TAG, "onState() called with: modelName = [" + modelName + "], device = [" + device + "], state = [" + state + "], isValid = [" + isValid + "]");
        if (modelName.equals(sendingEmailModel)) {
            SendingEmailState obj = SendingEmailState.fromJsonString(state);
            binding.from.setText(obj.getFrom());
            binding.to.setText(obj.getTo());
            binding.body.setText(obj.getBody());
        }
    }
}