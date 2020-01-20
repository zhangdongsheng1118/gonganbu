package com.yixin.tinode.ui.view;


import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lqr.recyclerview.LQRRecyclerView;

import co.tinode.tinodesdk.model.Drafty;

public interface IRelayAtView {

    Button getBtnToolbarSend();

    LQRRecyclerView getRvContacts();

    LQRRecyclerView getRvSelectedContacts();

    EditText getEtKey();

    View getHeaderView();

    Drafty getDrafty();
}
