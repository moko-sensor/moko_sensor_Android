package com.moko.sensor.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.sensor.R;
import com.moko.sensor.base.BaseAdapter;
import com.moko.sensor.entity.MokoDevice;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @Date 2018/6/8
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.adapter.DeviceAdapter
 */
public class DeviceAdapter extends BaseAdapter<MokoDevice> {

    public DeviceAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindViewHolder(int position, ViewHolder viewHolder, View convertView, ViewGroup parent) {
        final DeviceViewHolder holder = (DeviceViewHolder) viewHolder;
        final MokoDevice device = getItem(position);
        setView(holder, device);
    }

    private void setView(DeviceViewHolder holder, final MokoDevice device) {
        if (!device.isOnline) {
            holder.ivDevice.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.sensor_offline));
            holder.ivArrow.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.arrow_right));
            holder.tvDeviceName.setTextColor(ContextCompat.getColor(mContext, R.color.grey_bfbfbf));
            holder.tvDeviceState.setText(mContext.getString(R.string.device_state_offline));
            holder.tvDeviceState.setTextColor(ContextCompat.getColor(mContext, R.color.grey_bfbfbf));
        } else {
            holder.ivDevice.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.sensor_online));
            holder.ivArrow.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.arrow_right_blue));
            holder.tvDeviceName.setTextColor(ContextCompat.getColor(mContext, R.color.blue_0188cc));
            holder.tvDeviceState.setText(mContext.getString(R.string.device_state_online));
            holder.tvDeviceState.setTextColor(ContextCompat.getColor(mContext, R.color.blue_0188cc));
        }
        holder.tvDeviceName.setText(device.nickName);
    }

    @Override
    protected ViewHolder createViewHolder(int position, LayoutInflater inflater, ViewGroup parent) {
        final View convertView = inflater.inflate(R.layout.sensor_device_item, parent, false);
        return new DeviceViewHolder(convertView);
    }

    static class DeviceViewHolder extends ViewHolder {
        @Bind(R.id.iv_device)
        ImageView ivDevice;
        @Bind(R.id.tv_device_name)
        TextView tvDeviceName;
        @Bind(R.id.iv_arrow)
        ImageView ivArrow;
        @Bind(R.id.tv_device_state)
        TextView tvDeviceState;

        public DeviceViewHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this, convertView);
        }
    }
}
