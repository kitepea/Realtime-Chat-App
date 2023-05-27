package com.floki.chatapp.ViewHolder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.floki.chatapp.databinding.LayoutPeopleBinding;

    public class UserViewHolder extends RecyclerView.ViewHolder {
        public LayoutPeopleBinding binding;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutPeopleBinding.bind(itemView);
        }
    }
