package com.floki.chatapp;

import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.floki.chatapp.Common.Common;
import com.floki.chatapp.Listener.IFirebaseLoadFailed;
import com.floki.chatapp.Listener.ILoadTimeFromDatabaseListener;
import com.floki.chatapp.Model.ChatInfoModel;
import com.floki.chatapp.Model.ChatMessageModel;
import com.floki.chatapp.ViewHolder.ChatTextHolder;
import com.floki.chatapp.ViewHolder.ChatTextReceiveHolder;
import com.floki.chatapp.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements ILoadTimeFromDatabaseListener, IFirebaseLoadFailed {

    private final static int MY_CAMERA_REQUEST_CODE = 1337;
    private final static int MY_RESULT_LOAD_IMAGE = 1490;
    private ActivityChatBinding binding;
    FirebaseDatabase database;
    DatabaseReference chatRef, offsetRef;
    //offsetRef để lưu trữ vị trí cuối cùng của dữ liệu đã tải về.
    ILoadTimeFromDatabaseListener listener;
    IFirebaseLoadFailed errorListener;
    FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder> adapter;
    FirebaseRecyclerOptions<ChatMessageModel> options;
    Uri fileUri;
    LinearLayoutManager layoutManager;

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        if (adapter != null) adapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.startListening();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageView imgSend = findViewById(R.id.img_send);
        imgSend.setOnClickListener(v -> {
            onSubmitChatClick();
        });

        initView();
        loadChatContent();
    }

    void onSubmitChatClick() { // offsetRef
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeInMs = System.currentTimeMillis() + offset;
                listener.onLoadOnlyTimeSuccess(estimatedServerTimeInMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorListener.onError(error.getMessage());
            }
        });
    }

    private void loadChatContent() {
        String receiveId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder>(options) {
            @Override
            public int getItemViewType(int position) {
                if (adapter.getItem(position).getSenderId()
                        .equals(receiveId)) //If message is own
                    return !adapter.getItem(position).isPicture() ? 0 : 1;
                else return !adapter.getItem(position).isPicture() ? 2 : 3;
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatMessageModel model) {
                if (holder instanceof ChatTextHolder) {
                    ChatTextHolder chatTextHolder = (ChatTextHolder) holder;
                    chatTextHolder.binding.txtChatMessage.setText(model.getContent());
                    chatTextHolder.binding.txtTime.setText(DateUtils.getRelativeTimeSpanString(
                            model.getTimeStamp(),
                            Calendar.getInstance().getTimeInMillis(),
                            0).toString());
                }
                else if (holder instanceof ChatTextReceiveHolder)
                {
                    ChatTextReceiveHolder chatTextHolder = (ChatTextReceiveHolder) holder;
                    chatTextHolder.binding.txtChatMessage.setText(model.getContent());
                    chatTextHolder.binding.txtTime.setText(DateUtils.getRelativeTimeSpanString(
                            model.getTimeStamp(),
                            Calendar.getInstance().getTimeInMillis(),
                            0).toString());
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if (viewType == 0) //Text message of user own message
                {
                    view= LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_message_text_own, parent, false);
                    return new ChatTextReceiveHolder(view);
                }
                else //if (viewType == 2) //Text message of a friend
                {
                    view= LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_message_text_friend, parent, false);
                    return new ChatTextHolder(view);
                }
            }
        };

        //Autoscroll when receive new message
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    binding.recyclerChat.scrollToPosition(positionStart);
                }
            }
        });
        binding.recyclerChat.setAdapter(adapter);
    }

    private void initView() {
        listener = this;
        errorListener = this;
        database = FirebaseDatabase.getInstance();
        chatRef = database.getReference(Common.CHAT_REFERENCE);

        offsetRef = database.getReference(".info/serverTimeOffset");

        Query query = chatRef.child(Common.generateChatRoomId(
                Common.chatUser.getUid(),
                FirebaseAuth.getInstance().getCurrentUser().getUid()
        )).child(Common.CHAT_DETAIL_REFERENCES);

        options = new FirebaseRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class)
                .build();
        layoutManager = new LinearLayoutManager(this);
        binding.recyclerChat.setLayoutManager(layoutManager);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(Common.chatUser.getUid());
        TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                .withBorder(4)
                .endConfig()
                .round();

        TextDrawable drawable = builder.build(Common.chatUser.getFirstName().substring(0, 1), color);
        binding.imgAvatar.setImageDrawable(drawable);
        binding.txtName.setText(Common.getName(Common.chatUser));

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {
        ChatMessageModel chatMessageModel = new ChatMessageModel();
        chatMessageModel.setName(Common.getName(Common.currentUser));
        chatMessageModel.setContent(binding.edtChat.getText().toString());
        chatMessageModel.setTimeStamp(estimateTimeInMs);
        chatMessageModel.setSenderId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //Set chat
        chatMessageModel.setPicture(false);
        submitChatToFirebase(chatMessageModel, chatMessageModel.isPicture(), estimateTimeInMs);
    }

    private void submitChatToFirebase(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        //18:35
        chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                        FirebaseAuth.getInstance().getCurrentUser().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            appendChat(chatMessageModel, isPicture, estimateTimeInMs);
                        } else
                            createChat(chatMessageModel, isPicture, estimateTimeInMs);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void appendChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        Map<String, Object> update_data = new HashMap<>();
        update_data.put("lastUpdate", estimateTimeInMs);

        // Only text
        update_data.put("lastMessage", chatMessageModel.getContent());

        // Update
        // Update on User List
        FirebaseDatabase.getInstance()
                .getReference(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .updateChildren(update_data)
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(unused -> {
                    //Submit success to chatinfo
                    //Copy to friend chat list
                    FirebaseDatabase.getInstance()
                            .getReference(Common.CHAT_LIST_REFERENCE)
                            .child(Common.chatUser.getUid()) //Swap
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) // Swap
                            .updateChildren(update_data)
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(unused1 -> {

                                //Add on Chat ref
                                chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                                                FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                        .child(Common.CHAT_DETAIL_REFERENCES)
                                        .push()
                                        .setValue(chatMessageModel)
                                        .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Clear input field
                                                binding.edtChat.setText("");
                                                binding.edtChat.requestFocus();

                                                // Notify the adapter that the dataset has changed
                                                if (adapter != null) {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                        });
                            });
                });

    }

    private void createChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        ChatInfoModel chatInfoModel = new ChatInfoModel();
        chatInfoModel.setCreateId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        chatInfoModel.setFriendName(Common.getName(Common.chatUser));
        chatInfoModel.setFriendId(Common.chatUser.getUid());
        chatInfoModel.setCreateName(Common.getName(Common.currentUser));

        //Only text
        chatInfoModel.setLastMessage(chatMessageModel.getContent());

        chatInfoModel.setLastUpdate(estimateTimeInMs);
        chatInfoModel.setCreateDate(estimateTimeInMs);

        //Submit on Firebase
        //Add on user chat list
        FirebaseDatabase.getInstance()
                .getReference(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .setValue(chatInfoModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(unused -> {
                    //Submit success to chatinfo
                    //Copy to friend chat list
                    FirebaseDatabase.getInstance()
                            .getReference(Common.CHAT_LIST_REFERENCE)
                            .child(Common.chatUser.getUid()) //Swap
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) // Swap
                            .setValue(chatInfoModel)
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(unused1 -> {

                                //Add on Chat ref
                                chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                                                FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                        .child(Common.CHAT_DETAIL_REFERENCES)
                                        .push()
                                        .setValue(chatMessageModel)
                                        .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Clear input field
                                                binding.edtChat.setText("");
                                                binding.edtChat.requestFocus();

                                                // Notify the adapter that the dataset has changed
                                                if (adapter != null) {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                        });
                            });
                });
    }

    @Override
    public void onError(String message) {
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}