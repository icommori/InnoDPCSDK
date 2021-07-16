package com.innocomm.innodpcagent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.innocomm.innoservice.InnoManager;

import java.util.Calendar;

public class ProprietaryAPIFragment extends Fragment {

    private static final String TAG = ProprietaryAPIFragment.class.getSimpleName();
    public EditText editText,prop_key,prop_val;
    public TextView inputlog,bugreport_state;
    public Button btn_usb_none,btn_usb_mtp,btn_bugreport;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate ");
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy ");
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.v(TAG,"onCreateView ");
        return inflater.inflate(R.layout.proprietary_api, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.v(TAG,"onViewCreated ");
        MainActivity mMainActivity = (MainActivity) getActivity();
        editText = (EditText) view.findViewById(R.id.editText1);
        inputlog = (TextView) view.findViewById(R.id.inputlog);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                //Log.v(TAG, "onEditorAction actionId: " + actionId);
                if (event != null) Log.v(TAG, "onEditorAction getKeyCode: " + event.getKeyCode());
                if(v.getLineCount()>5) {
                    v.setText("");
                }

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    synchronized (mMainActivity.mThreads) {
                        String text = editText.getEditableText().toString();
                        if (mMainActivity.mSendStrQueue != null && mMainActivity.mSendStrQueue.size() > 0) {
                            String compareText = mMainActivity.mSendStrQueue.get(0);
                            if (compareText != null && !text.equals(compareText)) {
                                Log.v(TAG, "failCompare: " + text + "<>" + compareText);
                                mMainActivity.failCompare++;
                            }
                        }
                        v.setText("");
                        mMainActivity.lastUpdateCount++;
                        if ((Calendar.getInstance().getTimeInMillis() - mMainActivity.lastUpdateTime) >= (mMainActivity.FPS_PRINT_INTERVAL * 1000)) {
                            String logmsg = "FPS: " + (mMainActivity.lastUpdateCount / mMainActivity.FPS_PRINT_INTERVAL) + " fail:" + mMainActivity.failCompare + "/" + mMainActivity.count;
                            Log.v(TAG, logmsg);
                            inputlog.setText(logmsg);
                            mMainActivity.lastUpdateCount = 0;
                            mMainActivity.lastUpdateTime = Calendar.getInstance().getTimeInMillis();
                        }
                        mMainActivity.mSendStrQueue.clear();
                    }
                    handled = true;
                }
                return handled;
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (b) editText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                }, 200);
            }
        });
        editText.requestFocus();

        prop_key  = view.findViewById(R.id.prop_key);
        prop_val  = view.findViewById(R.id.prop_val);
        btn_usb_none = view.findViewById(R.id.btn_usb_none);
        btn_usb_mtp = view.findViewById(R.id.btn_usb_mtp);
        UpdateUSBState(true);

        bugreport_state= view.findViewById(R.id.bugreport_state);
        btn_bugreport= view.findViewById(R.id.btn_bugreport);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG,"onDestroyView ");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult " + requestCode);
    }

    public void UpdateUSBState(boolean immediate) {

            final int connectionType = Application.getInstance().mInnoManager.usb_getConnectionType();
            btn_usb_none.setEnabled(false);
            btn_usb_mtp.setEnabled(false);
            btn_usb_mtp.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn_usb_mtp.setEnabled(connectionType == InnoManager.USB_FUNCTION_MTP ? false : true);
                    btn_usb_none.setEnabled(connectionType == InnoManager.USB_FUNCTION_NONE ? false : true);
                }
            }, immediate ? 0 : 10000);

    }

}
