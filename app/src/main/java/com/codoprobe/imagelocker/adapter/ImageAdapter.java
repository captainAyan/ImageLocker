package com.codoprobe.imagelocker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codoprobe.imagelocker.ImageActivity;
import com.codoprobe.imagelocker.R;
import com.codoprobe.imagelocker.utility.ChainRepository;


class ImageViewHolder extends RecyclerView.ViewHolder {

    ImageView image;
    TextView data;
    CardView holder;

    ImageViewHolder(@NonNull View itemView) {
        super(itemView);

        holder = (CardView) itemView.findViewById(R.id.holder);
        image = (ImageView) itemView.findViewById(R.id.image);
        data = (TextView) itemView.findViewById(R.id.data);
    }
}

public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private ChainRepository chainHolder;
    private Context ctx;

    public ImageAdapter(Context ctx, ChainRepository chainHolderSingleton) {
        this.chainHolder = chainHolderSingleton;
        this.ctx = ctx;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.image_item, viewGroup, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, final int i) {
        String dataString = "Bitmap Size : " + chainHolder
                .getChains().get(i).getBitmap().getByteCount();

        imageViewHolder.data.setText(dataString);
        imageViewHolder.image.setImageBitmap(chainHolder.getChains().get(i).getThumbnail());

        imageViewHolder.holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, ImageActivity.class);
                intent.putExtra("index", i);

                ctx.startActivity(intent);
            }
        });

        imageViewHolder.holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new AlertDialog.Builder(ctx)
                        .setTitle("Delete image")
                        .setMessage("Are you sure you want to delete this image?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                chainHolder.deleteChain(i);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return chainHolder.getChains().size();
    }
}
