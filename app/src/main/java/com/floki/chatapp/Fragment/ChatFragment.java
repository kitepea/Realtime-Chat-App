package com.floki.chatapp.Fragment;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.floki.chatapp.Common.Common;
import com.floki.chatapp.CustomLinearLayoutManager;
import com.floki.chatapp.Model.ChatInfoModel;
import com.floki.chatapp.R;
import com.floki.chatapp.ViewHolder.ChatInfoHolder;
import com.floki.chatapp.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class ChatFragment extends Fragment {
    public FragmentChatBinding binding;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    FirebaseRecyclerAdapter adapter;
    private ChatViewModel mViewModel;

    static ChatFragment instance;
    public static ChatFragment getInstance(){
        return instance == null ? new ChatFragment() : instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_chat, container, false);
        initView(itemView);
        loadChatList(itemView);
        return itemView;
    }

    private void loadChatList(View itemView) {
        RecyclerView recycle_chat_fragment = itemView.findViewById(R.id.recycler_chat_fragment);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseRecyclerOptions<ChatInfoModel> options = new FirebaseRecyclerOptions
                .Builder<ChatInfoModel>()
                .setQuery(query, ChatInfoModel.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<ChatInfoModel, ChatInfoHolder>(options) {

            @NonNull
            @Override
            public ChatInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_chat_item, parent, false);
                return new ChatInfoHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatInfoHolder holder, int position, @NonNull ChatInfoModel model) {
                if (!Objects.equals(adapter.getRef(position)
                        .getKey(), FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    ColorGenerator generator = ColorGenerator.MATERIAL;
                    int color = generator.getColor(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                            .withBorder(4)
                            .endConfig()
                            .round();
                    String displayName = FirebaseAuth.getInstance().getCurrentUser().getUid()
                            .equals(model.getCreateId()) ? model.getFriendName() : model.getCreateName();

                    TextDrawable drawable = builder.build(displayName.substring(0, 1), color);
                    holder.binding.imgAvatar.setImageDrawable(drawable);
                    holder.binding.txtName.setText(displayName);
                    holder.binding.txtLastMessage.setText(model.getLastMessage());
                    holder.binding.txtTime.setText(simpleDateFormat.format(model.getLastUpdate()));

                    holder.itemView.setOnClickListener(v -> {
                        //Implement later
                    });
                }
                else {
                    //If equal key - hide yourself
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };

        adapter.startListening();
        recycle_chat_fragment.setAdapter(adapter);

    }

    private void initView(View itemView) {
        RecyclerView recycler_chat = itemView.findViewById(R.id.recycler_chat_fragment);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        recycler_chat.setLayoutManager(layoutManager);
        LinearLayoutManager layoutManager = new CustomLinearLayoutManager(getActivity());
        recycler_chat.setLayoutManager(layoutManager);
        recycler_chat.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        if (adapter != null) adapter.stopListening();
        super.onStop();
    }
}