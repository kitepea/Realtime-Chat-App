package com.floki.chatapp.ViewHolder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.floki.chatapp.databinding.LayoutChatItemBinding;

public class ChatInfoHolder extends RecyclerView.ViewHolder {

    public LayoutChatItemBinding binding;

    public ChatInfoHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutChatItemBinding.bind(itemView);
    }
}
