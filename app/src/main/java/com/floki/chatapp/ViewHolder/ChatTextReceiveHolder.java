package com.floki.chatapp.ViewHolder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.floki.chatapp.databinding.LayoutMessageTextFriendBinding;

public class ChatTextReceiveHolder extends RecyclerView.ViewHolder {
    public LayoutMessageTextFriendBinding binding;

    public ChatTextReceiveHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutMessageTextFriendBinding.bind(itemView);
    }
}
