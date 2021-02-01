package com.example.realmexample.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realmexample.R;
import com.example.realmexample.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private List<UserModel> list = new ArrayList<>();
    private final OnClickUserItemListener listener;

    public UserAdapter(OnClickUserItemListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_layout, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        holder.onBind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView userImage;
        private final TextView nameTv;
        private final TextView phoneTv;

        private OnClickUserItemListener listener;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            nameTv = itemView.findViewById(R.id.nameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);

            itemView.setOnClickListener(this);
        }

        private void onBind(UserModel model, OnClickUserItemListener listener) {
            this.listener = listener;

            nameTv.setText(model.getName());
            phoneTv.setText(model.getPhone());

            byte[] img = model.getImage();

            if (img != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                userImage.setImageBitmap(bitmap);
            }
            else {
                userImage.setImageResource(R.drawable.ic_baseline_account_circle_24);
            }
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onClickItem(getAdapterPosition());
            }
        }
    }
}
