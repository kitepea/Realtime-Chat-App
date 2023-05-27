package com.floki.chatapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.floki.chatapp.ChatActivity;
import com.floki.chatapp.Common.Common;
import com.floki.chatapp.CustomLinearLayoutManager;
import com.floki.chatapp.Model.UserModel;
import com.floki.chatapp.R;
import com.floki.chatapp.ViewHolder.UserViewHolder;
import com.floki.chatapp.databinding.FragmentChatBinding;
import com.floki.chatapp.databinding.FragmentPeopleBinding;
import com.floki.chatapp.databinding.LayoutPeopleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ConcurrentModificationException;
import java.util.Objects;

public class PeopleFragment extends Fragment {
    FirebaseRecyclerAdapter adapter;
    public FragmentPeopleBinding binding;
    private PeopleViewModel mViewModel;
    static PeopleFragment instance;
    public static PeopleFragment getInstance(){
        return instance == null ? new PeopleFragment() : instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_people, container, false);
        initView(itemView);
        loadPeople(itemView);
        return itemView;
    }

    private void loadPeople(View itemView) {
        RecyclerView recycle_people = itemView.findViewById(R.id.recycler_people);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(Common.USER_REFERENCES);
        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions
                .Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_people, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel model) {
                if (!Objects.equals(adapter.getRef(position).getKey(), FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    //Hide yourself
                    ColorGenerator generator = ColorGenerator.MATERIAL;
                    int color = generator.getColor(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    TextDrawable.IBuilder builder = TextDrawable.builder()
                            .beginConfig()
                            .withBorder(4)
                            .endConfig()
                            .round();
                    TextDrawable drawable = builder.build(model.getFirstName().substring(0, 1), color);
                    holder.binding.imgAvatar.setImageDrawable(drawable);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(model.getFirstName()).append(" ").append(model.getLastName());
                    holder.binding.txtName.setText(stringBuilder.toString());
                    holder.binding.txtBio.setText(model.getBio());

                    //Event
                    holder.itemView.setOnClickListener(v -> {
                        Common.chatUser = model;
                        Common.chatUser.setUid(adapter.getRef(position).getKey());
                        startActivity(new Intent(getContext(), ChatActivity.class));
                    });
                }
                else
                {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };
        adapter.startListening();
        recycle_people.setAdapter(adapter);
    }

    private void initView(View itemView) {
        RecyclerView recycle_people = itemView.findViewById(R.id.recycler_people);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        recycle_people.setLayoutManager(layoutManager);
        LinearLayoutManager layoutManager = new CustomLinearLayoutManager(getActivity());
        recycle_people.setLayoutManager(layoutManager);
        recycle_people.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PeopleViewModel.class);
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