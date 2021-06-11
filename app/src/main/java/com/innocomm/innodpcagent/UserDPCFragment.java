package com.innocomm.innodpcagent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class UserDPCFragment extends Fragment {

    private static final String TAG = UserDPCFragment.class.getSimpleName();
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
        return inflater.inflate(R.layout.dpc_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.v(TAG,"onViewCreated ");
        ListView listview = view.findViewById(R.id.lsv_restrictions);
        RestrictionAdapter adapter = new RestrictionAdapter(getActivity());
        listview.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult " + requestCode);
    }
}
