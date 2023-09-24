package com.getmati.plugins.capacitor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.metamap.metamap_sdk.Metadata;
import com.metamap.metamap_sdk.MetamapSdk;
import com.metamap.metamap_sdk.metadata.FontConfig;
import com.metamap.metamap_sdk.metadata.MetamapLanguage;
import com.metamap.metamap_sdk.metadata.UIConfig;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@CapacitorPlugin(name = "MetaMapCapacitor")
public class MetaMapCapacitorPlugin extends Plugin {

    private List<String> configValues = Arrays.asList("identityId", "fixedLanguage", "buttonColor", "buttonTextColor", "fontConfig");

    @SuppressWarnings("unused")
    @PluginMethod
    public void showMetaMapFlow(PluginCall call) {
        Log.i("MetaMapCapacitorPlugin", "showMetaMapFlow");
        bridge.getActivity().runOnUiThread(() -> {
            final String clientId = call.getString("clientId");
            if (clientId == null) {
                Log.e("MetaMapCapacitorPlugin", "clientId should be not null");
                return;
            }

            final String flowId = call.getString("flowId");
            if (flowId == null) {
                Log.e("MetaMapCapacitorPlugin", "flowId should be not null");
                return;
            }

            JSONObject metadata = call.getObject("metadata", new JSObject());
            if (metadata == null) {
                Log.e("MetaMapCapacitorPlugin", "metadata should be not null");
                return;
            }

            try {
                Iterator<String> keys = metadata.keys();
                Metadata.Builder metadataBuilder = new Metadata.Builder();

                if (metadata.has("identityId")) {
                    metadataBuilder.identityId(metadata.getString("identityId"));
                }

                Integer buttonColor = null;
                if (metadata.has("buttonColor")) {
                    buttonColor = Color.parseColor(metadata.getString("buttonColor"));
                }

                Integer buttonTextColor = null;
                if (metadata.has("buttonTextColor")) {
                    buttonTextColor = Color.parseColor(metadata.getString("buttonTextColor"));
                }

                FontConfig fontConfig = new FontConfig("proxima_nova_regular.ttf", "proxima_nova_bold.ttf");

                UIConfig uiConfig = new UIConfig(MetamapLanguage.SPANISH, buttonColor, buttonTextColor, fontConfig);
                metadataBuilder.uiConfig(uiConfig);

                metadataBuilder.additionalData("sdkType", "capacitor");

                while (keys.hasNext()) {
                    String key = keys.next();
                    if (!configValues.contains(key)) {
                        metadataBuilder.additionalData(key, metadata.get(key));
                    }
                }

                Metadata data = metadataBuilder.build();
                Log.i("MetaMapCapacitorPlugin", "metadata: " + data.getDataJson());

                Intent flowIntent = MetamapSdk.INSTANCE.createFlowIntent(bridge.getActivity(), clientId, flowId, data, null, null);
                startActivityForResult(call, flowIntent, "callback");
            } catch (Exception exception) {
                Log.e("MetaMapCapacitorPlugin", "Verification error" + exception.getMessage());
                call.reject("Verification failed");
            }
        });
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        Log.w("MetaMapCapacitorPlugin", "WILL NOT BE CALLED");
    }

    @SuppressWarnings("unused")
    @ActivityCallback
    public void callback(PluginCall call, ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK && activityResult.getData() != null) {
            JSObject result = new JSObject();
            String identityId = activityResult.getData().getStringExtra(MetamapSdk.ARG_IDENTITY_ID);
            String verificationID = activityResult.getData().getStringExtra(MetamapSdk.ARG_VERIFICATION_ID);
            result.put("identityId", identityId);
            result.put("verificationID", verificationID);
            call.resolve(result);
            Log.i("MetaMapCapacitorPlugin", "Activity.RESULT_OK");
        } else {
            call.reject("verificationCancelled");
            Log.e("MetaMapCapacitorPlugin", "Activity.RESULT_CANCELLED");
        }
    }
}
